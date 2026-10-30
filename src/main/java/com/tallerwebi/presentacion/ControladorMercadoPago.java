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

  private final ServicioMercadoPago servicioMercadoPago;

  @Autowired
  private ServicioPagoRealizado servicioPagoRealizado;

  @Autowired
  private ServicioPagoPendiente servicioPagoPendiente;

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
    String titulo = "Transferencia";
    session.setAttribute("emisorPago", emisor);
    session.setAttribute("receptorPago", receptor);
    session.setAttribute("montoPago", monto);

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

  @GetMapping("/pago/succesControladorMP")
  public ModelAndView pagoExitoso(
    @RequestParam(value = "collection_id", required = false) String collectionId,
    @RequestParam(value = "status", required = false) String status
  ) {
    Hogar casa = (Hogar) session.getAttribute(HOGAR_SESSION_KEY);

    String emisor = (String) session.getAttribute("emisorPago");
    String receptor = (String) session.getAttribute("receptorPago");
    Double monto = (Double) session.getAttribute("montoPago");

    if (casa != null && monto != null) {
      PagoRealizado pago = new PagoRealizado(emisor, receptor, monto, LocalDateTime.now(), casa);
      servicioPagoRealizado.registrarPagoExitoso(pago);
    }

    return new ModelAndView("succesControladorMP");
  }

  @GetMapping("/pago/errorControladorMP")
  public ModelAndView errorControladorMP() {
    return new ModelAndView("errorControladorMP");
  }

  // Endpoint único para cuando el pago queda pendiente (Guarda el pendiente y redirige a la liquidación)
  @GetMapping("/pago/pending")
  public ModelAndView pagoPendiente() {
    Hogar casa = (Hogar) session.getAttribute(HOGAR_SESSION_KEY);
    String emisor = (String) session.getAttribute("emisorPago");
    String receptor = (String) session.getAttribute("receptorPago");
    Double monto = (Double) session.getAttribute("montoPago");

    if (casa != null && monto != null) {
      PagoPendiente pendiente = new PagoPendiente(emisor, receptor, monto, casa);
      servicioPagoPendiente.guardarPendiente(pendiente);
    }

    return new ModelAndView("redirect:/liquidacion");
  }

  @PostMapping("/pago/confirmar-manual")
  public ModelAndView confirmarPagoManual(@RequestParam("id") Long id) {
    Hogar casa = (Hogar) session.getAttribute(HOGAR_SESSION_KEY);

    if (casa != null) {
      // 1. Buscamos el pendiente exacto por su ID
      PagoPendiente pendiente = servicioPagoPendiente.buscarPorId(id);

      if (pendiente != null) {
        // 2. Registrar como exitoso en el historial usando sus datos reales
        PagoRealizado pago = new PagoRealizado(
          pendiente.getEmisor(),
          pendiente.getReceptor(),
          pendiente.getMonto(),
          LocalDateTime.now(),
          casa
        );
        servicioPagoRealizado.registrarPagoExitoso(pago);

        // 3. Borrarlo de pendientes por ID
        servicioPagoPendiente.eliminarPorId(id);
      }
    }

    return new ModelAndView("redirect:/liquidacion");
  }

  @PostMapping("/pago/devolver-sugeridas")
  public ModelAndView devolverASugeridas(@RequestParam("id") Long id) {
    Hogar casa = (Hogar) session.getAttribute(HOGAR_SESSION_KEY);

    if (casa != null) {
      // Borra exactamente el registro pendiente seleccionado
      servicioPagoPendiente.eliminarPorId(id);
    }

    return new ModelAndView("redirect:/liquidacion");
  }

  // --- WEBHOOK REFACTORIZADO PARA CUMPLIR CON PMD ---

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
