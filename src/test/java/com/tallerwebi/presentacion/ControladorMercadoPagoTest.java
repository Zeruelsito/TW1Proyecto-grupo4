package com.tallerwebi.presentacion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.tallerwebi.dominio.ServicioMercadoPago;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

class ControladorMercadoPagoTest {

  private MockMvc mockMvc;
  private ServicioMercadoPago mercadoPagoService;

  @BeforeEach
  void setUp() {
    // Inicializamos el mock del servicio manualmente
    mercadoPagoService = mock(ServicioMercadoPago.class);

    // Creamos el controlador inyectándole el mock
    ControladorMercadoPago mercadoPagoController = new ControladorMercadoPago(mercadoPagoService);

    // Configuramos MockMvc de forma aislada sin levantar todo Spring Boot
    mockMvc = standaloneSetup(mercadoPagoController).build();
  }

  @Test
  void cuandoCrearPreferenciaEsExitoso_debeRetornarOkYUrl() throws Exception {
    String urlSimulada =
      "https://www.mercadopago.com.ar/sandbox/checkout/pref?pref_id=12345678-abcd";

    when(mercadoPagoService.crearPreferencia(eq("Producto Test"), any(BigDecimal.class)))
      .thenReturn(urlSimulada);

    mockMvc
      .perform(post("/api/pago/crear").param("titulo", "Producto Test").param("precio", "1500.50"))
      .andExpect(status().isOk())
      .andExpect(content().string(urlSimulada));
  }

  @Test
  void cuandoCrearPreferenciaFalla_debeRetornarInternalServerError() throws Exception {
    when(mercadoPagoService.crearPreferencia(any(), any())).thenReturn(null);

    mockMvc
      .perform(post("/api/pago/crear").param("titulo", "Producto Test").param("precio", "1500.50"))
      .andExpect(status().isInternalServerError())
      .andExpect(content().string("No se pudo generar la preferencia de pago"));
  }
}
