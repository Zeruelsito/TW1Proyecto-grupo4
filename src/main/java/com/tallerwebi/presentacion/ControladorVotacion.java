package com.tallerwebi.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.tallerwebi.dominio.Propuesta;
import com.tallerwebi.dominio.enums.EstadoPropuesta;
import com.tallerwebi.dominio.excepcion.PropuestaNoExiste;
import com.tallerwebi.dominio.excepcion.UsuarioYaVoto;
import com.tallerwebi.dominio.excepcion.VotacionCerradaException;
import com.tallerwebi.dominio.service.ServicioVotacion;

import jakarta.servlet.http.HttpServletRequest;

@Controller 
public class ControladorVotacion {
    private final ServicioVotacion servicioVotacion;

    public ControladorVotacion(ServicioVotacion servicioVotacion) {
        this.servicioVotacion = servicioVotacion;
    }

    @RequestMapping(path = "/propuestas", method = RequestMethod.GET)
  public ModelAndView irAPropuestas(HttpServletRequest request) {
    if (obtenerIdUsuario(request) == null) {
      return new ModelAndView("redirect:/login");
    }
    return armarVista(new DatosPropuesta(), null);
  }

  @RequestMapping(path = "/propuestas/guardar", method = RequestMethod.POST)
  public ModelAndView guardarPropuesta(
    @ModelAttribute("datosPropuesta") DatosPropuesta datos,
    HttpServletRequest request
  ) {
    Long idUsuario = obtenerIdUsuario(request);
    if (idUsuario == null) {
      return new ModelAndView("redirect:/login");
    }

    try {
      servicioVotacion.guardarPropuesta(armarPropuesta(datos, idUsuario));
    } catch (PropuestaNoExiste e) {
      return armarVista(datos, e.getMessage());
    }
    return new ModelAndView("redirect:/propuestas");
  }

  @RequestMapping(path = "/propuestas/{id}/votar", method = RequestMethod.POST)
  public ModelAndView votar(
    @PathVariable("id") Long idPropuesta,
    @RequestParam("esAfirmativo") boolean esAfirmativo,
    @RequestParam(value = "comentario", required = false) String comentario,
    HttpServletRequest request
  ) {
    Long idUsuario = obtenerIdUsuario(request);
    if (idUsuario == null) {
      return new ModelAndView("redirect:/login");
    }

    try {
        servicioVotacion.votar(idUsuario, idPropuesta, esAfirmativo, comentario);
    } catch (PropuestaNoExiste | VotacionCerradaException | UsuarioYaVoto e) {
      return armarVista(new DatosPropuesta(), e.getMessage());
    }
    return new ModelAndView("redirect:/propuestas");
  }

  private Long obtenerIdUsuario(HttpServletRequest request) {
    return (Long) request.getSession().getAttribute("idUsuario");
  }

  private Propuesta armarPropuesta(DatosPropuesta datos, Long idUsuario) {
    double quorum = datos.getPorcentajeQuorum() == null ? 0 : datos.getPorcentajeQuorum();
    return new Propuesta(
      datos.getTitulo(),
      datos.getDescripcion(),
      idUsuario,
      datos.getFechaLimite(),
      quorum,
      EstadoPropuesta.PENDIENTE
    );
  }

  private ModelAndView armarVista(DatosPropuesta datos, String mensajeError) {
    ModelAndView mav = new ModelAndView("propuestas");
    mav.addObject("listaPropuestas", servicioVotacion.obtenerTodasLasPropuestas());
    mav.addObject("datosPropuesta", datos);
    if (mensajeError != null) {
      mav.addObject("mensajeError", mensajeError);
    }
    return mav;
  }
}

