package com.tallerwebi.presentacion;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.tallerwebi.dominio.*;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorLiquidacion {

  private HttpSession session;

  private final ServicioLiquidacion servicioLiquidacion;
  private final ServicioRegistroGasto servicioRegistroGasto;
  private final ServicioHogar servicioHogar;
  private final ServicioUsuario servicioUsuario;
  private final ServicioPagoRealizado servicioPagoRealizado; // <-- NUEVO: Servicio inyectado

  @Autowired
  public ControladorLiquidacion(
    HttpSession session,
    ServicioLiquidacion servicioLiquidacion,
    ServicioRegistroGasto servicioRegistroGasto,
    ServicioHogar servicioHogar,
    ServicioUsuario servicioUsuario,
    ServicioPagoRealizado servicioPagoRealizado
  ) {
    this.session = session;
    this.servicioLiquidacion = servicioLiquidacion;
    this.servicioRegistroGasto = servicioRegistroGasto;
    this.servicioHogar = servicioHogar;
    this.servicioUsuario = servicioUsuario;
    this.servicioPagoRealizado = servicioPagoRealizado;
  }

  @GetMapping("/liquidacion")
  public ModelAndView irALiquidacion(HttpSession session) {
    Map<String, Object> modelo = new HashMap<>();
    Long usuarioId = SessionUtils.obtenerUsuarioIdDeSesion(session);
    Usuario usuarioActual = servicioUsuario.obtenerUsuarioPorId(usuarioId);

    String nombreUser = (usuarioActual != null && usuarioActual.getNombre() != null)
      ? usuarioActual.getNombre()
      : "Usuario";
    modelo.put("usuarioLogueado", nombreUser);

    List<Hogar> listaHogares = servicioHogar.obtenerHogares(usuarioId);
    modelo.put("listaHogares", listaHogares);

    Hogar hogarSesion = (Hogar) session.getAttribute("hogarActivoEnSession");

    if (hogarSesion != null && hogarSesion.getId() != null) {
      Hogar hogar = servicioHogar.seleccionarHogarPorId(hogarSesion.getId());

      if (hogar != null) {
        modelo.put("casa", hogar);
        List<Gasto> gastos = servicioRegistroGasto.obtenerTodosLosGastos(hogar.getId());
        List<TransferenciaSugerida> transferencias = servicioLiquidacion.obtenerLiquidacionDelMes(
          hogar,
          gastos
        );

        if (transferencias != null) {
          modelo.put("transferencias", transferencias);
        }

        // NUEVO: Obtenemos y pasamos el historial de pagos a la vista de liquidación
        List<PagoRealizado> historialPagos = servicioPagoRealizado.obtenerHistorialPagos(
          hogar.getId()
        );
        modelo.put("historialPagos", historialPagos);
      }
    }

    return new ModelAndView("liquidacion", modelo);
  }

  @PostMapping("/procesar-liquidacion")
  public String procesarPagoLiquidacion() throws MPException, MPApiException {
    return "redirect:/procesarPago";
  }

  @PostMapping("/procesarPago")
  public ModelAndView mostrarPasarela(
    @RequestParam("nombreHogar") String nombreHogar,
    @RequestParam("emisor") String emisor,
    @RequestParam("receptor") String receptor,
    @RequestParam("monto") Double monto
  ) {
    ModelAndView modelAndView = new ModelAndView("procesarPago");
    modelAndView.addObject("nombreHogar", nombreHogar);
    modelAndView.addObject("emisor", emisor);
    modelAndView.addObject("receptor", receptor);
    modelAndView.addObject("monto", monto);
    return modelAndView;
  }

  @GetMapping("/exitoso")
  public String pagoExitoso(
    @RequestParam(value = "payment_id", required = false) String paymentId,
    Model model
  ) {
    model.addAttribute("paymentId", paymentId);
    return "pago-exitoso";
  }

  @PostMapping("/simular-exito")
  public ModelAndView simularExito(
    @RequestParam("nombreHogar") String nombreHogar,
    @RequestParam("emisor") String emisor,
    @RequestParam("receptor") String receptor,
    @RequestParam("monto") Double monto
  ) {
    // NUEVO: Buscamos el hogar y guardamos el pago en la base de datos
    Hogar casa = (Hogar) session.getAttribute("hogarActivoEnSession");
    if (casa != null) {
      PagoRealizado pago = new PagoRealizado(emisor, receptor, monto, LocalDateTime.now(), casa);
      servicioPagoRealizado.registrarPagoExitoso(pago);
    }

    // Redirigimos de vuelta a la liquidación para que la tabla se actualice sola
    return new ModelAndView("redirect:/liquidacion");
  }

  @PostMapping("/simular-error")
  public ModelAndView simularErrorPago() {
    return new ModelAndView("errorControladorLiquidacion");
  }
}
