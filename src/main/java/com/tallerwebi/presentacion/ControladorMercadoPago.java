package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioMercadoPago;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pago")
public class ControladorMercadoPago {

  private final ServicioMercadoPago servicioMercadoPago;

  @Autowired
  public ControladorMercadoPago(ServicioMercadoPago servicioMercadoPago) {
    this.servicioMercadoPago = servicioMercadoPago;
  }

  @PostMapping("/crear")
  public ResponseEntity<String> crearPreferencia(
    @RequestParam("titulo") String titulo,
    @RequestParam("precio") BigDecimal precio
  ) {
    String initPoint = servicioMercadoPago.crearPreferencia(titulo, precio);
    if (initPoint != null) {
      return ResponseEntity.ok(initPoint);
    }
    return ResponseEntity.internalServerError().body("No se pudo generar la preferencia de pago");
  }
}
