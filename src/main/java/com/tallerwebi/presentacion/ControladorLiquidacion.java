package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorLiquidacion {

  private final ServicioLiquidacion servicioLiquidacion;
  private final ServicioRegistroGasto servicioRegistroGasto;
  private final ServicioPagoPendiente servicioPagoPendiente;
  private final ServicioHogar servicioHogar;
  private final ServicioUsuario servicioUsuario;
  private final ServicioPagoRealizado servicioPagoRealizado;

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
    this.servicioLiquidacion = servicioLiquidacion;
    this.servicioRegistroGasto = servicioRegistroGasto;
    this.servicioHogar = servicioHogar;
    this.servicioUsuario = servicioUsuario;
    this.servicioPagoRealizado = servicioPagoRealizado;
    this.servicioPagoPendiente = servicioPagoPendiente;
  }

  @GetMapping("/liquidacion")
  public ModelAndView irALiquidacion(HttpSession session, RedirectAttributes redirectAttributes) {
    Long hogarId = (Long) session.getAttribute("HOGAR_ID");
    if (hogarId == null) {
      redirectAttributes.addFlashAttribute(
        "alertaHogar",
        "Para ver la liquidación y otras secciones debes pertenecer a un hogar."
      );
      return new ModelAndView("redirect:/dashboard");
    }

    Map<String, Object> modelo = new HashMap<>();
    Long usuarioId = SessionUtils.obtenerUsuarioIdDeSesion(session);
    modelo.put("listaHogares", servicioHogar.obtenerHogares(usuarioId));

    Hogar hogar = resolverHogarAConsultar(session, hogarId);
    if (hogar != null) {
      cargarDatosLiquidacionEnModelo(hogar, modelo);
    }

    return new ModelAndView("liquidacion", modelo);
  }

  private Hogar resolverHogarAConsultar(HttpSession session, Long hogarIdFallback) {
    Hogar hogarSesion = (Hogar) session.getAttribute("hogarActivoEnSession");
    Long idHogarConsultar = hogarSesion != null && hogarSesion.getId() != null
      ? hogarSesion.getId()
      : hogarIdFallback;
    return servicioHogar.seleccionarHogarPorId(idHogarConsultar);
  }

  private void cargarDatosLiquidacionEnModelo(Hogar hogar, Map<String, Object> modelo) {
    modelo.put("casa", hogar);
    List<Gasto> gastos = servicioRegistroGasto.obtenerTodosLosGastos(hogar.getId());
    List<PagoPendiente> transferenciasPendientes = servicioPagoPendiente.obtenerPendientes(hogar);
    List<TransferenciaSugerida> transferencias = servicioLiquidacion.obtenerLiquidacionDelMes(
      hogar,
      gastos
    );

    modelo.put("transferenciasPendientes", transferenciasPendientes);
    modelo.put("clavesPendientes", construirClavesPendientes(transferenciasPendientes));
    if (transferencias != null) {
      modelo.put("transferencias", transferencias);
    }

    List<PagoRealizado> historialPagos = servicioPagoRealizado.obtenerHistorialPagos(hogar.getId());
    modelo.put("historialPagos", historialPagos);
  }

  private Set<String> construirClavesPendientes(List<PagoPendiente> pendientes) {
    Set<String> claves = new HashSet<>();
    for (PagoPendiente p : pendientes) {
      if (p.getEmisor() != null && p.getReceptor() != null && p.getMonto() != null) {
        claves.add(
          p.getEmisor().getNombre() + "_" + p.getReceptor().getNombre() + "_" + p.getMonto()
        );
      }
    }
    return claves;
  }
}
