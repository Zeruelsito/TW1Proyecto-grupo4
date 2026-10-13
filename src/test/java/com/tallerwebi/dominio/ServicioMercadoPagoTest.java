package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ServicioMercadoPagoTest {

  ServicioMercadoPago servicioMercadoPago = new ServicioMercadoPagoImpl();

  @Test
  void cuandoTokenEsInvalido_debeCapturarExcepcionYRetornarNull() {
    // Inyectamos un token ficticio mediante Reflection para simular el @Value
    ReflectionTestUtils.setField(
      servicioMercadoPago,
      "accessToken",
      "APP_USR-token-falso-invalido"
    );

    String resultado = servicioMercadoPago.crearPreferencia("Remera", new BigDecimal("5000.00"));

    assertNull(resultado);
  }
}
