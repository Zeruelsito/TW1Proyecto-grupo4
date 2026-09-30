package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioCreditoTarea;
import com.tallerwebi.dominio.Tarea;
import com.tallerwebi.dominio.excepcion.TareaNoEncontradaException;
import com.tallerwebi.dominio.excepcion.TareaYaCompletadaException;
import com.tallerwebi.dominio.excepcion.ValorCreditoInvalidoException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorTarea {

  private static final String VISTA_TAREAS = "tareas";
  private static final String ATRIBUTO_NUEVA_TAREA = "nuevaTarea";
  private static final String ATRIBUTO_ERROR = "error";

  private final ServicioCreditoTarea servicioCreditoTarea;

  @Autowired
  public ControladorTarea(ServicioCreditoTarea servicioCreditoTarea) {
    this.servicioCreditoTarea = servicioCreditoTarea;
  }

  @GetMapping("/tareas")
  public ModelAndView irATareas(HttpServletRequest request) {
    Map<String, Object> model = new HashMap<>();
    Long usuarioId = obtenerUsuarioIdDeSesion(request);

    cargarDatosDashboardTareas(model, usuarioId);
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

    try {
      servicioCreditoTarea.registrarTarea(tarea);
      return new ModelAndView("redirect:/tareas");
    } catch (ValorCreditoInvalidoException e) {
      model.put(ATRIBUTO_ERROR,"El valor del crédito debe ser mayor a $0.";
      cargarDatosDashboardTareas(model, usuarioId);
      return new ModelAndView(VISTA_TAREAS, model);
    }
  }

  @PostMapping("/tareas/completar")
  public ModelAndView completarTarea(
    @RequestParam(value = "tareaId", required = false) Long tareaId,
    HttpServletRequest request
  ) {
    Map<String, Object> model = new HashMap<>();
    Long usuarioId = obtenerUsuarioIdDeSesion(request);

    if (tareaId == null) {
      model.put(ATRIBUTO_ERROR, "Identificador de tarea inválido.");
      cargarDatosDashboardTareas(model, usuarioId);
      model.put(ATRIBUTO_NUEVA_TAREA, new Tarea());
      return new ModelAndView(VISTA_TAREAS, model);
    }

    try {
      servicioCreditoTarea.completarTareaYGenerarCredito(tareaId, usuarioId);
      return new ModelAndView("redirect:/tareas");
    } catch (TareaNoEncontradaException e) {
      model.put(ATRIBUTO_ERROR, "La tarea seleccionada no existe.");
    } catch (TareaYaCompletadaException e) {
      model.put(ATRIBUTO_ERROR, "Esta tarea ya fue completada previamente.");
    }

    cargarDatosDashboardTareas(model, usuarioId);
    model.put(ATRIBUTO_NUEVA_TAREA, new Tarea());
    return new ModelAndView(VISTA_TAREAS, model);
  }

  /* --- Métodos Auxiliares Privados para Cumplir PMD --- */

  private void cargarDatosDashboardTareas(Map<String, Object> model, Long usuarioId) {
    Double creditoAcumulado = servicioCreditoTarea.obtenerCreditoAcumuladoPorUsuario(usuarioId);
    model.put("tareas", servicioCreditoTarea.obtenerTodasLasTareas());
    model.put("creditoAcumulado", creditoAcumulado);

    // IMPORTANTE: Variable enviada a la vista
    model.put("precioHoraHogar", 1000.0);
  }

  private Long obtenerUsuarioIdDeSesion(HttpServletRequest request) {
    Long usuarioId = (Long) request.getSession().getAttribute("USUARIO_ID");
    if (usuarioId == null) {
      return 1L; // Fallback mock para desarrollo local
    }
    return usuarioId;
  }
}
