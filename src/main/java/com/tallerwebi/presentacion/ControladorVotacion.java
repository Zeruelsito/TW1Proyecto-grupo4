package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.PropuestaNoExiste;
import com.tallerwebi.dominio.excepcion.UsuarioYaVoto;
import com.tallerwebi.dominio.excepcion.VotacionCerradaException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorVotacion {

  private static final String SESION_USUARIO_ID = "USUARIO_ID";
  private static final String SESION_HOGAR_ID = "HOGAR_ID";
  private static final String REDIRECT_LOGIN = "redirect:/login";
  private static final String REDIRECT_DASHBOARD = "redirect:/dashboard";
  private static final String REDIRECT_PROPUESTAS = "redirect:/propuestas";
  private static final String MENSAJE_SIN_HOGAR =
    "Para ver las propuestas y otras secciones debes pertenecer a un hogar.";

  private final ServicioVotacion servicioVotacion;

  public ControladorVotacion(ServicioVotacion servicioVotacion) {
    this.servicioVotacion = servicioVotacion;
  }

  @RequestMapping(path = "/propuestas", method = RequestMethod.GET)
  public ModelAndView irAPropuestas(HttpSession session, RedirectAttributes redirectAttributes) {
    ModelAndView redireccion = validarSesion(session, redirectAttributes);
    if (redireccion != null) {
      return redireccion;
    }
    return armarVista(new DatosPropuesta(), obtenerHogarId(session), null);
  }

  @RequestMapping(path = "/propuestas/guardar", method = RequestMethod.POST)
  public ModelAndView guardarPropuesta(
    @ModelAttribute("datosPropuesta") DatosPropuesta datos,
    HttpSession session,
    RedirectAttributes redirectAttributes
  ) {
    ModelAndView redireccion = validarSesion(session, redirectAttributes);
    if (redireccion != null) {
      return redireccion;
    }

    Long hogarId = obtenerHogarId(session);
    try {
      servicioVotacion.guardarPropuesta(armarPropuesta(datos, obtenerIdUsuario(session), hogarId));
    } catch (PropuestaNoExiste e) {
      return armarVista(datos, hogarId, e.getMessage());
    }
    return new ModelAndView(REDIRECT_PROPUESTAS);
  }

  @RequestMapping(path = "/propuestas/{id}/votar", method = RequestMethod.POST)
  public ModelAndView votar(
    @PathVariable("id") Long idPropuesta,
    @RequestParam("esAfirmativo") boolean esAfirmativo,
    @RequestParam(value = "comentario", required = false) String comentario,
    HttpSession session,
    RedirectAttributes redirectAttributes
  ) {
    ModelAndView redireccion = validarSesion(session, redirectAttributes);
    if (redireccion != null) {
      return redireccion;
    }

    try {
      servicioVotacion.votar(
        obtenerIdUsuario(session),
        obtenerHogarId(session),
        idPropuesta,
        esAfirmativo,
        comentario
      );
    } catch (PropuestaNoExiste | VotacionCerradaException | UsuarioYaVoto e) {
      redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
    }
    return new ModelAndView(REDIRECT_PROPUESTAS);
  }

  private ModelAndView validarSesion(HttpSession session, RedirectAttributes redirectAttributes) {
    if (obtenerIdUsuario(session) == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    if (obtenerHogarId(session) == null) {
      redirectAttributes.addFlashAttribute("alertaHogar", MENSAJE_SIN_HOGAR);
      return new ModelAndView(REDIRECT_DASHBOARD);
    }
    return null;
  }

  private Long obtenerIdUsuario(HttpSession session) {
    return (Long) session.getAttribute(SESION_USUARIO_ID);
  }

  private Long obtenerHogarId(HttpSession session) {
    return (Long) session.getAttribute(SESION_HOGAR_ID);
  }

  private Propuesta armarPropuesta(DatosPropuesta datos, Long idUsuario, Long hogarId) {
    double quorum = datos.getPorcentajeQuorum() == null ? 0 : datos.getPorcentajeQuorum();
    Propuesta propuesta = new Propuesta(
      datos.getTitulo(),
      datos.getDescripcion(),
      idUsuario,
      datos.getFechaLimite(),
      quorum,
      EstadoPropuesta.PENDIENTE
    );
    propuesta.setHogarId(hogarId);
    return propuesta;
  }

  private ModelAndView armarVista(DatosPropuesta datos, Long hogarId, String mensajeError) {
    ModelAndView mav = new ModelAndView("propuestas");
    mav.addObject("listaPropuestas", servicioVotacion.obtenerPropuestasDelHogar(hogarId));
    mav.addObject("datosPropuesta", datos);
    if (mensajeError != null) {
      mav.addObject("mensajeError", mensajeError);
    }
    return mav;
  }
}
