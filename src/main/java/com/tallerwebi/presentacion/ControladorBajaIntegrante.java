package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioBajaIntegrante;
import com.tallerwebi.dominio.SimulacionBaja;
import com.tallerwebi.dominio.excepcion.HogarSinIntegrantesException;
import com.tallerwebi.dominio.excepcion.IntegranteConSaldoPendienteException;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.excepcion.UsuarioYaDadoDeBajaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorBajaIntegrante {

  private static final String VISTA = "baja-integrante";

  private final ServicioBajaIntegrante servicioBajaIntegrante;

  @Autowired
  public ControladorBajaIntegrante(ServicioBajaIntegrante servicioBajaIntegrante) {
    this.servicioBajaIntegrante = servicioBajaIntegrante;
  }

  @GetMapping("/hogar/baja")
  public ModelAndView irABaja(
    @RequestParam(value = "confirmada", required = false) Boolean confirmada
  ) {
    ModelAndView mav = vistaBase();
    if (Boolean.TRUE.equals(confirmada)) {
      mav.addObject("mensaje", "La baja se registró y las cuotas se recalcularon.");
    }
    return mav;
  }

  @GetMapping("/hogar/baja/simular")
  public ModelAndView simular(@RequestParam("usuarioId") Long usuarioId) {
    ModelAndView mav = vistaBase();
    try {
      SimulacionBaja simulacion = servicioBajaIntegrante.simularBaja(usuarioId);
      mav.addObject("simulacion", simulacion);
      mav.addObject("usuarioIdSeleccionado", usuarioId);
    } catch (RuntimeException excepcion) {
      mav.addObject("error", mensajeDeError(excepcion));
    }
    return mav;
  }

  @PostMapping("/hogar/baja/confirmar")
  public ModelAndView confirmar(@RequestParam("usuarioId") Long usuarioId) {
    try {
      servicioBajaIntegrante.confirmarBaja(usuarioId);
    } catch (RuntimeException excepcion) {
      return simulacionConError(usuarioId, mensajeDeError(excepcion));
    }
    return new ModelAndView("redirect:/hogar/baja?confirmada=true");
  }

  /** Vuelve a mostrar la simulación del integrante (si se puede) junto con el mensaje de error. */
  private ModelAndView simulacionConError(Long usuarioId, String error) {
    ModelAndView mav = simular(usuarioId);
    mav.addObject("error", error);
    return mav;
  }

  private ModelAndView vistaBase() {
    ModelAndView mav = new ModelAndView(VISTA);
    mav.addObject("integrantes", servicioBajaIntegrante.obtenerIntegrantesActivos());
    return mav;
  }

  private String mensajeDeError(RuntimeException excepcion) {
    if (excepcion instanceof UsuarioNoEncontrado) {
      return "El integrante seleccionado no existe.";
    }
    if (excepcion instanceof UsuarioYaDadoDeBajaException) {
      return "Ese integrante ya fue dado de baja.";
    }
    if (excepcion instanceof HogarSinIntegrantesException) {
      return "No se puede dar de baja al último integrante del hogar.";
    }
    if (excepcion instanceof IntegranteConSaldoPendienteException) {
      return "No se puede dar de baja: el integrante todavía tiene saldo pendiente.";
    }
    throw excepcion;
  }
}
