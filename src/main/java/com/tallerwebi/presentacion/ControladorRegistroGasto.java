package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.DescripcionGastoVaciaException;
import com.tallerwebi.dominio.excepcion.MontoGastoInvalidoException;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorRegistroGasto {

  private static final String REDIRECT_GASTOS = "redirect:/gastos";

  private final ServicioRegistroGasto servicioRegistroGasto;
  private final ServicioUsuario servicioUsuario;

  public ControladorRegistroGasto(
    ServicioRegistroGasto servicioRegistroGasto,
    ServicioUsuario servicioUsuario
  ) {
    this.servicioRegistroGasto = servicioRegistroGasto;
    this.servicioUsuario = servicioUsuario;
  }

  @RequestMapping(path = "/gastos", method = RequestMethod.GET)
  public ModelAndView irAGastos(HttpSession session, RedirectAttributes redirectAttributes) {
    Long hogarId = obtenerHogarIdDeSesion(session);

    // Si no tiene hogar asignado en la sesión, lo mandamos a la vista de dashboard para que seleccione o cree un hogar
    if (hogarId == null) {
      redirectAttributes.addFlashAttribute(
        "alertaHogar",
        "¡Atención! Para acceder a las funcionalidades de KUMO debes ser parte de un hogar. Creá uno o unite a uno existente."
      );
      return new ModelAndView("redirect:/dashboard");
    }

    ModelAndView mav = new ModelAndView("gastos");
    Long usuarioId = SessionUtils.obtenerUsuarioIdDeSesion(session);
    Usuario usuarioActual = servicioUsuario.obtenerUsuarioPorId(usuarioId);
    mav.addObject(
      "nombreUsuario",
      usuarioActual.getNombre() != null ? usuarioActual.getNombre() : "Usuario"
    );
    cargarDatosGenerales(mav, hogarId);
    mav.addObject("gasto", new Gasto());
    return mav;
  }

  @RequestMapping(path = "/gastos/guardar", method = RequestMethod.POST)
  public ModelAndView registrarGasto(
    @ModelAttribute("gasto") Gasto gasto,
    @RequestParam(value = "participantesIds", required = false) List<Long> participantesIds,
    HttpSession session
  ) {
    ModelAndView mav = new ModelAndView();
    Long hogarId = obtenerHogarIdDeSesion(session);

    try {
      this.servicioRegistroGasto.registrarGastoConParticipantes(gasto, hogarId, participantesIds);
      mav.setViewName(REDIRECT_GASTOS);
      return mav;
    } catch (DescripcionGastoVaciaException e) {
      mav.addObject("mensajeError", "La descripción del gasto no puede estar vacía.");
    } catch (MontoGastoInvalidoException e) {
      mav.addObject("mensajeError", "El monto del gasto debe ser mayor a cero.");
    }

    mav.setViewName("gastos");
    cargarDatosGenerales(mav, hogarId);
    mav.addObject("gasto", gasto);
    return mav;
  }

  @RequestMapping(path = "/gastos/pagar", method = RequestMethod.POST)
  public ModelAndView pagarGasto(
    @RequestParam("gastoId") Long gastoId, // Recibimos Long en lugar de String
    @RequestParam("pagadorId") Long pagadorId
  ) {
    this.servicioRegistroGasto.marcarComoPagado(gastoId, pagadorId);
    return new ModelAndView(REDIRECT_GASTOS);
  }

  @RequestMapping(path = "/gastos/actualizar-fijo", method = RequestMethod.POST)
  public ModelAndView actualizarGastoFijo(
    @RequestParam("id") Long id,
    @RequestParam("monto") Double monto,
    @RequestParam("fechaVencimiento") String fechaVencimientoStr
  ) {
    LocalDateTime nuevaFecha = (fechaVencimientoStr != null && !fechaVencimientoStr.isEmpty())
      ? LocalDateTime.parse(fechaVencimientoStr)
      : null;
    this.servicioRegistroGasto.actualizarGastoFijo(id, monto, nuevaFecha);
    return new ModelAndView(REDIRECT_GASTOS);
  }

  @RequestMapping(path = "/gastos/eliminar", method = RequestMethod.POST)
  public ModelAndView eliminarGasto(@RequestParam("id") Long id) {
    this.servicioRegistroGasto.eliminarGasto(id);
    return new ModelAndView(REDIRECT_GASTOS);
  }

  private void cargarDatosGenerales(ModelAndView mav, Long hogarId) {
    mav.addObject("listaGastos", servicioRegistroGasto.obtenerTodosLosGastos(hogarId));
    mav.addObject(
      "gastosFijosRecurrentes",
      servicioRegistroGasto.obtenerGastosFijosRecurrentes(hogarId)
    );
    mav.addObject("integrantes", servicioRegistroGasto.obtenerIntegrantes(hogarId));
    mav.addObject(
      "sugerenciaProximoPagador",
      servicioRegistroGasto.obtenerSugerenciaProximoPagador(hogarId)
    );
    mav.addObject("hoy", LocalDateTime.now()); // Pasamos LocalDateTime.now() para la comparación
  }

  @RequestMapping("/vencimientos")
  public ModelAndView irAVencimientos(
    @RequestParam(value = "filtro", defaultValue = "todos") String filtro,
    HttpSession session,
    RedirectAttributes redirectAttributes
  ) {
    LocalDateTime ahora = LocalDateTime.now();
    Long hogarId = obtenerHogarIdDeSesion(session);
    if (hogarId == null) {
      redirectAttributes.addFlashAttribute(
        "alertaHogar",
        "¡Atención! Para acceder a las funcionalidades de KUMO debes ser parte de un hogar. Creá uno o unite a uno existente."
      );
      return new ModelAndView("redirect:/dashboard");
    }

    List<Gasto> vencimientos =
      switch (filtro) {
        case "proximos" -> servicioRegistroGasto.obtenerVencimientosProximos(ahora, hogarId);
        case "vencidos" -> servicioRegistroGasto.obtenerVencimientosVencidos(ahora, hogarId);
        default -> servicioRegistroGasto.obtenerVencimientosPendientes(hogarId);
      };

    String filtroAplicado = "proximos".equals(filtro) || "vencidos".equals(filtro)
      ? filtro
      : "todos";

    ModelAndView mav = new ModelAndView("vencimientos");
    mav.addObject("vencimientos", vencimientos);
    mav.addObject("filtro", filtroAplicado);
    mav.addObject("hoy", ahora);
    return mav;
  }

  private Long obtenerHogarIdDeSesion(HttpSession session) {
    Long hogarId = (Long) session.getAttribute("HOGAR_ID");
    // Si no hay hogar en sesión, devolvemos null en lugar de un ID fijo (1L) para evitar cruce de datos
    return hogarId;
  }
}
