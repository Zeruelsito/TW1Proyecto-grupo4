package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioBajaIntegrante;
import com.tallerwebi.dominio.SimulacionBaja;
import com.tallerwebi.dominio.excepcion.HogarSinIntegrantesException;
import com.tallerwebi.dominio.excepcion.IntegranteConSaldoPendienteException;
import com.tallerwebi.dominio.excepcion.PagoNoCorrespondeALiquidacionException;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.excepcion.UsuarioYaDadoDeBajaException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    @RequestParam(value = "confirmada", required = false) Boolean confirmada,
    HttpSession session
  ) {
    ModelAndView mav = vistaBase(session);
    if (Boolean.TRUE.equals(confirmada)) {
      mav.addObject("mensaje", "La baja se registró y las cuotas se recalcularon.");
    }
    return mav;
  }

  @GetMapping("/hogar/baja/simular")
  public ModelAndView simular(
    @RequestParam("usuarioId") Long usuarioId,
    @RequestParam(value = "pagoRegistrado", required = false) Boolean pagoRegistrado,
    @RequestParam(value = "pagoPendiente", required = false) Boolean pagoPendiente,
    HttpSession session
  ) {
    ModelAndView mav = vistaBase(session);
    if (Boolean.TRUE.equals(pagoRegistrado)) {
      mav.addObject("mensaje", "El pago se registró y la liquidación final se actualizó.");
    }
    if (Boolean.TRUE.equals(pagoPendiente)) {
      mav.addObject(
        "mensaje",
        "El pago quedó registrado como pendiente de confirmación (Pago Fácil / Rapipago)."
      );
    }
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
  public ModelAndView confirmar(
    @RequestParam("usuarioId") Long usuarioId,
    HttpSession session,
    RedirectAttributes redirectAttributes
  ) {
    try {
      servicioBajaIntegrante.confirmarBaja(usuarioId);
    } catch (RuntimeException excepcion) {
      return simulacionConError(usuarioId, mensajeDeError(excepcion), session);
    }
    redirectAttributes.addFlashAttribute(
      "mensajeExitoBaja",
      "Saliste del hogar exitosamente. Tus cuentas quedaron saldadas y el hogar fue actualizado."
    );
    return new ModelAndView("redirect:/dashboard");
  }

  private ModelAndView simulacionConError(Long usuarioId, String error, HttpSession session) {
    ModelAndView mav = simular(usuarioId, null, null, session);
    mav.addObject("error", error);
    return mav;
  }

  private ModelAndView vistaBase(HttpSession session) {
    ModelAndView mav = new ModelAndView(VISTA);
    mav.addObject("integrantes", servicioBajaIntegrante.obtenerIntegrantesActivos());
    Long usuarioLogueadoId = obtenerUsuarioId(session);
    mav.addObject("usuarioLogueadoId", usuarioLogueadoId);
    return mav;
  }

  private Long obtenerUsuarioId(HttpSession session) {
    if (session == null) {
      return null;
    }
    Long id = SessionUtils.obtenerUsuarioIdDeSesion(session);
    if (id != null) {
      return id;
    }
    Object attr = session.getAttribute("USUARIO_ID");
    return attr instanceof Long usuarioIdAttr ? usuarioIdAttr : null;
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
    if (excepcion instanceof PagoNoCorrespondeALiquidacionException) {
      return "Ese pago no corresponde a la liquidación pendiente o ya fue registrado.";
    }
    throw excepcion;
  }
}
