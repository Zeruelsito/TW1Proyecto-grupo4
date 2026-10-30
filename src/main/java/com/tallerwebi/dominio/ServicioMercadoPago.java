package com.tallerwebi.dominio;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import java.math.BigDecimal;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface ServicioMercadoPago {
  Preference crearUrl(String titulo, BigDecimal precio) throws MPException, MPApiException;
}
