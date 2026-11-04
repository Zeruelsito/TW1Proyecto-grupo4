package com.tallerwebi.presentacion;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.tallerwebi.dominio.*;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorMercadoPago {

  private static final Logger logger = LoggerFactory.getLogger(ControladorMercadoPago.class);

  // Constantes para cumplir con las reglas de PMD (Evitar literales repetidos)
  private static final String HOGAR_SESSION_KEY = "hogarActivoEnSession";
  private static final String TIPO_PAYMENT = "payment";
  private static final String STATUS_APPROVED = "approved";
  private static final String STATUS_REJECTED = "rejected";
  private static final String STATUS_PENDING = "pending";

  private static final String BAJA_USUARIO_SESSION_KEY = "bajaUsuarioIdPago";
  private static final String REDIRECT_SIMULAR_BAJA = "redirect:/hogar/baja/simular?usuarioId=";

  // Constantes de sesión para los pagos (Soluciona el aviso de AvoidDuplicateLiterals)
  private static final String SESSION_EMISOR_PAGO = "emisorPago";
  private static final String SESSION_RECEPTOR_PAGO = "receptorPago";
  private static final String SESSION_MONTO_PAGO = "montoPago";

  private final ServicioMercadoPago servicioMercadoPago;

  @Autowired
  private ServicioPagoRealizado servicioPagoRealizado;

  @Autowired
  private ServicioPagoPendiente servicioPagoPendiente;

  @Autowired
  private ServicioBajaIntegrante servicioBajaIntegrante;

  @Autowired
  private HttpSession session;

  public ControladorMercadoPago(ServicioMercadoPago servicioMercadoPago) {
    this.servicioMercadoPago = servicioMercadoPago;
  }

  // 1. Endpoint para iniciar el pago
  @PostMapping("/pago")
  public ModelAndView pago(
    @RequestParam("emisor") String emisor,
    @RequestParam("receptor") String receptor,
    @RequestParam("monto") Double monto
  ) {
    session.removeAttribute(BAJA_USUARIO_SESSION_KEY);
    String titulo = "Transferencia";
    session.setAttribute(SESSION_EMISOR_PAGO, emisor);
    session.setAttribute(SESSION_RECEPTOR_PAGO, receptor);
    session.setAttribute(SESSION_MONTO_PAGO, monto);

    try {
      Preference preference = servicioMercadoPago.crearUrl(titulo, BigDecimal.valueOf(monto));

      return new ModelAndView("redirect:" + preference.getSandboxInitPoint());
    } catch (MPApiException e) {
      if (logger.isErrorEnabled()) {
        String errorContent = (e.getApiResponse() != null)
          ? e.getApiResponse().getContent()
          : "Sin contenido de respuesta";
        logger.error("Error de API de Mercado Pago: {}", errorContent, e);
      }
      Map<String, Object> modelo = new HashMap<>();
      modelo.put("error", "Error al conectar con Mercado Pago: " + e.getMessage());
      return new ModelAndView("errorControladorMP", modelo);
    } catch (MPException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error general de Mercado Pago", e);
      }
      Map<String, Object> modelo = new HashMap<>();
      modelo.put("error", "Error al conectar con Mercado Pago: " + e.getMessage());
      return new ModelAndView("errorControladorMP", modelo);
    }
  }

  // 1.b Endpoint para pagar con Mercado Pago la liquidación final de una baja
  @PostMapping("/pago/baja")
  public ModelAndView pagoBaja(
    @RequestParam("usuarioId") Long usuarioId,
    @RequestParam("emisor") String emisorNombre,
    @RequestParam("receptor") String receptorNombre,
    @RequestParam("monto") Double monto
  ) {
    try {
      Hogar casa = (Hogar) session.getAttribute(HOGAR_SESSION_KEY);
      Usuario emisor = buscarUsuarioEnCasa(casa, emisorNombre);
      Usuario receptor = buscarUsuarioEnCasa(casa, receptorNombre);

      SimulacionBaja simulacion = servicioBajaIntegrante.simularBaja(usuarioId);

      // Ahora le pasamos los objetos Usuario reales al método
      if (!simulacion.tieneTransferenciaPendiente(emisor, receptor, monto)) {
        return new ModelAndView(REDIRECT_SIMULAR_BAJA + usuarioId);
      }
    } catch (RuntimeException e) {
      logger.warn("No se pudo validar el pago de la baja del usuario {}", usuarioId, e);
      return new ModelAndView(REDIRECT_SIMULAR_BAJA + usuarioId);
    }

    ModelAndView mav = pago(emisorNombre, receptorNombre, monto);
    session.setAttribute(BAJA_USUARIO_SESSION_KEY, usuarioId);
    return mav;
  }

  @GetMapping("/pago/succesControladorMP")
  public ModelAndView pagoExitoso(
    @RequestParam(value = "collection_id", required = false) String collectionId,
    @RequestParam(value = "status", required = false) String status
  ) {
    Long bajaUsuarioId = (Long) session.getAttribute(BAJA_USUARIO_SESSION_KEY);

    if (bajaUsuarioId != null) {
      return procesarPagoBaja(bajaUsuarioId, status);
    }

    procesarPagoHogarNormal();
    return new ModelAndView("succesControladorMP");
  }

  private ModelAndView procesarPagoBaja(Long bajaUsuarioId, String status) {
    session.removeAttribute(BAJA_USUARIO_SESSION_KEY);
    Double monto = (Double) session.getAttribute(SESSION_MONTO_PAGO);

    if (STATUS_APPROVED.equals(status) && monto != null) {
      Hogar casa = (Hogar) session.getAttribute(HOGAR_SESSION_KEY);
      Usuario emisor = buscarUsuarioEnCasa(
        casa,
        (String) session.getAttribute(SESSION_EMISOR_PAGO)
      );
      Usuario receptor = buscarUsuarioEnCasa(
        casa,
        (String) session.getAttribute(SESSION_RECEPTOR_PAGO)
      );

      servicioBajaIntegrante.registrarPago(bajaUsuarioId, emisor, receptor, monto);
      return new ModelAndView(REDIRECT_SIMULAR_BAJA + bajaUsuarioId + "&pagoRegistrado=true");
    }
    return new ModelAndView(REDIRECT_SIMULAR_BAJA + bajaUsuarioId);
  }

  private void procesarPagoHogarNormal() {
    Hogar casa = (Hogar) session.getAttribute(HOGAR_SESSION_KEY);
    Double monto = (Double) session.getAttribute(SESSION_MONTO_PAGO);

    if (casa != null && monto != null) {
      Usuario emisor = buscarUsuarioEnCasa(
        casa,
        (String) session.getAttribute(SESSION_EMISOR_PAGO)
      );
      Usuario receptor = buscarUsuarioEnCasa(
        casa,
        (String) session.getAttribute(SESSION_RECEPTOR_PAGO)
      );

      if (emisor != null && receptor != null) {
        PagoRealizado pago = new PagoRealizado(emisor, receptor, monto, LocalDateTime.now(), casa);
        servicioPagoRealizado.registrarPagoExitoso(pago);
      }
    }
  }

  private Usuario buscarUsuarioEnCasa(Hogar casa, String nombreUsuario) {
    if (casa == null || casa.getIntegrantes() == null || nombreUsuario == null) {
      return null;
    }
    return casa
      .getIntegrantes()
      .stream()
      .filter(u -> nombreUsuario.equals(u.getNombre()))
      .findFirst()
      .orElse(null);
  }

  @GetMapping("/pago/errorControladorMP")
  public ModelAndView errorControladorMP() {
    return new ModelAndView("errorControladorMP");
  }

  @GetMapping("/pago/pending")
  public ModelAndView pagoPendiente() {
    Hogar casa = (Hogar) session.getAttribute(HOGAR_SESSION_KEY);
    String nombreEmisor = (String) session.getAttribute(SESSION_EMISOR_PAGO);
    String nombreReceptor = (String) session.getAttribute(SESSION_RECEPTOR_PAGO);
    Double monto = (Double) session.getAttribute(SESSION_MONTO_PAGO);

    if (casa != null && monto != null && nombreEmisor != null && nombreReceptor != null) {
      Usuario emisor = buscarUsuarioEnCasa(casa, nombreEmisor);
      Usuario receptor = buscarUsuarioEnCasa(casa, nombreReceptor);

      if (emisor != null && receptor != null) {
        PagoPendiente pendiente = new PagoPendiente(emisor, receptor, monto, casa);
        servicioPagoPendiente.guardarPendiente(pendiente);
      }
    }

    return new ModelAndView("redirect:/liquidacion");
  }

  @PostMapping("/pago/confirmar-manual")
  public ModelAndView confirmarPagoManual(@RequestParam("id") Long id) {
    Hogar casa = (Hogar) session.getAttribute(HOGAR_SESSION_KEY);

    if (casa != null) {
      PagoPendiente pendiente = servicioPagoPendiente.buscarPorId(id);

      if (pendiente != null) {
        PagoRealizado pago = new PagoRealizado(
          pendiente.getEmisor(),
          pendiente.getReceptor(),
          pendiente.getMonto(),
          LocalDateTime.now(),
          casa
        );
        servicioPagoRealizado.registrarPagoExitoso(pago);
        servicioPagoPendiente.eliminarPorId(id);
      }
    }

    return new ModelAndView("redirect:/liquidacion");
  }

  @PostMapping("/pago/devolver-sugeridas")
  public ModelAndView devolverASugeridas(@RequestParam("id") Long id) {
    Hogar casa = (Hogar) session.getAttribute(HOGAR_SESSION_KEY);

    if (casa != null) {
      servicioPagoPendiente.eliminarPorId(id);
    }

    return new ModelAndView("redirect:/liquidacion");
  }

  @PostMapping("/webhook/mercadopago")
  public ResponseEntity<String> recibirWebhook(@RequestBody Map<String, Object> payload) {
    try {
      if (esNotificacionDePago(payload)) {
        Long paymentId = extraerIdDePago(payload);
        if (paymentId != null) {
          procesarEstadoPago(paymentId);
        }
      }
      return ResponseEntity.ok("OK");
    } catch (Exception e) {
      logger.error("Error procesando webhook de Mercado Pago", e);
      return ResponseEntity.internalServerError().body("Error");
    }
  }

  private boolean esNotificacionDePago(Map<String, Object> payload) {
    String type = (String) payload.get("type");
    String action = (String) payload.get("action");
    return TIPO_PAYMENT.equals(type) || (action != null && action.startsWith(TIPO_PAYMENT));
  }

  private Long extraerIdDePago(Map<String, Object> payload) {
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) payload.get("data");
    if (data != null && data.containsKey("id")) {
      return Long.valueOf(String.valueOf(data.get("id")));
    }
    return null;
  }

  private void procesarEstadoPago(Long paymentId) throws Exception {
    PaymentClient paymentClient = new PaymentClient();
    Payment mpPayment = paymentClient.get(paymentId);
    String status = mpPayment.getStatus();

    if (STATUS_APPROVED.equals(status)) {
      logger.info("El pago {} fue APROBADO oficialmente por webhook.", paymentId);
    } else if (STATUS_REJECTED.equals(status)) {
      logger.warn("El pago {} fue RECHAZADO.", paymentId);
    } else if (STATUS_PENDING.equals(status)) {
      logger.info("El pago {} sigue PENDIENTE.", paymentId);
    }
  }
}
