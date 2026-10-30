package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioCreditoTarea;
import com.tallerwebi.dominio.ServicioTareaRepetitiva;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Tarea;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.TareaNoEncontradaException;
import com.tallerwebi.dominio.excepcion.TareaYaCompletadaException;
import com.tallerwebi.dominio.excepcion.ValorCreditoInvalidoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
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

  private final ServicioTareaRepetitiva servicioTareaRepetitiva;
  private final ServicioCreditoTarea servicioCreditoTarea;
  private final ServicioUsuario servicioUsuario;

  @Autowired
  public ControladorTarea(
    ServicioCreditoTarea servicioCreditoTarea,
    ServicioUsuario servicioUsuario,
    ServicioTareaRepetitiva servicioTareaRepetitiva
  ) {
    this.servicioCreditoTarea = servicioCreditoTarea;
    this.servicioTareaRepetitiva = servicioTareaRepetitiva;
    this.servicioUsuario = servicioUsuario;
  }

  @GetMapping("/tareas")
  public ModelAndView irATareas(
    HttpServletRequest request,
    HttpSession session,
    RedirectAttributes redirectAttributes
  ) {
    Map<String, Object> model = new HashMap<>();
    Long usuarioId = SessionUtils.obtenerUsuarioIdDeSesion(session);
    Usuario usuarioActual = servicioUsuario.obtenerUsuarioPorId(usuarioId);
    Long hogarId = (Long) session.getAttribute("HOGAR_ID");
    if (hogarId == null) {
      redirectAttributes.addFlashAttribute(
        "alertaHogar",
        "Para ver la liquidación y otras secciones debes pertenecer a un hogar."
      );
      return new ModelAndView("redirect:/dashboard");
    }

    model.put(
      "nombreUsuario",
      usuarioActual.getNombre() != null ? usuarioActual.getNombre() : "Usuario"
    );

    cargarDatosDashboardTareas(model, usuarioId, obtenerHogarIdDeSesion(request));
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
        servicioCreditoTarea.registrarTareaDesdeCatalogo(
          tareaRepetitivaId,
          tarea.getHorasDedicadas(),
          hogarId
        );
      } else {
        servicioCreditoTarea.registrarTarea(tarea);
      }
      return new ModelAndView("redirect:/tareas");
    } catch (TareaNoEncontradaException e) {
      model.put(ATRIBUTO_ERROR, "La tarea seleccionada no existe en tu hogar.");
    } catch (ValorCreditoInvalidoException e) {
      model.put(ATRIBUTO_ERROR, "El valor del crédito debe ser mayor a $0.");
    }
    cargarDatosDashboardTareas(model, usuarioId, hogarId);
    return new ModelAndView(VISTA_TAREAS, model);
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
      cargarDatosDashboardTareas(model, usuarioId, obtenerHogarIdDeSesion(request));
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

    cargarDatosDashboardTareas(model, usuarioId, obtenerHogarIdDeSesion(request));
    model.put(ATRIBUTO_NUEVA_TAREA, new Tarea());
    return new ModelAndView(VISTA_TAREAS, model);
  }

  /* --- Métodos Auxiliares Privados para Cumplir PMD --- */

  private void cargarDatosDashboardTareas(Map<String, Object> model, Long usuarioId, Long hogarId) {
    Double creditoAcumulado = servicioCreditoTarea.obtenerCreditoAcumuladoPorUsuario(usuarioId);
    model.put("tareas", servicioCreditoTarea.obtenerTodasLasTareas());
    model.put("creditoAcumulado", creditoAcumulado);
    model.put("catalogo", servicioTareaRepetitiva.obtenerTodas(hogarId));

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
}
