package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.ServicioLiquidacion;
import com.tallerwebi.dominio.ServicioRegistroGasto;
import com.tallerwebi.dominio.TransferenciaSugerida;
import com.tallerwebi.dominio.Usuario;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorLiquidacionTest {

  private ControladorLiquidacion controladorLiquidacion;
  private ServicioLiquidacion servicioLiquidacionMock;
  private ServicioRegistroGasto servicioRegistroGastoMock; // <-- Agregamos el mock

  @BeforeEach
  public void init() {
    servicioLiquidacionMock = mock(ServicioLiquidacion.class);
    servicioRegistroGastoMock = mock(ServicioRegistroGasto.class);

    // Actualizado: Pasamos ambos servicios al constructor
    controladorLiquidacion =
      new ControladorLiquidacion(servicioLiquidacionMock, servicioRegistroGastoMock);
  }

  @Test
  public void debedevolverVistaLiquidacionConTransferenciasSugeridas() {
    // Given
    TransferenciaSugerida transferenciaEsperada = new TransferenciaSugerida(
      "Maria",
      "Juan",
      3000.0
    );
    List<TransferenciaSugerida> transferenciasMock = List.of(transferenciaEsperada);

    // Simulamos que el servicio de registro devuelve integrantes y gastos para que el controlador no falle
    Usuario juan = new Usuario("Juan", "juan@kumo.com", "1234");
    juan.setId(1L);
    Usuario maria = new Usuario("Maria", "maria@kumo.com", "1212");
    maria.setId(2L);

    when(servicioRegistroGastoMock.obtenerIntegrantes()).thenReturn(List.of(juan, maria));
    when(servicioRegistroGastoMock.obtenerTodosLosGastos()).thenReturn(List.of());

    when(servicioLiquidacionMock.obtenerLiquidacionDelMes(any(Hogar.class), anyList()))
      .thenReturn(transferenciasMock);

    // When
    ModelAndView modelAndView = controladorLiquidacion.irALiquidacion();

    // Then
    assertThat(modelAndView.getViewName(), is(equalTo("liquidacion")));
    assertThat(modelAndView.getModel().get("transferencias"), is(notNullValue()));

    @SuppressWarnings("unchecked")
    List<TransferenciaSugerida> transferenciasEnModelo = (List<TransferenciaSugerida>) modelAndView
      .getModel()
      .get("transferencias");

    assertThat(transferenciasEnModelo, hasSize(1));
    assertThat(transferenciasEnModelo.get(0), is(equalTo(transferenciaEsperada)));
    assertThat(modelAndView.getModel().get("usuarioLogueado"), is(equalTo("Juan")));
  }
}
