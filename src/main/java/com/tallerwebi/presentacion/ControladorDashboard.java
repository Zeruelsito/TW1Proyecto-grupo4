package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.RepositorioHogar;
import com.tallerwebi.dominio.ServicioCalculosGasto;
import com.tallerwebi.dominio.ServicioHogar;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorDashboard {

  private static final String HOGAR_ID = "HOGAR_ID";
  private static final String REDIRECT_DASHBOARD = "redirect:/dashboard";

  @Autowired
  ServicioUsuario servicioUsuario;

  @Autowired
  ServicioCalculosGasto servicioCalculosGasto;

  @Autowired
  ServicioHogar servicioHogar;

  @Autowired
  RepositorioHogar repositorioHogar;

  @GetMapping("/dashboard")
  public ModelAndView irADashboard(HttpSession session) {
    Map<String, Object> model = new HashMap<>();

    Long usuarioId = SessionUtils.obtenerUsuarioIdDeSesion(session);
    Usuario usuarioActual = servicioUsuario.obtenerUsuarioPorId(usuarioId);
    if (usuarioActual == null) {
      usuarioActual = new Usuario("Juan", 300000.0);
      usuarioActual.setId(usuarioId);
    }

    model.put(
      "nombreUsuario",
      usuarioActual.getNombre() != null ? usuarioActual.getNombre() : "Usuario"
    );

    List<Hogar> hogaresUsuario = servicioHogar.obtenerHogares(usuarioId);
    model.put("hogaresUsuario", hogaresUsuario);

    if (hogaresUsuario != null && !hogaresUsuario.isEmpty()) {
      Hogar primerHogar = hogaresUsuario.getFirst();
      if (session.getAttribute(HOGAR_ID) == null) {
        session.setAttribute(HOGAR_ID, primerHogar.getId());
        session.setAttribute("hogarActivoEnSession", primerHogar);
      }
      procesarMetricasHogar(primerHogar, model);
    }

    return new ModelAndView("dashboard", model);
  }

  private void procesarMetricasHogar(Hogar hogarActual, Map<String, Object> model) {
    List<Usuario> usuariosAgregados = obtenerIntegrantesOTodos(hogarActual);
    verificarYAsignarIngresos(usuariosAgregados);

    Double valorPisoMinimo = verificarPisoMinimo(hogarActual);

    if (usuariosAgregados != null && !usuariosAgregados.isEmpty()) {
      servicioCalculosGasto.calcularTotalSumaDeCapacidadesDeCadaUsuario(
        usuariosAgregados,
        valorPisoMinimo
      );
      servicioCalculosGasto.establecerMontoTotalDeLasSumasDeCapacidades(usuariosAgregados);
      servicioCalculosGasto.calcularPorcentajesDeCapacidadMensualDeUsuarios(usuariosAgregados);
      servicioCalculosGasto.calcularCuotasMensualesDeUsuarios(usuariosAgregados, valorPisoMinimo);

      Double precioHora = hogarActual.getPrecioHoraTarea() != null
        ? hogarActual.getPrecioHoraTarea()
        : 5000d;
      servicioCalculosGasto.calcularHorasDeTareasDeUsuarios(usuariosAgregados, precioHora);
    }

    model.put("usuarios", usuariosAgregados);
  }

  private List<Usuario> obtenerIntegrantesOTodos(Hogar hogarActual) {
    List<Usuario> integrantes = hogarActual.getIntegrantes();
    if (integrantes == null || integrantes.isEmpty()) {
      return servicioUsuario.obtenerTodos();
    }
    return integrantes;
  }

  private void verificarYAsignarIngresos(List<Usuario> usuarios) {
    for (Usuario u : usuarios) {
      if (u.getIngresoMensual() == null) {
        u.setIngresoMensual(150000.0);
      }
    }
  }

  private Double verificarPisoMinimo(Hogar hogarActual) {
    Double valorPisoMinimo = hogarActual.getPisoMinimoHogar();
    if (valorPisoMinimo == null || valorPisoMinimo <= 0.0) {
      valorPisoMinimo = 50000.0;
      hogarActual.setPisoMinimoHogar(valorPisoMinimo);
    }
    return valorPisoMinimo;
  }

  @PostMapping("/crear-hogar")
  public ModelAndView crearHogar(
    @RequestParam("nombreHogar") String nombreHogar,
    @RequestParam("pin") String pin,
    HttpSession session
  ) {
    Long usuarioId = SessionUtils.obtenerUsuarioIdDeSesion(session);
    Usuario usuario = servicioUsuario.obtenerUsuarioPorId(usuarioId);

    if (usuario != null) {
      Hogar nuevoHogar = new Hogar(nombreHogar, pin);
      nuevoHogar.getIntegrantes().add(usuario);
      servicioHogar.crearHogar(nuevoHogar);
      session.setAttribute(HOGAR_ID, nuevoHogar.getId());
      session.setAttribute("hogarActivoEnSession", nuevoHogar);
    }

    return new ModelAndView(REDIRECT_DASHBOARD);
  }

  // NUEVO FLUJO PASO 1: Buscar hogar por PIN para previsualizarlo
  @PostMapping("/previsualizar-unirse")
  public ModelAndView previsualizarUnirse(
    @RequestParam("pin") String pin,
    HttpSession session,
    RedirectAttributes redirectAttributes
  ) {
    // Buscamos a través del servicio para asegurar la transacción de Hibernate
    Hogar hogarEncontrado = servicioHogar.buscarPorPin(pin);

    if (hogarEncontrado == null) {
      redirectAttributes.addFlashAttribute(
        "errorPin",
        "El PIN ingresado es incorrecto o no existe un hogar con el mismo."
      );
      return new ModelAndView(REDIRECT_DASHBOARD);
    }

    // Validar si el usuario ya es parte de este hogar
    Long usuarioId = SessionUtils.obtenerUsuarioIdDeSesion(session);
    List<Hogar> hogaresDelUsuario = servicioHogar.obtenerHogares(usuarioId);

    boolean yaEsParte = hogaresDelUsuario
      .stream()
      .anyMatch(h -> h.getId().equals(hogarEncontrado.getId()));

    if (yaEsParte) {
      redirectAttributes.addFlashAttribute(
        "errorPin",
        "Ya formás parte de este hogar, no podés volver a unirte."
      );
      return new ModelAndView(REDIRECT_DASHBOARD);
    }

    redirectAttributes.addFlashAttribute("hogarPrevisualizado", hogarEncontrado);
    return new ModelAndView(REDIRECT_DASHBOARD);
  }

  // NUEVO FLUJO PASO 2: Confirmar la unión definitiva al hogar encontrado
  @PostMapping("/confirmar-unirse-hogar")
  public ModelAndView confirmarUnirseHogar(
    @RequestParam("hogarId") Long hogarId,
    HttpSession session
  ) {
    Long usuarioId = SessionUtils.obtenerUsuarioIdDeSesion(session);
    Usuario usuario = servicioUsuario.obtenerUsuarioPorId(usuarioId);

    if (usuario != null) {
      Hogar hogar = servicioHogar.buscarPorId(hogarId);
      if (hogar != null) {
        servicioHogar.unirseAHogar(hogar.getNombreHogar(), hogar.getPin(), usuario);
        session.setAttribute(HOGAR_ID, hogar.getId());
        session.setAttribute("hogarActivoEnSession", hogar);
      }
    }

    return new ModelAndView(REDIRECT_DASHBOARD);
  }
}
