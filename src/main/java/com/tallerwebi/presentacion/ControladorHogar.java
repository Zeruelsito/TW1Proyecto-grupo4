package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.ServicioHogar;
import com.tallerwebi.dominio.excepcion.HogarNoEncontradoException;
import com.tallerwebi.dominio.excepcion.ParametroInvalidoException;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorHogar {

  private static final String REDIRECT = "redirect:/hogar";

  private final ServicioHogar servicioHogar;

  @Autowired
  public ControladorHogar(ServicioHogar servicioHogar) {
    this.servicioHogar = servicioHogar;
  }

  @GetMapping("/hogar") // pedir y mostrar
  public ModelAndView irAHogar(HttpSession session, RedirectAttributes redirectAttributes) {
    ModelAndView mav = new ModelAndView("hogar");
    Long usuarioId = (Long) session.getAttribute("USUARIO_ID");
    Long hogarId = (Long) session.getAttribute("HOGAR_ID");

    if (hogarId == null) {
      redirectAttributes.addFlashAttribute(
        "alertaHogar",
        "Para ver el hogar y otras secciones debes pertenecer a un hogar."
      );
      return new ModelAndView("redirect:/dashboard");
    }

    Hogar hogarActivo = (Hogar) session.getAttribute("hogarActivoEnSession");

    if (hogarActivo == null) {
      List<Hogar> listaHogares = servicioHogar.obtenerHogares(usuarioId);
      if (listaHogares != null && !listaHogares.isEmpty()) {
        hogarActivo = listaHogares.get(0);
        session.setAttribute("hogarActivoEnSession", hogarActivo);
      }
    }

    mav.addObject("hogar", hogarActivo); // <-- Pasamos el hogar activo correcto a la vista
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
      servicioHogar.seleccionarHogarPorId(hogarId);
      session.setAttribute("HOGAR_ID", hogarId);
      session.setAttribute("hogarActivoEnSession", servicioHogar.seleccionarHogarPorId(hogarId));
    } catch (HogarNoEncontradoException e) {
      return new ModelAndView(REDIRECT);
    }
    return new ModelAndView(REDIRECT);
  }
}
