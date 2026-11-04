package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioReporteTest {

  private ServicioRegistroGasto servicioRegistroGasto;
  private ServicioCreditoTarea servicioCreditoTarea;
  private ServicioLiquidacion servicioLiquidacion;
  private ServicioHogar servicioHogar;
  private HttpSession session;
  private ServicioReporteImpl servicioReporte;

  private static final Long HOGAR_ID = 1L;
  private YearMonth mesTest;

  @BeforeEach
  public void init() {
    servicioRegistroGasto = mock(ServicioRegistroGasto.class);
    servicioCreditoTarea = mock(ServicioCreditoTarea.class);
    servicioLiquidacion = mock(ServicioLiquidacion.class);
    servicioHogar = mock(ServicioHogar.class);
    session = mock(HttpSession.class);

    servicioReporte =
      new ServicioReporteImpl(
        servicioRegistroGasto,
        servicioCreditoTarea,
        servicioLiquidacion,
        servicioHogar,
        session
      );

    mesTest = YearMonth.of(2026, 10);
  }

  @Test
  public void dadoUnMesКогдаGenerarReporteEntoncesRetornaReporteMensualConDatosCorrectos() {
    // Given
    Hogar hogarMock = givenUnHogar();
    List<Usuario> integrantesMock = givenIntegrantesDelHogar();
    List<Gasto> gastosMock = givenGastosDelMes();
    List<Tarea> tareasMock = givenTareasDelMes();
    List<TransferenciaSugerida> transferenciasMock = givenTransferenciasSugeridas();

    when(session.getAttribute("HOGAR_ID")).thenReturn(HOGAR_ID);
    when(servicioRegistroGasto.obtenerIntegrantes(HOGAR_ID)).thenReturn(integrantesMock);
    when(servicioRegistroGasto.obtenerTodosLosGastos(HOGAR_ID)).thenReturn(gastosMock);
    when(servicioCreditoTarea.obtenerTodasLasTareas()).thenReturn(tareasMock);
    when(servicioHogar.obtenerHogar()).thenReturn(hogarMock);
    when(servicioCreditoTarea.obtenerCreditoAcumuladoPorUsuario(1L)).thenReturn(500.0);
    when(servicioCreditoTarea.obtenerCreditoAcumuladoPorUsuario(2L)).thenReturn(1000.0);
    when(servicioLiquidacion.obtenerLiquidacionDelMes(eq(hogarMock), anyList()))
      .thenReturn(transferenciasMock);

    // When
    ReporteMensual reporte = whenGenerarReporte(mesTest);

    // Then
    thenElReporteContieneLosDatosEsperados(reporte, integrantesMock, gastosMock, tareasMock);
  }

  @Test
  public void dadoQueNoHayHogarEnSesionКогдаGenerarReporteEntoncesObtieneHogarDeServicio() {
    // Given
    Hogar hogarMock = givenUnHogar();
    List<Usuario> integrantesMock = givenIntegrantesDelHogar();

    when(session.getAttribute("HOGAR_ID")).thenReturn(null);
    when(servicioHogar.obtenerHogar()).thenReturn(hogarMock);
    when(servicioRegistroGasto.obtenerIntegrantes(HOGAR_ID)).thenReturn(integrantesMock);
    when(servicioRegistroGasto.obtenerTodosLosGastos(HOGAR_ID)).thenReturn(Collections.emptyList());
    when(servicioCreditoTarea.obtenerTodasLasTareas()).thenReturn(Collections.emptyList());
    when(servicioCreditoTarea.obtenerCreditoAcumuladoPorUsuario(anyLong())).thenReturn(0.0);
    when(servicioLiquidacion.obtenerLiquidacionDelMes(any(Hogar.class), anyList()))
      .thenReturn(Collections.emptyList());

    // When
    ReporteMensual reporte = whenGenerarReporte(mesTest);

    // Then
    assertNotNull(reporte);
    verify(servicioHogar, atLeastOnce()).obtenerHogar();
  }

  @Test
  public void dadoUnReporteMensualКогдаExportarCsvEntoncesRetornaStringConFormatoCsv() {
    // Given
    ReporteMensual reporteMock = givenUnReporteMensualMock();

    // When
    String csv = whenExportarCsv(reporteMock);

    // Then
    thenElCsvContieneLasSeccionesPrincipales(csv);
  }

  private Hogar givenUnHogar() {
    Hogar hogar = new Hogar("Hogar Kumo");
    hogar.setId(HOGAR_ID);
    return hogar;
  }

  private List<Usuario> givenIntegrantesDelHogar() {
    Usuario u1 = new Usuario("Maria", "maria@kumo.com", "1234");
    u1.setId(1L);
    Usuario u2 = new Usuario("Juan", "juan@kumo.com", "5678");
    u2.setId(2L);
    return List.of(u1, u2);
  }

  private List<Gasto> givenGastosDelMes() {
    Usuario u1 = new Usuario("Maria", "maria@kumo.com", "1234");
    u1.setId(1L);
    Gasto gasto = new Gasto(
      "G001",
      TipoGasto.OCASIONAL,
      "Supermercado",
      4000.0,
      u1,
      "ALIMENTACION"
    );
    gasto.setFecha(LocalDateTime.of(2026, 10, 15, 10, 0));
    return List.of(gasto);
  }

  private List<Tarea> givenTareasDelMes() {
    Usuario u1 = new Usuario("Maria", "maria@kumo.com", "1234");
    u1.setId(1L);
    Tarea tarea = new Tarea("Limpiar cocina", 2.0, 500.0, u1);
    tarea.setFechaRegistro(LocalDateTime.of(2026, 10, 16, 11, 0));
    return List.of(tarea);
  }

  private List<TransferenciaSugerida> givenTransferenciasSugeridas() {
    return List.of(new TransferenciaSugerida("Juan", "test@unlam.edu.ar", "Maria", 2000.0));
  }

  private ReporteMensual givenUnReporteMensualMock() {
    Usuario u1 = new Usuario("Maria", "maria@kumo.com", "1234");
    u1.setId(1L);
    Gasto gasto = new Gasto("G002", TipoGasto.OCASIONAL, "Internet", 5000.0, u1, "SERVICIOS");
    Tarea tarea = new Tarea("Pintar", 4.0, 1000.0, u1);
    tarea.setFechaRegistro(LocalDateTime.of(2026, 10, 11, 14, 0));

    FilaBalance balance = new FilaBalance("Maria", 5000.0, 1000.0, 2500.0);
    TransferenciaSugerida transferencia = new TransferenciaSugerida(
      "Juan",
      "test@unlam.edu.ar",
      "Maria",
      2500.0
    );

    ReporteMensual reporteMock = mock(ReporteMensual.class);
    when(reporteMock.getMes()).thenReturn(mesTest);
    when(reporteMock.getGastos()).thenReturn(Collections.singletonList(gasto));
    when(reporteMock.getTareas()).thenReturn(Collections.singletonList(tarea));
    when(reporteMock.getBalances()).thenReturn(Collections.singletonList(balance));
    when(reporteMock.getTransferencias()).thenReturn(Collections.singletonList(transferencia));
    when(reporteMock.nombreDe(1L)).thenReturn("Maria");
    when(reporteMock.getTotalGastos()).thenReturn(5000.0);
    return reporteMock;
  }

  private ReporteMensual whenGenerarReporte(YearMonth mes) {
    return servicioReporte.generarReporte(mes);
  }

  private String whenExportarCsv(ReporteMensual reporte) {
    return servicioReporte.exportarCsv(reporte);
  }

  private void thenElReporteContieneLosDatosEsperados(
    ReporteMensual reporte,
    List<Usuario> integrantes,
    List<Gasto> gastos,
    List<Tarea> tareas
  ) {
    assertNotNull(reporte);
    assertThat(reporte.getMes(), equalTo(mesTest));
    assertThat(reporte.getGastos(), hasSize(gastos.size()));
    assertThat(reporte.getTareas(), hasSize(tareas.size()));
    assertThat(reporte.getBalances(), hasSize(integrantes.size()));
    assertThat(reporte.getTransferencias(), hasSize(1));
  }

  private void thenElCsvContieneLasSeccionesPrincipales(String csv) {
    assertNotNull(csv);
    assertThat(csv, containsString("Reporte mensual"));
    assertThat(csv, containsString("GASTOS"));
    assertThat(csv, containsString("TAREAS"));
    assertThat(csv, containsString("BALANCES POR INTEGRANTE"));
    assertThat(csv, containsString("LIQUIDACION SUGERIDA"));
  }
}
