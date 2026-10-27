package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class ServicioMercadoPagoTest {

  @InjectMocks
  private ServicioMercadoPago mercadoPagoService;

  String token = System.getenv("MP_ACCESS_TOKEN");

  @BeforeEach
  void setUp() {
    // Inyectamos el valor de la propiedad @Value("${mercadopago.seller.access.token}") manualmente para el test
    mercadoPagoService = new ServicioMercadoPagoImpl();
    ReflectionTestUtils.setField(mercadoPagoService, "accessToken", token);
  }

  @Test
  @DisplayName("Debe generar la preferencia y retornar el enlace de Sandbox (SandboxInitPoint)")
  void debeCrearPreferenciaYRetornarSandboxUrl() throws MPException, MPApiException {
    // ARRANGE (Preparación de datos)
    String titulo = "Liquidación de gastos - De usuario 123 a 456";
    Double monto = new Double("1500.50");
    String urlSandboxEsperada =
      "https://sandbox.mercadopago.com.ar/checkout/v1/redirect?pref_id=test-123456";

    // Mock del objeto Preference que retorna Mercado Pago
    Preference preferenceMock = mock(Preference.class);
    when(preferenceMock.getSandboxInitPoint()).thenReturn(urlSandboxEsperada);

    // Interceptamos la instanciación de 'new PreferenceClient()' dentro del servicio
    try (
      MockedConstruction<PreferenceClient> mockedClient = Mockito.mockConstruction(
        PreferenceClient.class,
        (mock, context) -> {
          when(mock.create(any(PreferenceRequest.class))).thenReturn(preferenceMock);
        }
      )
    ) {
      // ACT (Ejecución del método)
      String resultadoUrl = mercadoPagoService.crearUrl(titulo, monto);

      // ASSERT (Verificaciones)
      assertNotNull(resultadoUrl, "La URL generada no debe ser nula");
      assertEquals(
        urlSandboxEsperada,
        resultadoUrl,
        "La URL devuelta debe coincidir con la de Sandbox mockeada"
      );

      // Verifica que se haya instanciado e invocado el cliente de preferencia de Mercado Pago
      assertEquals(
        1,
        mockedClient.constructed().size(),
        "Se debe haber creado una instancia de PreferenceClient"
      );
      verify(mockedClient.constructed().get(0), times(1)).create(any(PreferenceRequest.class));
    }
  }

  @Test
  @DisplayName("Debe lanzar MPException si la API de Mercado Pago falla")
  void debeLanzarExcepcionCuandoFallaMercadoPago() throws MPException, MPApiException {
    // ARRANGE
    String titulo = "Pago con error";
    Double monto = 500.00;

    try (
      MockedConstruction<PreferenceClient> mockedClient = Mockito.mockConstruction(
        PreferenceClient.class,
        (mock, context) -> {
          // Usamos doThrow en lugar de when().thenThrow() para excepciones chequeadas
          doThrow(new MPException("Error en comunicación con la API de Mercado Pago"))
            .when(mock)
            .create(any(PreferenceRequest.class));
        }
      )
    ) {
      // ACT & ASSERT
      assertThrows(
        MPException.class,
        () -> {
          mercadoPagoService.crearUrl(titulo, monto);
        },
        "Debe propagar la MPException al fallar la creación"
      );
    }
  }
}
