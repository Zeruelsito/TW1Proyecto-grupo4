package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioRegistroGasto;
import com.tallerwebi.dominio.ServicioTareaRepetitiva;
import com.tallerwebi.dominio.excepcion.HogarSinIntegrantesException;
import com.tallerwebi.dominio.excepcion.TareaNoEncontradaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorTareaRepetitiva {

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
  public ModelAndView irATareasRepetitivas() {
    ModelAndView mav = new ModelAndView("tareas-repetitivas");
    mav.addObject("tareasRepetitivas", servicioTareaRepetitiva.obtenerTodas());
    mav.addObject("integrantes", servicioRegistroGasto.obtenerIntegrantes());
    return mav;
  }

  @PostMapping("/tareas-repetitivas/rotar")
  public ModelAndView rotarTarea(@RequestParam("tareaId") Long tareaId) {
    try {
      servicioTareaRepetitiva.asignarEquitativamente(
        tareaId,
        servicioRegistroGasto.obtenerIntegrantes()
      );
      return new ModelAndView("redirect:/tareas-repetitivas");
    } catch (TareaNoEncontradaException e) {
      ModelAndView mav = new ModelAndView("tareas-repetitivas");
      mav.addObject("mensajeError", "La tarea seleccionada no existe.");
      mav.addObject("tareasRepetitivas", servicioTareaRepetitiva.obtenerTodas());
      mav.addObject("integrantes", servicioRegistroGasto.obtenerIntegrantes());
      return mav;
    } catch (HogarSinIntegrantesException e) {
      ModelAndView mav = new ModelAndView("tareas-repetitivas");
      mav.addObject("mensajeError", "El hogar no tiene integrantes para asignar la tarea.");
      mav.addObject("tareasRepetitivas", servicioTareaRepetitiva.obtenerTodas());
      mav.addObject("integrantes", servicioRegistroGasto.obtenerIntegrantes());
      return mav;
    }
  }
}
