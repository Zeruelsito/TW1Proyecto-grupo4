package com.tallerwebi.dominio;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ServicioMercadoPagoImpl implements ServicioMercadoPago {

  private static final Logger logger = LoggerFactory.getLogger(ServicioMercadoPagoImpl.class);

  @Value("${MP_ACCESS_TOKEN}")
  private String accessToken;

  @Override
  public String crearPreferencia(String titulo, BigDecimal precio) {
    MercadoPagoConfig.setAccessToken(accessToken);
    PreferenceItemRequest itemRequest = PreferenceItemRequest
      .builder()
      .title(titulo)
      .quantity(1)
      .unitPrice(precio)
      .currencyId("ARS")
      .build();

    PreferenceRequest preferenceRequest = PreferenceRequest
      .builder()
      .items(Collections.singletonList(itemRequest))
      .build();

    try {
      PreferenceClient client = new PreferenceClient();
      Preference preference = client.create(preferenceRequest);
      return preference.getInitPoint();
    } catch (MPApiException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error de API de Mercado Pago: {}", e.getApiResponse().getContent());
      }
    } catch (MPException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error general de SDK de Mercado Pago: {}", e.getMessage());
      }
    }
    return null;
  }
}
