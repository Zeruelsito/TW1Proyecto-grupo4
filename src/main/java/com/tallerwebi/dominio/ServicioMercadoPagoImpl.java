package com.tallerwebi.dominio;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(ServicioMercadoPagoImpl.class);

  @Value("${MP_ACCESS_TOKEN}")
  private String accessToken;

  @Override
  public String crearUrl(String titulo, Double precio) throws MPException, MPApiException {
    validarParametros(titulo, precio);

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(
        "Iniciando creación de preferencia de pago para: '{}' con monto: {}",
        titulo,
        precio
      );
    }

    MercadoPagoConfig.setAccessToken(accessToken);

    PreferenceRequest preferenceRequest = construirPreferenceRequest(titulo, precio);
    PreferenceClient client = new PreferenceClient();

    try {
      Preference preference = client.create(preferenceRequest);
      return preference.getSandboxInitPoint();
    } catch (MPApiException e) {
      if (LOGGER.isErrorEnabled()) {
        String errorContent = (e.getApiResponse() != null)
          ? e.getApiResponse().getContent()
          : "Sin contenido";
        LOGGER.error(
          "Error al comunicarse com la API de Mercado Pago - Status: {} | Detalle: {}",
          e.getStatusCode(),
          errorContent,
          e
        );
      }
      throw e;
    }
  }

  private void validarParametros(String titulo, Double precio) {
    if (titulo == null || titulo.trim().isEmpty()) {
      throw new IllegalArgumentException("El título del pago no puede ser nulo o vacío");
    }
    if (precio == null || precio.compareTo(0.0) <= 0) {
      throw new IllegalArgumentException("El precio debe ser mayor a cero");
    }
  }

  private PreferenceRequest construirPreferenceRequest(String titulo, Double precio) {
    PreferenceItemRequest itemRequest = PreferenceItemRequest
      .builder()
      .title(titulo)
      .quantity(1)
      .unitPrice(BigDecimal.valueOf(precio))
      .currencyId("ARS")
      .build();

    return PreferenceRequest
      .builder()
      .items(Collections.singletonList(itemRequest))
      .backUrls(
        PreferenceBackUrlsRequest
          .builder()
          .success("http://localhost:8080/pagos/exitoso")
          .failure("http://localhost:8080/pagos/errorControladorMp")
          .pending("http://localhost:8080/pagos/pendiente")
          .build()
      )
      .autoReturn("approved")
      .build();
  }
}
