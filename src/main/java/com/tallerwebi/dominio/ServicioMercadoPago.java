package com.tallerwebi.dominio;

import java.math.BigDecimal;

@FunctionalInterface
public interface ServicioMercadoPago {
  String crearPreferencia(String titulo, BigDecimal precio);
}
