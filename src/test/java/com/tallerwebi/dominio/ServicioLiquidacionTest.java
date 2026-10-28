package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ServicioLiquidacionTest {

  @Mock
  private ServicioCreditoTarea servicioCreditoTareaMock;

  @Mock
  private ServicioPagoRealizado servicioPagoRealizadoMock;

  @InjectMocks
  private ServicioLiquidacionImpl servicioLiquidacion;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void queRetorneListaVaciaSiHogarEsNullOIntegrantesEstanVacios() {
    Hogar hogarVacio = new Hogar("Hogar Vacío");
    hogarVacio.setIntegrantes(Collections.emptyList());

    List<TransferenciaSugerida> resultadoNull = servicioLiquidacion.obtenerLiquidacionDelMes(
      null,
      Collections.emptyList()
    );
    List<TransferenciaSugerida> resultadoVacio = servicioLiquidacion.obtenerLiquidacionDelMes(
      hogarVacio,
      Collections.emptyList()
    );

    assertTrue(resultadoNull.isEmpty());
    assertTrue(resultadoVacio.isEmpty());
  }

  @Test
  public void queCalculeLiquidacionCorrectamenteYFiltrePagosYaRealizados() {
    Usuario u1 = new Usuario();
    u1.setId(1L);
    u1.setEmail("juan@test.com");
    u1.setNombre("Juan");

    Usuario u2 = new Usuario();
    u2.setId(2L);
    u2.setEmail("maria@test.com");
    u2.setNombre("María");

    Hogar hogar = new Hogar("Casa");
    hogar.setId(10L);
    hogar.setIntegrantes(Arrays.asList(u1, u2));

    // Gasto total de 1000 pagado por Juan (u1) -> Cuota consumo individual = 500 c/u
    Gasto gasto = new Gasto();
    gasto.setMonto(1000.0);
    gasto.setPagado(true);
    gasto.setUsuarioPagador(u1);
    List<Gasto> gastos = Collections.singletonList(gasto);

    // Sin créditos de tareas
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(anyLong())).thenReturn(0.0);

    // Supongamos que ya existe un pago realizado de María a Juan por 500 (debe filtrarse)
    PagoRealizado pagoRealizado = new PagoRealizado(
      "María",
      "Juan",
      500.0,
      LocalDateTime.now(),
      hogar
    );
    when(servicioPagoRealizadoMock.obtenerHistorialPagos(10L))
      .thenReturn(Collections.singletonList(pagoRealizado));

    List<TransferenciaSugerida> transferencias = servicioLiquidacion.obtenerLiquidacionDelMes(
      hogar,
      gastos
    );

    // Como ya fue abonado, el resultado debe estar vacío
    assertTrue(transferencias.isEmpty());
  }

  @Test
  public void queGenereTransferenciaSugeridaSiAunNoFuePagado() {
    Usuario u1 = new Usuario();
    u1.setId(1L);
    u1.setEmail("juan@test.com");
    u1.setNombre("Juan");

    Usuario u2 = new Usuario();
    u2.setId(2L);
    u2.setEmail("maria@test.com");
    u2.setNombre("María");

    Hogar hogar = new Hogar("Casa");
    hogar.setId(10L);
    hogar.setIntegrantes(Arrays.asList(u1, u2));

    Gasto gasto = new Gasto();
    gasto.setMonto(1000.0);
    gasto.setPagado(true);
    gasto.setUsuarioPagador(u1);
    List<Gasto> gastos = Collections.singletonList(gasto);

    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(anyLong())).thenReturn(0.0);
    when(servicioPagoRealizadoMock.obtenerHistorialPagos(10L)).thenReturn(Collections.emptyList());

    List<TransferenciaSugerida> transferencias = servicioLiquidacion.obtenerLiquidacionDelMes(
      hogar,
      gastos
    );

    assertEquals(1, transferencias.size());
    assertEquals(500.0, transferencias.get(0).getMonto());
    assertEquals("María", transferencias.get(0).getEmisor());
    assertEquals("Juan", transferencias.get(0).getReceptor());
  }

  @Test
  public void queRegistrePagoExitosoCorrectamente() {
    Hogar hogar = new Hogar("Casa");
    doNothing().when(servicioPagoRealizadoMock).registrarPagoExitoso(any(PagoRealizado.class));

    servicioLiquidacion.registrarPagoExitoso(hogar, "emisor@test.com", "receptor@test.com", 300.0);

    verify(servicioPagoRealizadoMock, times(1)).registrarPagoExitoso(any(PagoRealizado.class));
  }

  @Test
  public void queObtengaHistorialPagosCorrectamente() {
    when(servicioPagoRealizadoMock.obtenerHistorialPagos(1L)).thenReturn(Collections.emptyList());

    List<PagoRealizado> historial = servicioLiquidacion.obtenerHistorialPagos(1L);

    assertNotNull(historial);
    verify(servicioPagoRealizadoMock, times(1)).obtenerHistorialPagos(1L);
  }

  @Test
  public void queCalculeLiquidacionOptimaConNombresCorrectamente() {
    Map<String, Double> balances = new HashMap<>();
    balances.put("deudor@test.com", -250.0);
    balances.put("acreedor@test.com", 250.0);

    Map<String, String> emailToNombre = new HashMap<>();
    emailToNombre.put("deudor@test.com", "Deudor");
    emailToNombre.put("acreedor@test.com", "Acreedor");

    List<TransferenciaSugerida> transferencias =
      servicioLiquidacion.calcularLiquidacionOptimaConNombres(balances, emailToNombre);

    assertEquals(1, transferencias.size());
    assertEquals(250.0, transferencias.get(0).getMonto());
    assertEquals("Deudor", transferencias.get(0).getEmisor());
    assertEquals("Acreedor", transferencias.get(0).getReceptor());
  }
}
