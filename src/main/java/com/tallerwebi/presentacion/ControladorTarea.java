package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.AlcanceTarea;
import com.tallerwebi.dominio.Gasto;
import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.PagoPendiente;
import com.tallerwebi.dominio.ServicioCreditoTarea;
import com.tallerwebi.dominio.ServicioHogar;
import com.tallerwebi.dominio.ServicioLiquidacion;
import com.tallerwebi.dominio.ServicioPagoPendiente;
import com.tallerwebi.dominio.ServicioRegistroGasto;
import com.tallerwebi.dominio.ServicioTareaRepetitiva;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Tarea;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.TareaNoEncontradaException;
import com.tallerwebi.dominio.excepcion.TareaYaCompletadaException;
import com.tallerwebi.dominio.excepcion.ValorCreditoInvalidoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorTarea {

  private static final String VISTA_TAREAS = "tareas";
  private static final String ATRIBUTO_NUEVA_TAREA = "nuevaTarea";
  private static final String ATRIBUTO_ERROR = "error";
  private static final String REDIRECT_TAREAS = "redirect:/tareas";

  private static final String CLAVE_DINERO_BRECHA = "dineroRequeridoBrecha";
  private static final String CLAVE_HORAS_BRECHA = "horasRequeridasBrecha";

  private final ServicioTareaRepetitiva servicioTareaRepetitiva;
  private final ServicioCreditoTarea servicioCreditoTarea;
  private final ServicioUsuario servicioUsuario;
  private final ServicioHogar servicioHogar;
  private final ServicioLiquidacion servicioLiquidacion;
  private final ServicioRegistroGasto servicioRegistroGasto;
  private final ServicioPagoPendiente servicioPagoPendiente;

  @Autowired
  public ControladorTarea(
    ServicioCreditoTarea servicioCreditoTarea,
    ServicioUsuario servicioUsuario,
    ServicioTareaRepetitiva servicioTareaRepetitiva,
    ServicioHogar servicioHogar,
    ServicioLiquidacion servicioLiquidacion,
    ServicioRegistroGasto servicioRegistroGasto,
    ServicioPagoPendiente servicioPagoPendiente
  ) {
    this.servicioCreditoTarea = servicioCreditoTarea;
    this.servicioTareaRepetitiva = servicioTareaRepetitiva;
    this.servicioUsuario = servicioUsuario;
    this.servicioHogar = servicioHogar;
    this.servicioLiquidacion = servicioLiquidacion;
    this.servicioRegistroGasto = servicioRegistroGasto;
    this.servicioPagoPendiente = servicioPagoPendiente;
  }

  @GetMapping("/tareas")
  public ModelAndView irATareas(
    HttpServletRequest request,
    HttpSession session,
    RedirectAttributes redirectAttributes
  ) {
    Map<String, Object> model = new HashMap<>();
    Long usuarioId = obtenerUsuarioIdDeSesion(session);
    Long hogarId = (Long) session.getAttribute("HOGAR_ID");
    if (hogarId == null) {
      redirectAttributes.addFlashAttribute(
        "alertaHogar",
        "Para ver las tareas y otras secciones debes pertenecer a un hogar."
      );
      return new ModelAndView(REDIRECT_TAREAS);
    }

    cargarDatosDashboardTareas(model, usuarioId, hogarId);
    model.put(ATRIBUTO_NUEVA_TAREA, new Tarea());

    return new ModelAndView(VISTA_TAREAS, model);
  }

  @PostMapping("/tareas/crear")
  public ModelAndView crearTarea(
    @ModelAttribute(ATRIBUTO_NUEVA_TAREA) Tarea tarea,
    HttpServletRequest request
  ) {
    Map<String, Object> model = new HashMap<>();
    Long usuarioId = obtenerUsuarioIdDeSesion(request);
    Long hogarId = obtenerHogarIdDeSesion(request);
    Long tareaRepetitivaId = obtenerTareaRepetitivaId(request);

    try {
      if (tareaRepetitivaId != null) {
        servicioCreditoTarea.registrarTareaDesdeCatalogo(tareaRepetitivaId, null, hogarId);
      } else {
        servicioCreditoTarea.registrarTarea(tarea);
        if (request.getParameter("guardarComoPlantilla") != null) {
          guardarPlantilla(
            tarea.getTitulo(),
            request.getParameter("alcancePlantilla"),
            usuarioId,
            hogarId
          );
        }
      }
      return new ModelAndView(REDIRECT_TAREAS);
    } catch (TareaNoEncontradaException e) {
      model.put(ATRIBUTO_ERROR, "La tarea seleccionada no existe en tu hogar.");
    } catch (ValorCreditoInvalidoException e) {
      model.put(ATRIBUTO_ERROR, "El título de la tarea no puede estar vacío.");
    }
    cargarDatosDashboardTareas(model, usuarioId, hogarId);
    return new ModelAndView(VISTA_TAREAS, model);
  }

  @PostMapping("/tareas/completar")
  public ModelAndView completarTarea(
    @RequestParam(value = "tareaId", required = false) Long tareaId,
    @RequestParam(value = "horasDedicadas", required = false) Double horasDedicadas,
    HttpServletRequest request
  ) {
    Map<String, Object> model = new HashMap<>();
    Long usuarioId = obtenerUsuarioIdDeSesion(request);
    Long hogarId = obtenerHogarIdDeSesion(request);

    if (tareaId == null) {
      model.put(ATRIBUTO_ERROR, "Identificador de tarea inválido.");
      cargarDatosDashboardTareas(model, usuarioId, hogarId);
      model.put(ATRIBUTO_NUEVA_TAREA, new Tarea());
      return new ModelAndView(VISTA_TAREAS, model);
    }

    try {
      Hogar hogar = servicioHogar.buscarPorId(hogarId);
      Double tarifaHogar = obtenerPrecioHoraHogar(hogar);

      servicioCreditoTarea.completarTareaYGenerarCredito(
        tareaId,
        usuarioId,
        horasDedicadas,
        tarifaHogar
      );
      return new ModelAndView(REDIRECT_TAREAS);
    } catch (TareaNoEncontradaException e) {
      model.put(ATRIBUTO_ERROR, "La tarea seleccionada no existe.");
    } catch (TareaYaCompletadaException e) {
      model.put(ATRIBUTO_ERROR, "Esta tarea ya fue completada previamente.");
    } catch (ValorCreditoInvalidoException e) {
      model.put(ATRIBUTO_ERROR, "La cantidad de horas ingresadas debe ser mayor a cero.");
    }

    cargarDatosDashboardTareas(model, usuarioId, hogarId);
    model.put(ATRIBUTO_NUEVA_TAREA, new Tarea());
    return new ModelAndView(VISTA_TAREAS, model);
  }

  @PostMapping("/tareas/liquidar-creditos")
  public ModelAndView liquidarCreditos(HttpServletRequest request) {
    Long usuarioId = obtenerUsuarioIdDeSesion(request);
    Long hogarId = obtenerHogarIdDeSesion(request);
    try {
      Hogar hogar = servicioHogar.buscarPorId(hogarId);
      List<Gasto> gastos = hogar != null
        ? servicioRegistroGasto.obtenerTodosLosGastos(hogarId)
        : Collections.emptyList();

      // Delegamos toda la lógica pesada de liquidación al servicio
      servicioCreditoTarea.procesarLiquidacionCreditos(usuarioId, hogarId, gastos, hogar);

      return new ModelAndView(REDIRECT_TAREAS);
    } catch (Exception e) {
      return new ModelAndView(REDIRECT_TAREAS);
    }
  }

  /* --- Métodos Auxiliares Privados del Controlador --- */

  private void cargarDatosDashboardTareas(Map<String, Object> model, Long usuarioId, Long hogarId) {
    Double creditoTotalBruto = servicioCreditoTarea.obtenerCreditoAcumuladoPorUsuario(usuarioId);
    model.put("tareas", servicioCreditoTarea.obtenerTodasLasTareas());
    model.put("catalogo", servicioTareaRepetitiva.obtenerCatalogo(hogarId, usuarioId));

    Double creditoLiquidadoMes = servicioCreditoTarea.obtenerCreditoLiquidadoPorUsuario(usuarioId);
    model.put("creditoLiquidadoMes", creditoLiquidadoMes);

    Hogar hogar = servicioHogar.buscarPorId(hogarId);
    Double precioHora = obtenerPrecioHoraHogar(hogar);
    model.put("precioHoraHogar", precioHora);

    Usuario usuarioActual = servicioUsuario.obtenerUsuarioPorId(usuarioId);
    List<Gasto> gastos = hogar != null
      ? servicioRegistroGasto.obtenerTodosLosGastos(hogarId)
      : Collections.emptyList();

    // Delegamos el cálculo de métricas de brecha al servicio
    Map<String, Double> metricasBrecha = servicioCreditoTarea.calcularMetricasBrechaUsuario(
      usuarioActual,
      hogar,
      gastos,
      precioHora
    );
    Double dineroRequeridoBrecha = metricasBrecha.get(CLAVE_DINERO_BRECHA);
    Double horasRequeridasBrecha = metricasBrecha.get(CLAVE_HORAS_BRECHA);

    double deudaUsuario = (hogar != null && usuarioActual != null)
      ? servicioLiquidacion.obtenerDeudaUsuario(usuarioActual, hogar, gastos)
      : 0.0;

    Double baseACompensar = Math.max(dineroRequeridoBrecha, deudaUsuario);

    double creditoAcumuladoNeto = Math.max(
      0.0,
      (creditoTotalBruto != null ? creditoTotalBruto : 0.0) - baseACompensar
    );

    model.put("creditoAcumulado", creditoAcumuladoNeto);

    boolean tienePagoPendiente = tienePagoPendienteActivo(hogar, usuarioActual);
    model.put("tienePagoPendiente", tienePagoPendiente);

    boolean puedeLiquidar = (creditoTotalBruto > 0) && (deudaUsuario > 0) && !tienePagoPendiente;
    model.put("puedeLiquidar", puedeLiquidar);
    model.put("deudaUsuario", deudaUsuario);

    Double dineroFaltante = Math.max(
      0.0,
      dineroRequeridoBrecha - (creditoTotalBruto != null ? creditoTotalBruto : 0.0)
    );

    Double horasFaltantes = (precioHora > 0)
      ? Math.ceil((dineroFaltante / precioHora) * 2.0) / 2.0
      : 0.0;
    boolean tieneRetencionDeCreditos = dineroFaltante > 0;

    model.put("horasRealizadas", creditoTotalBruto);
    model.put(CLAVE_HORAS_BRECHA, horasRequeridasBrecha);
    model.put(CLAVE_DINERO_BRECHA, dineroRequeridoBrecha);
    model.put("horasFaltantes", horasFaltantes);
    model.put("dineroFaltante", dineroFaltante);
    model.put("tieneRetencionDeCreditos", tieneRetencionDeCreditos);
  }

  private boolean tienePagoPendienteActivo(Hogar hogar, Usuario usuarioActual) {
    if (servicioPagoPendiente == null || hogar == null || usuarioActual == null) {
      return false;
    }
    List<PagoPendiente> transferenciasPendientes = servicioPagoPendiente.obtenerPendientes(hogar);
    return transferenciasPendientes.stream().anyMatch(p -> coincideEmisor(p, usuarioActual));
  }

  private boolean coincideEmisor(PagoPendiente pagoPendiente, Usuario usuarioActual) {
    String nombreEmisor = pagoPendiente.getEmisor().getNombre();
    if (nombreEmisor == null) {
      return false;
    }
    return (
      nombreEmisor.equals(usuarioActual.getNombre()) ||
      nombreEmisor.equals(usuarioActual.getEmail())
    );
  }

  private Double obtenerPrecioHoraHogar(Hogar hogar) {
    return (hogar != null && hogar.getPrecioHoraTarea() != null)
      ? hogar.getPrecioHoraTarea()
      : 1000.0;
  }

  private Long obtenerUsuarioIdDeSesion(HttpServletRequest request) {
    Long usuarioId = (Long) request.getSession().getAttribute("USUARIO_ID");
    if (usuarioId == null) {
      return 1L;
    }
    return usuarioId;
  }

  private Long obtenerUsuarioIdDeSesion(HttpSession session) {
    if (session == null) {
      return 1L;
    }
    Long usuarioId = (Long) session.getAttribute("USUARIO_ID");
    if (usuarioId == null) {
      return 1L;
    }
    return usuarioId;
  }

  private Long obtenerHogarIdDeSesion(HttpServletRequest request) {
    Object hogarId = request.getSession().getAttribute("HOGAR_ID");
    if (hogarId instanceof Long) {
      return (Long) hogarId;
    }
    return 1L;
  }

  private Long obtenerTareaRepetitivaId(HttpServletRequest request) {
    String valor = request.getParameter("tareaRepetitivaId");
    if (valor == null || valor.isBlank()) {
      return null;
    }
    return Long.valueOf(valor);
  }

  private void guardarPlantilla(String titulo, String alcance, Long usuarioId, Long hogarId) {
    if (AlcanceTarea.PERSONAL.name().equals(alcance)) {
      Usuario propietario = servicioUsuario.obtenerUsuarioPorId(usuarioId);
      servicioTareaRepetitiva.guardarComoPlantillaPersonal(titulo, hogarId, propietario);
    } else {
      servicioTareaRepetitiva.guardarComoPlantilla(titulo, hogarId);
    }
  }
}
