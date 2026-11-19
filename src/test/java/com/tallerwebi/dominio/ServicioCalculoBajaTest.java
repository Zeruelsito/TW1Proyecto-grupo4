package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ServicioCalculoBajaTest {

  private RepositorioUsuario repositorioUsuarioMock;
  private ServicioLiquidacion servicioLiquidacionMock;
  private ServicioCreditoTarea servicioCreditoTareaMock;
  private ServicioPagoRealizado servicioPagoRealizadoMock;
  private CalculadorCuotasBaja calculadorCuotasBajaMock;

  private ServicioCalculoBajaImpl servicioCalculoBaja;

  private Hogar hogarTest;
  private Usuario usuario1;
  private Usuario usuario2;
  private Usuario usuario3;

  @BeforeEach
  public void setUp() {
    repositorioUsuarioMock = Mockito.mock(RepositorioUsuario.class);
    servicioLiquidacionMock = Mockito.mock(ServicioLiquidacion.class);
    servicioCreditoTareaMock = Mockito.mock(ServicioCreditoTarea.class);
    servicioPagoRealizadoMock = Mockito.mock(ServicioPagoRealizado.class);
    calculadorCuotasBajaMock = Mockito.mock(CalculadorCuotasBaja.class);

    servicioCalculoBaja =
      new ServicioCalculoBajaImpl(
        repositorioUsuarioMock,
        servicioLiquidacionMock,
        servicioCreditoTareaMock,
        servicioPagoRealizadoMock,
        calculadorCuotasBajaMock
      );

    usuario1 = new Usuario("Carlos", 50000.0);
    usuario1.setId(1L);

    usuario2 = new Usuario("Juan", 60000.0);
    usuario2.setId(2L);

    usuario3 = new Usuario("Ana", 70000.0);
    usuario3.setId(3L);

    hogarTest = new Hogar();
    hogarTest.setId(10L);
    hogarTest.setPisoMinimoHogar(1000.0);
    hogarTest.setPrecioHoraTarea(500.0);
    hogarTest.setIntegrantes(Arrays.asList(usuario1, usuario2, usuario3));

    when(servicioPagoRealizadoMock.obtenerHistorialPagos(any()))
      .thenReturn(Collections.emptyList());
    when(repositorioUsuarioMock.obtenerTodos())
      .thenReturn(Arrays.asList(usuario1, usuario2, usuario3));
  }

  @Test
  public void queRetorneCeroHorasYDeudaSiSalienteONHogarSonNulos() {
    double[] resultado = servicioCalculoBaja.calcularDeudaHorasTarea(
      null,
      hogarTest,
      Collections.emptyList()
    );

    assertThat(resultado[0], is(closeTo(0.0, 0.001)));
    assertThat(resultado[1], is(closeTo(0.0, 0.001)));
  }

  @Test
  public void queCalculeDeudaPorHorasTareaUsandoDineroRequeridoYCreditoAcumulado() {
    Map<String, Double> metricas = new HashMap<>();
    metricas.put("dineroRequeridoBrecha", 2000.0);
    when(
      servicioCreditoTareaMock.calcularMetricasBrechaUsuario(
        eq(usuario2),
        eq(hogarTest),
        anyList(),
        eq(500.0)
      )
    )
      .thenReturn(metricas);
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(2L)).thenReturn(500.0);

    // $2000 requeridos - $500 acumulados = $1500 deuda => ceil(1500 / 500 * 2) / 2 = 3.0 hs
    double[] resultado = servicioCalculoBaja.calcularDeudaHorasTarea(
      usuario2,
      hogarTest,
      Collections.emptyList()
    );

    assertThat(resultado[0], is(closeTo(1500.0, 0.001)));
    assertThat(resultado[1], is(closeTo(3.0, 0.001)));
  }

  @Test
  public void queDistribuyaDeudaPorHorasEntreRestantesYApliquePagosRealizados() {
    List<Usuario> integrantes = Arrays.asList(usuario1, usuario2, usuario3);
    List<Usuario> restantes = Arrays.asList(usuario1, usuario3);

    PagoRealizado pago = new PagoRealizado(
      usuario2,
      usuario1,
      200.0,
      LocalDateTime.now(),
      hogarTest
    );
    when(servicioPagoRealizadoMock.obtenerHistorialPagos(10L)).thenReturn(List.of(pago));

    Map<String, Double> balances = servicioCalculoBaja.calcularBalancesConBaja(
      integrantes,
      restantes,
      usuario2,
      Collections.emptyList(),
      600.0,
      10L
    );

    assertThat(balances.get("Juan"), is(closeTo(-400.0, 0.001)));
    assertThat(balances.get("Carlos"), is(closeTo(100.0, 0.001)));
    assertThat(balances.get("Ana"), is(closeTo(300.0, 0.001)));
  }

  @Test
  public void queFiltreTransferenciasDelSalienteYCompleteIdsDeEmisorYReceptor() {
    List<TransferenciaSugerida> sugeridas = List.of(
      new TransferenciaSugerida("Juan", "juan@mail.com", "Carlos", 500.0),
      new TransferenciaSugerida("Carlos", "carlos@mail.com", "Ana", 300.0),
      new TransferenciaSugerida("Ana", "ana@mail.com", "Juan", 150.0)
    );
    when(servicioLiquidacionMock.calcularLiquidacionOptima(any())).thenReturn(sugeridas);

    List<TransferenciaSugerida> delSaliente = servicioCalculoBaja.liquidacionDelSaliente(
      "Juan",
      new HashMap<>()
    );

    assertThat(delSaliente, hasSize(2));
    assertThat(delSaliente.get(0).getEmisor(), is("Juan"));
    assertThat(delSaliente.get(0).getEmisorId(), is(2L));
    assertThat(delSaliente.get(0).getReceptorId(), is(1L));
    assertThat(delSaliente.get(1).getReceptor(), is("Juan"));
    assertThat(delSaliente.get(1).getEmisorId(), is(3L));
    assertThat(delSaliente.get(1).getReceptorId(), is(2L));
  }
}
