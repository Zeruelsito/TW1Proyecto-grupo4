package com.tallerwebi.dominio;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ServicioMercadoPagoImpl implements ServicioMercadoPago {

  private final PreferenceClient client;

  @Autowired
  public ServicioMercadoPagoImpl(
    @Value(
      "APP_USR-5982680599461888-102114-d7684bf5791dddd4432384969f0d21f4-3760410508"
    ) String accessToken
  ) {
    MercadoPagoConfig.setAccessToken(accessToken);
    this.client = new PreferenceClient();
  }

  // 2. Constructor para Pruebas Unitarias (permite inyectar el mock de PreferenceClient)
  public ServicioMercadoPagoImpl(PreferenceClient client) {
    this.client = client;
  }

  @Override
  public Preference crearUrl(String titulo, BigDecimal precio) throws MPException, MPApiException {
    PreferenceItemRequest item = PreferenceItemRequest
      .builder()
      .title(titulo)
      .quantity(1)
      .currencyId("ARS")
      .unitPrice(precio)
      .build();

    PreferenceRequest request = PreferenceRequest
      .builder()
      .items(List.of(item))
      .autoReturn("approved")
      .backUrls(
        PreferenceBackUrlsRequest
          .builder()
          .success("https://improve-monsoon-feeble.ngrok-free.dev/spring/pago/succesControladorMP")
          .failure("https://improve-monsoon-feeble.ngrok-free.dev/spring/pago/errorControladorMP")
          .pending("https://improve-monsoon-feeble.ngrok-free.dev/spring/pago/pending")
          .build()
      )
      .build();

    return client.create(request);
  }
}
