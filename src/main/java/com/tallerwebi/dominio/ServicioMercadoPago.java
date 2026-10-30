package com.tallerwebi.dominio;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface ServicioMercadoPago {
  String crearUrl(String titulo, Double precio) throws MPException, MPApiException;
}
