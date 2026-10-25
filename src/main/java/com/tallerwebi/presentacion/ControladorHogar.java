package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioHogar;
import com.tallerwebi.dominio.excepcion.HogarNoEncontradoException;
import com.tallerwebi.dominio.excepcion.ParametroInvalidoException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorHogar {

  private static final String REDIRECT = "redirect:/hogar";

  private final ServicioHogar servicioHogar;

  @Autowired
  public ControladorHogar(ServicioHogar servicioHogar) {
    this.servicioHogar = servicioHogar;
  }

  @GetMapping("/hogar") // pedir y mostrar
  public ModelAndView irAHogar(HttpSession session) {
    ModelAndView mav = new ModelAndView("hogar");
    Long usuarioId = (Long) session.getAttribute("USUARIO_ID");

    // PARCHE TEMPORAL PARA PRUEBAS (mientras no esté el login conectado):
    if (usuarioId == null) {
      usuarioId = 1L; // Corresponde al usuario 'Juan' con ID 1 en el data.sql
    }

    mav.addObject("hogar", servicioHogar.obtenerHogar());
    mav.addObject("hogares", servicioHogar.obtenerHogares(usuarioId));
    mav.addObject("historial", servicioHogar.obtenerHistorial());
    return mav;
  }

  @PostMapping("/hogar/parametros") //enviar o hacer cambio
  public ModelAndView guardarParametros( //caudno se ejecuta el formulario
    @RequestParam("precioHoraTarea") Double precioHoraTarea,
    @RequestParam("pisoMinimoHogar") Double pisoMinimoHogar
  ) {
    try {
      servicioHogar.actualizarParametros(precioHoraTarea, pisoMinimoHogar);
    } catch (ParametroInvalidoException e) {
      return new ModelAndView(REDIRECT);
    }
    return new ModelAndView(REDIRECT);
  }

  @PostMapping("/hogar/cambiar")
  public ModelAndView cambiarHogar(@RequestParam("hogarId") Long hogarId, HttpSession session) {
    try {
      servicioHogar.seleccionarHogar(hogarId);
      session.setAttribute("HOGAR_ID", hogarId);
    } catch (HogarNoEncontradoException e) {
      return new ModelAndView(REDIRECT);
    }
    return new ModelAndView(REDIRECT);
  }
}
