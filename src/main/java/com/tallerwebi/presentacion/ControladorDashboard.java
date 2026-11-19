package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
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
  private static final String HOGAR_ACTIVO_EN_SESSION = "hogarActivoEnSession";
  private static final String REDIRECT_DASHBOARD = "redirect:/dashboard";

  @Autowired
  ServicioUsuario servicioUsuario;

  @Autowired
  ServicioCalculosGasto servicioCalculosGasto;

  @Autowired
  ServicioRegistroGasto servicioRegistroGasto;

  @Autowired
  ServicioHogar servicioHogar;

  @GetMapping("/dashboard")
  public ModelAndView irADashboard(HttpSession session) {
    Map<String, Object> model = new HashMap<>();

    Long usuarioId = SessionUtils.obtenerUsuarioIdDeSesion(session);
    List<Hogar> hogaresUsuario = servicioHogar.obtenerHogares(usuarioId);
    model.put("hogaresUsuario", hogaresUsuario);
    model.put("sinHogar", hogaresUsuario == null || hogaresUsuario.isEmpty());

    if (hogaresUsuario != null && !hogaresUsuario.isEmpty()) {
      Hogar hogarActivo = resolverHogarActivo(session, hogaresUsuario);
      session.setAttribute(HOGAR_ID, hogarActivo.getId());
      session.setAttribute(HOGAR_ACTIVO_EN_SESSION, hogarActivo);

      procesarMetricasHogar(hogarActivo, model);
      Long hogarId = hogarActivo.getId();
      List<Gasto> todosLosGastos = servicioRegistroGasto.obtenerTodosLosGastos(hogarId);
      List<Gasto> gastosPagados = todosLosGastos
        .stream()
        .filter(g -> g.getPagado() != null && g.getPagado())
        .toList();

      double totalGastosPagados = gastosPagados.stream().mapToDouble(Gasto::getMonto).sum();
      model.put("totalGastosMes", totalGastosPagados);
      model.put("cantidadGastos", gastosPagados.size());
    } else {
      session.removeAttribute(HOGAR_ID);
      session.removeAttribute(HOGAR_ACTIVO_EN_SESSION);
    }

    return new ModelAndView("dashboard", model);
  }

  private Hogar resolverHogarActivo(HttpSession session, List<Hogar> hogaresUsuario) {
    Object hogarIdSesion = session.getAttribute(HOGAR_ID);
    if (hogarIdSesion instanceof Long idSesion) {
      for (Hogar h : hogaresUsuario) {
        if (h.getId() != null && h.getId().equals(idSesion)) {
          return h;
        }
      }
    }
    return hogaresUsuario.getFirst();
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
      double totalOcasionales = obtenerTotalGastosOcasionales(
        hogarActual.getId(),
        usuariosAgregados
      );
      asignarHorasSegunReglaTareas(
        usuariosAgregados,
        precioHora,
        totalOcasionales,
        valorPisoMinimo
      );
    }

    model.put("usuarios", usuariosAgregados);
    model.put("sugerenciaProximoPagador", resolverSugerenciaPagador(hogarActual));
  }

  private void asignarHorasSegunReglaTareas(
    List<Usuario> integrantes,
    Double precioHora,
    double totalOcasionales,
    Double valorPisoMinimo
  ) {
    if (precioHora == null || precioHora <= 0) {
      return;
    }
    for (Usuario usuario : integrantes) {
      Double montoBrecha = servicioCalculosGasto.calcularValorDeHorasDeTareaPorUsuario(
        usuario,
        precioHora,
        totalOcasionales,
        valorPisoMinimo,
        integrantes
      );
      double valor = (montoBrecha != null && montoBrecha > 0) ? montoBrecha : 0.0;
      double horas = valor > 0 ? Math.ceil((valor / precioHora) * 2.0) / 2.0 : 0.0;
      usuario.setHorasTareaARealizar(horas);
    }
  }

  private double obtenerTotalGastosOcasionales(Long hogarId, List<Usuario> integrantes) {
    if (hogarId == null || servicioRegistroGasto == null) {
      return 0.0;
    }
    List<Gasto> gastos = servicioRegistroGasto.obtenerTodosLosGastos(hogarId);
    if (gastos == null || integrantes == null || integrantes.isEmpty()) {
      return 0.0;
    }
    return gastos
      .stream()
      .filter(g -> g != null && g.getMonto() != null && g.getTipoGasto() == TipoGasto.OCASIONAL)
      .filter(g -> esGastoCompartidoPorTodos(g, integrantes))
      .mapToDouble(Gasto::getMonto)
      .sum();
  }

  private boolean esGastoCompartidoPorTodos(Gasto gasto, List<Usuario> integrantes) {
    List<Usuario> participantes = gasto.getParticipantes();
    return (
      participantes == null || participantes.isEmpty() || participantes.size() >= integrantes.size()
    );
  }

  private String resolverSugerenciaPagador(Hogar hogarActual) {
    return obtenerSugerenciaSegura(hogarActual.getId());
  }

  private String obtenerSugerenciaSegura(Long hogarId) {
    try {
      return servicioRegistroGasto.obtenerSugerenciaProximoPagador(hogarId);
    } catch (Exception e) {
      return "Sin datos";
    }
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
      session.setAttribute(HOGAR_ACTIVO_EN_SESSION, nuevoHogar);
    }

    return new ModelAndView(REDIRECT_DASHBOARD);
  }

  @PostMapping("/previsualizar-unirse")
  public ModelAndView previsualizarUnirse(
    @RequestParam("pin") String pin,
    HttpSession session,
    RedirectAttributes redirectAttributes
  ) {
    Hogar hogarEncontrado = servicioHogar.buscarPorPin(pin);

    if (hogarEncontrado == null) {
      redirectAttributes.addFlashAttribute(
        "errorPin",
        "El PIN ingresado es incorrecto o no existe un hogar con el mismo."
      );
      return new ModelAndView(REDIRECT_DASHBOARD);
    }

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
        session.setAttribute(HOGAR_ACTIVO_EN_SESSION, hogar);
      }
    }

    return new ModelAndView(REDIRECT_DASHBOARD);
  }
}
