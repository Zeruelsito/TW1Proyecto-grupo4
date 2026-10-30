package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioMercadoPagoTest {

  private PreferenceClient preferenceClientMock;
  private ServicioMercadoPagoImpl servicioMercadoPago;

  @BeforeEach
  public void setUp() {
    // Creamos el mock del cliente de Mercado Pago
    preferenceClientMock = mock(PreferenceClient.class);

    // Inyectamos el mock usando el constructor alternativo que provee la clase
    servicioMercadoPago = new ServicioMercadoPagoImpl(preferenceClientMock);
  }

  @Test
  public void queSePuedaCrearUnaUrlDePreferenciaExitosamente() throws MPException, MPApiException {
    String titulo = "Transferencia";
    BigDecimal precio = BigDecimal.valueOf(1500.0);

    Preference preferenceMock = mock(Preference.class);
    when(preferenceMock.getSandboxInitPoint())
      .thenReturn("https://sandbox.mercadopago.com.ar/checkout/init?pref_id=xyz");

    // Simulamos que el cliente de Mercado Pago responde correctamente al crear la preferencia
    when(preferenceClientMock.create(any(PreferenceRequest.class))).thenReturn(preferenceMock);

    Preference resultado = servicioMercadoPago.crearUrl(titulo, precio);

    assertThat(resultado, notNullValue());
    assertThat(
      resultado.getSandboxInitPoint(),
      equalTo("https://sandbox.mercadopago.com.ar/checkout/init?pref_id=xyz")
    );

    // Verificamos que se haya invocado al método create del cliente de Mercado Pago exactamente 1 vez
    verify(preferenceClientMock, times(1)).create(any(PreferenceRequest.class));
  }

  @Test
  public void quePropagueMPExceptionSiFallaLaCreacionDePreferencia()
    throws MPException, MPApiException {
    String titulo = "Transferencia";
    BigDecimal precio = BigDecimal.valueOf(1000.0);

    // Simulamos que el cliente de Mercado Pago lanza una MPException (ej. problema de red o credenciales)
    when(preferenceClientMock.create(any(PreferenceRequest.class)))
      .thenThrow(new MPException("Error de conexión con Mercado Pago"));

    org.junit.jupiter.api.Assertions.assertThrows(
      MPException.class,
      () -> {
        servicioMercadoPago.crearUrl(titulo, precio);
      }
    );

    verify(preferenceClientMock, times(1)).create(any(PreferenceRequest.class));
  }

  @Test
  public void quePropagueMPApiExceptionSiLaApiDeMercadoPagoRespondeConError()
    throws MPException, MPApiException {
    String titulo = "Transferencia";
    BigDecimal precio = BigDecimal.valueOf(2000.0);

    // Simulamos que el cliente lanza una MPApiException (ej. error 4xx o 5xx devuelto por la API)
    MPApiException apiExceptionMock = mock(MPApiException.class);
    when(preferenceClientMock.create(any(PreferenceRequest.class))).thenThrow(apiExceptionMock);

    org.junit.jupiter.api.Assertions.assertThrows(
      MPApiException.class,
      () -> {
        servicioMercadoPago.crearUrl(titulo, precio);
      }
    );

    verify(preferenceClientMock, times(1)).create(any(PreferenceRequest.class));
  }
}
