package com.tallerwebi.dominio;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import java.math.BigDecimal;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface ServicioMercadoPago {
  String crearUrl(String titulo, Double precio) throws MPException, MPApiException;
}
