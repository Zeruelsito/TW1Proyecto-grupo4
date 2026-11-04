package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioRegistroGasto;
import com.tallerwebi.dominio.ServicioTareaRepetitiva;
import com.tallerwebi.dominio.excepcion.HogarSinIntegrantesException;
import com.tallerwebi.dominio.excepcion.TareaNoEncontradaException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorTareaRepetitiva {

  private static final String VISTA_TAREAS_REPETITIVAS = "tareas-repetitivas";
  private static final String ATRIBUTO_TAREAS_REPETITIVAS = "tareasRepetitivas";
  private static final String ATRIBUTO_INTEGRANTES = "integrantes";

  private final ServicioTareaRepetitiva servicioTareaRepetitiva;
  private final ServicioRegistroGasto servicioRegistroGasto;

  @Autowired
  public ControladorTareaRepetitiva(
    ServicioTareaRepetitiva servicioTareaRepetitiva,
    ServicioRegistroGasto servicioRegistroGasto
  ) {
    this.servicioTareaRepetitiva = servicioTareaRepetitiva;
    this.servicioRegistroGasto = servicioRegistroGasto;
  }

  @GetMapping("/tareas-repetitivas")
  public ModelAndView irATareasRepetitivas(HttpSession session) {
    Long hogarId = obtenerHogarIdDeSesion(session);
    ModelAndView mav = new ModelAndView(VISTA_TAREAS_REPETITIVAS);
    mav.addObject(ATRIBUTO_TAREAS_REPETITIVAS, servicioTareaRepetitiva.obtenerTodas(hogarId));
    mav.addObject(ATRIBUTO_INTEGRANTES, servicioRegistroGasto.obtenerIntegrantes(hogarId));
    return mav;
  }

  @PostMapping("/tareas-repetitivas/rotar")
  public ModelAndView rotarTarea(@RequestParam("tareaId") Long tareaId, HttpSession session) {
    Long hogarId = (Long) session.getAttribute("HOGAR_ID");
    try {
      servicioTareaRepetitiva.asignarEquitativamente(
        tareaId,
        servicioRegistroGasto.obtenerIntegrantes(hogarId),
        hogarId
      );
      return new ModelAndView("redirect:/tareas-repetitivas");
    } catch (TareaNoEncontradaException e) {
      ModelAndView mav = new ModelAndView(VISTA_TAREAS_REPETITIVAS);
      mav.addObject("mensajeError", "La tarea seleccionada no existe.");
      mav.addObject(ATRIBUTO_TAREAS_REPETITIVAS, servicioTareaRepetitiva.obtenerTodas(hogarId));
      mav.addObject(ATRIBUTO_INTEGRANTES, servicioRegistroGasto.obtenerIntegrantes(hogarId));
      return mav;
    } catch (HogarSinIntegrantesException e) {
      ModelAndView mav = new ModelAndView(VISTA_TAREAS_REPETITIVAS);
      mav.addObject("mensajeError", "El hogar no tiene integrantes para asignar la tarea.");
      mav.addObject(ATRIBUTO_TAREAS_REPETITIVAS, servicioTareaRepetitiva.obtenerTodas(hogarId));
      mav.addObject(ATRIBUTO_INTEGRANTES, servicioRegistroGasto.obtenerIntegrantes(hogarId));
      return mav;
    }
  }

  /*
  @PostMapping("/tareas-repetitivas/quitar")
  public ModelAndView quitarTarea(@RequestParam("tareaId") Long tareaId, HttpSession session) {
    Long hogarId = obtenerHogarIdDeSesion(session);
    Long usuarioId = SessionUtils.obtenerUsuarioIdDeSesion(session);
    try {
      servicioTareaRepetitiva.quitarPlantilla(tareaId, hogarId, usuarioId);
      return new ModelAndView("redirect:/tareas-repetitivas");
    } catch (TareaNoEncontradaException e) {
      ModelAndView mav = new ModelAndView(VISTA_TAREAS_REPETITIVAS);
      mav.addObject("mensajeError", "La tarea seleccionada no existe.");
      mav.addObject(ATRIBUTO_TAREAS_REPETITIVAS, servicioTareaRepetitiva.obtenerTodas(hogarId));
      mav.addObject(ATRIBUTO_INTEGRANTES, servicioRegistroGasto.obtenerIntegrantes(hogarId));
      return mav;
    }
  }  */

  private Long obtenerHogarIdDeSesion(HttpSession session) {
    Long hogarId = (Long) session.getAttribute("HOGAR_ID");
    if (hogarId == null) {
      return 1L;
    }
    return hogarId;
  }
}
