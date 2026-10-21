package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Gasto;
import com.tallerwebi.dominio.ServicioRegistroGasto;
import com.tallerwebi.dominio.excepcion.DescripcionGastoVaciaException;
import com.tallerwebi.dominio.excepcion.MontoGastoInvalidoException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorRegistroGasto {

  private static final String REDIRECT_GASTOS = "redirect:/gastos";

  private final ServicioRegistroGasto servicioRegistroGasto;

  public ControladorRegistroGasto(ServicioRegistroGasto servicioRegistroGasto) {
    this.servicioRegistroGasto = servicioRegistroGasto;
  }

  @RequestMapping(path = "/gastos", method = RequestMethod.GET)
  public ModelAndView irAGastos() {
    ModelAndView mav = new ModelAndView("gastos");
    cargarDatosGenerales(mav);
    mav.addObject("gasto", new Gasto());
    return mav;
  }

  @RequestMapping(path = "/gastos/guardar", method = RequestMethod.POST)
  public ModelAndView registrarGasto(@ModelAttribute("gasto") Gasto gasto) {
    ModelAndView mav = new ModelAndView();

    try {
      this.servicioRegistroGasto.registrarGasto(gasto);
      mav.setViewName(REDIRECT_GASTOS);
      return mav;
    } catch (DescripcionGastoVaciaException e) {
      mav.addObject("mensajeError", "La descripción del gasto no puede estar vacía.");
    } catch (MontoGastoInvalidoException e) {
      mav.addObject("mensajeError", "El monto del gasto debe ser mayor a cero.");
    }

    mav.setViewName("gastos");
    cargarDatosGenerales(mav);
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

  private void cargarDatosGenerales(ModelAndView mav) {
    mav.addObject("listaGastos", servicioRegistroGasto.obtenerTodosLosGastos());
    mav.addObject("gastosFijosRecurrentes", servicioRegistroGasto.obtenerGastosFijosRecurrentes());
    mav.addObject("integrantes", servicioRegistroGasto.obtenerIntegrantes());
    mav.addObject(
      "sugerenciaProximoPagador",
      servicioRegistroGasto.obtenerSugerenciaProximoPagador()
    );
    mav.addObject("hoy", LocalDateTime.now()); // Pasamos LocalDateTime.now() para la comparación
  }

  @RequestMapping("/vencimientos")
  public ModelAndView irAVencimientos(
    @RequestParam(value = "filtro", defaultValue = "todos") String filtro
  ) {
    LocalDateTime ahora = LocalDateTime.now();

    List<Gasto> vencimientos =
      switch (filtro) {
        case "proximos" -> servicioRegistroGasto.obtenerVencimientosProximos(ahora);
        case "vencidos" -> servicioRegistroGasto.obtenerVencimientosVencidos(ahora);
        default -> servicioRegistroGasto.obtenerVencimientosPendientes();
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
}
