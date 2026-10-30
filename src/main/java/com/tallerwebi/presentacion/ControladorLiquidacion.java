package com.tallerwebi.presentacion;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.tallerwebi.dominio.*;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;
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
  private final ServicioPagoPendiente servicioPagoPendiente;
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
    ServicioPagoRealizado servicioPagoRealizado,
    ServicioPagoPendiente servicioPagoPendiente
  ) {
    this.session = session;
    this.servicioLiquidacion = servicioLiquidacion;
    this.servicioRegistroGasto = servicioRegistroGasto;
    this.servicioHogar = servicioHogar;
    this.servicioUsuario = servicioUsuario;
    this.servicioPagoRealizado = servicioPagoRealizado;
    this.servicioPagoPendiente = servicioPagoPendiente;
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
        List<PagoPendiente> transferenciasPendientes = servicioPagoPendiente.obtenerPendientes(
          hogar
        );
        List<TransferenciaSugerida> transferencias = servicioLiquidacion.obtenerLiquidacionDelMes(
          hogar,
          gastos
        );
        modelo.put("transferenciasPendientes", transferenciasPendientes);
        Set<String> clavesPendientes = new HashSet<>();
        for (PagoPendiente p : transferenciasPendientes) {
          String clave = p.getEmisor() + "_" + p.getReceptor() + "_" + p.getMonto();
          clavesPendientes.add(clave);
        }
        modelo.put("clavesPendientes", clavesPendientes);
        if (transferencias != null) {
          modelo.put("transferencias", transferencias);
        }

        List<PagoRealizado> historialPagos = servicioPagoRealizado.obtenerHistorialPagos(
          hogar.getId()
        );
        modelo.put("historialPagos", historialPagos);
      }
    }

    return new ModelAndView("liquidacion", modelo);
  }
}
