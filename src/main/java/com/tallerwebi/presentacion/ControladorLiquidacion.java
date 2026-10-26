package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorLiquidacion {

  private final ServicioLiquidacion servicioLiquidacion;
  private final ServicioRegistroGasto servicioRegistroGasto; // <-- Inyectamos el servicio

  @Autowired
  public ControladorLiquidacion(
    ServicioLiquidacion servicioLiquidacion,
    ServicioRegistroGasto servicioRegistroGasto
  ) {
    this.servicioLiquidacion = servicioLiquidacion;
    this.servicioRegistroGasto = servicioRegistroGasto;
  }

  @GetMapping("/liquidacion")
  public ModelAndView irALiquidacion(HttpSession session) {
    Map<String, Object> modelo = new HashMap<>();

    Hogar hogar = new Hogar("Hogar Compartido");
    Long hogarId = obtenerHogarIdDeSesion(session);

    // Obtenemos los integrantes reales desde la base de datos a través del servicio
    List<Usuario> integrantes = servicioRegistroGasto.obtenerIntegrantes(hogarId);
    hogar.setIntegrantes(integrantes);

    // Obtenemos los gastos reales persistidos en la base de datos
    List<Gasto> gastos = servicioRegistroGasto.obtenerTodosLosGastos(hogarId);

    // Calculamos las transferencias con los datos reales
    List<TransferenciaSugerida> transferencias = servicioLiquidacion.obtenerLiquidacionDelMes(
      hogar,
      gastos
    );

    modelo.put("transferencias", transferencias);
    modelo.put("hogar", hogar);
    modelo.put("usuarioLogueado", "Juan"); // (O lo podés dejar dinámico si ya manejás sesión)

    return new ModelAndView("liquidacion", modelo);
  }

  private Long obtenerHogarIdDeSesion(HttpSession session) {
    Long hogarId = (Long) session.getAttribute("HOGAR_ID");
    // ID de contingencia por si no hay sesión activa aún en desarrollo (ej. 1L)
    return hogarId != null ? hogarId : 1L;
  }
}
