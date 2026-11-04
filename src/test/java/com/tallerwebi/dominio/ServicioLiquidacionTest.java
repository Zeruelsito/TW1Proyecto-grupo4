package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ServicioLiquidacionTest {

  private ServicioCreditoTarea servicioCreditoTareaMock;
  private ServicioPagoRealizado servicioPagoRealizadoMock;
  private ServicioLiquidacionImpl servicioLiquidacion;

  private Hogar hogarTest;
  private Usuario usuario1;
  private Usuario usuario2;
  private Gasto gastoTest;

  @BeforeEach
  public void setUp() {
    servicioCreditoTareaMock = Mockito.mock(ServicioCreditoTarea.class);
    servicioPagoRealizadoMock = Mockito.mock(ServicioPagoRealizado.class);
    servicioLiquidacion =
      new ServicioLiquidacionImpl(servicioCreditoTareaMock, servicioPagoRealizadoMock);

    usuario1 = new Usuario();
    usuario1.setId(1L);
    usuario1.setEmail("carlos@unlam.edu.ar");
    usuario1.setNombre("Carlos");

    usuario2 = new Usuario();
    usuario2.setId(2L);
    usuario2.setEmail("juan@unlam.edu.ar");
    usuario2.setNombre("Juan");

    hogarTest = new Hogar();
    hogarTest.setId(10L);
    hogarTest.setIntegrantes(Arrays.asList(usuario1, usuario2));

    gastoTest = new Gasto();
    gastoTest.setMonto(1000.0);
    gastoTest.setPagado(true);
    gastoTest.setUsuarioPagador(usuario1);
    gastoTest.setParticipantes(Arrays.asList(usuario1, usuario2));
    gastoTest.setFecha(LocalDateTime.now());
  }

  @Test
  public void queRetorneListaVaciaSiHogarEsNuloOSinIntegrantes() {
    List<TransferenciaSugerida> resultadoNulo = servicioLiquidacion.obtenerLiquidacionDelMes(
      null,
      Collections.emptyList()
    );
    List<TransferenciaSugerida> resultadoVacio = servicioLiquidacion.obtenerLiquidacionDelMes(
      new Hogar(),
      Collections.emptyList()
    );

    assertThat(resultadoNulo, is(empty()));
    assertThat(resultadoVacio, is(empty()));
  }

  @Test
  public void queCalculeCorrectamenteLaLiquidacionDelMesSinPagosPrevios() {
    // Given: Carlos pagó 1000 y se divide entre 2 (500 cada uno). Juan debe 500 a Carlos.
    when(servicioCreditoTareaMock.obtenerCreditoLiquidadoPorUsuario(anyLong())).thenReturn(0.0);
    when(servicioPagoRealizadoMock.obtenerHistorialPagos(10L)).thenReturn(Collections.emptyList());

    // When
    List<TransferenciaSugerida> transferencias = servicioLiquidacion.obtenerLiquidacionDelMes(
      hogarTest,
      Arrays.asList(gastoTest)
    );

    // Then
    assertThat(transferencias, notNullValue());
    assertThat(transferencias, hasSize(1));
    assertThat(transferencias.get(0).getEmisor(), is("Juan"));
    assertThat(transferencias.get(0).getReceptor(), is("Carlos"));
    assertThat(transferencias.get(0).getMonto(), is(500.0));
  }

  @Test
  public void queFiltreTransferenciasSiYaFueronPagadasExitosamente() {
    // Given
    when(servicioCreditoTareaMock.obtenerCreditoLiquidadoPorUsuario(anyLong())).thenReturn(0.0);

    // Ya existe un pago registrado idéntico a la transferencia sugerida
    PagoRealizado pagoExistente = new PagoRealizado(usuario2, usuario1, 500.0, null, hogarTest);
    when(servicioPagoRealizadoMock.obtenerHistorialPagos(10L))
      .thenReturn(Arrays.asList(pagoExistente));

    // When
    List<TransferenciaSugerida> transferencias = servicioLiquidacion.obtenerLiquidacionDelMes(
      hogarTest,
      Arrays.asList(gastoTest)
    );

    // Then
    assertThat(transferencias, is(empty()));
  }

  @Test
  public void queCalculeLaDeudaDeUnUsuarioCorrectamente() {
    // Given: Juan consume 500 pero pagó 0 -> debe 500.
    when(servicioCreditoTareaMock.obtenerCreditoLiquidadoPorUsuario(2L)).thenReturn(0.0);

    // When
    double deuda = servicioLiquidacion.obtenerDeudaUsuario(
      usuario2,
      hogarTest,
      Arrays.asList(gastoTest)
    );

    // Then
    assertThat(deuda, is(500.0));
  }

  @Test
  public void queRetorneCeroDeudaSiElUsuarioNoTieneSaldoNegativo() {
    // Given: Carlos pagó 1000 y consume 500 -> saldo positivo (acreedor), deuda debe ser 0.
    when(servicioCreditoTareaMock.obtenerCreditoLiquidadoPorUsuario(1L)).thenReturn(0.0);

    // When
    double deuda = servicioLiquidacion.obtenerDeudaUsuario(
      usuario1,
      hogarTest,
      Arrays.asList(gastoTest)
    );

    // Then
    assertThat(deuda, is(0.0));
  }

  @Test
  public void quePermitaRegistrarUnPagoExitosoCorrectamente() {
    // When
    servicioLiquidacion.registrarPagoExitoso(hogarTest, usuario2, usuario1, 500.0);

    // Then
    verify(servicioPagoRealizadoMock, times(1)).registrarPagoExitoso(any());
  }

  @Test
  public void queObtengaElHistorialDePagosCorrectamente() {
    // Given
    List<PagoRealizado> listaMock = Arrays.asList(new PagoRealizado());
    when(servicioPagoRealizadoMock.obtenerHistorialPagos(10L)).thenReturn(listaMock);

    // When
    List<PagoRealizado> resultado = servicioLiquidacion.obtenerHistorialPagos(10L);

    // Then
    assertThat(resultado, notNullValue());
    assertThat(resultado, hasSize(1));
    verify(servicioPagoRealizadoMock, times(1)).obtenerHistorialPagos(10L);
  }
}
