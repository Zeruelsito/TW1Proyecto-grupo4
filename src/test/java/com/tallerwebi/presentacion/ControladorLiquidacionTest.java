package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.ServicioLiquidacion;
import com.tallerwebi.dominio.TransferenciaSugerida;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorLiquidacionTest {

  private ControladorLiquidacion controladorLiquidacion;
  private ServicioLiquidacion servicioLiquidacionMock;

  @BeforeEach
  public void init() {
    servicioLiquidacionMock = mock(ServicioLiquidacion.class);
    controladorLiquidacion = new ControladorLiquidacion(servicioLiquidacionMock);
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
