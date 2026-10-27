package com.tallerwebi.presentacion;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.tallerwebi.dominio.*;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorLiquidacion {

  private final ServicioLiquidacion servicioLiquidacion;
  private final ServicioRegistroGasto servicioRegistroGasto;
  private final ServicioHogar servicioHogar; // <-- Inyectamos el servicio
  private final ServicioUsuario servicioUsuario;

  @Autowired
  public ControladorLiquidacion(
    ServicioLiquidacion servicioLiquidacion,
    ServicioRegistroGasto servicioRegistroGasto,
    ServicioHogar servicioHogar,
    ServicioUsuario servicioUsuario
  ) {
    this.servicioLiquidacion = servicioLiquidacion;
    this.servicioRegistroGasto = servicioRegistroGasto;
    this.servicioHogar = servicioHogar;
    this.servicioUsuario = servicioUsuario;
  }

  @GetMapping("/liquidacion")
  public ModelAndView irALiquidacion(
    @RequestParam(value = "nombreHogar", required = false) String nombreHogar,
    HttpSession session
  ) {
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
      }
    }

    return new ModelAndView("liquidacion", modelo);
  }

  // Genera la URL de Mercado Pago y recarga la vista con los datos del hogar
  // que vaya a otro html que simule una pasarela de pago para que se vea
  @PostMapping("/procesar-liquidacion")
  public String procesarPagoLiquidacion(
    //    @RequestParam("nombreHogar") String nombreHogar,
    //    @RequestParam("emisor") String emisor,
    //    @RequestParam("receptor") String receptor,
    //    @RequestParam("monto") Double monto
  ) throws MPException, MPApiException {
    //    Map<String, Object> model = new HashMap<>();
    //
    //    List<Hogar> listaHogares = servicioHogar.obtenerHogares();
    //    model.put("listaHogares", listaHogares);
    //
    //    Hogar casa = servicioHogar.obtenerHogarPorNombre(nombreHogar);
    //    List<Gasto> gastos = servicioRegistroGasto.obtenerGastosPorHogar(casa);
    //    List<TransferenciaSugerida> transferencias = servicioLiquidacion.obtenerLiquidacionDelMes(
    //      casa,
    //      gastos
    //    );
    //
    //    model.put("casa", casa);
    //    model.put("transferencias", transferencias);
    //
    //    String concepto = "Pago de " + emisor + " a " + receptor + " (" + casa.getNombreHogar() + ")";
    //    String sandboxUrl = mercadoPagoService.crearUrl(concepto, monto);
    //    model.put("urlPago", sandboxUrl);
    //
    //    return new ModelAndView("liquidaciones", model);
    return "redirect:/procesarPago";
  }

  @PostMapping("/procesarPago")
  public ModelAndView mostrarPasarela(
    @RequestParam("emisor") String emisor,
    @RequestParam("receptor") String receptor,
    @RequestParam("monto") Double monto
  ) {
    ModelAndView modelAndView = new ModelAndView("procesarPago"); // Nombre de tu archivo .html
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

  @PostMapping("/simular-error")
  public ModelAndView simularErrorPago() {
    return new ModelAndView("errorControladorLiquidacion");
  }
}
