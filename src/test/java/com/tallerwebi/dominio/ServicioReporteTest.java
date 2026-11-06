package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
  public void dadoUnMesCuandoGenerarReporteEntoncesRetornaReporteMensualConDatosCorrectos() {
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
    when(servicioHogar.seleccionarHogarPorId(HOGAR_ID)).thenReturn(hogarMock);
    when(servicioLiquidacion.obtenerLiquidacionDelMes(any(Hogar.class), anyList()))
      .thenReturn(transferenciasMock);

    // When
    ReporteMensual reporte = whenGenerarReporte(mesTest);

    // Then
    thenElReporteContieneLosDatosEsperados(reporte, integrantesMock, gastosMock, tareasMock);
  }

  @Test
  public void dadoQueNoHayHogarEnSesionCuandoGenerarReporteEntoncesObtieneHogarDeServicio() {
    // Given
    Hogar hogarMock = givenUnHogar();
    List<Usuario> integrantesMock = givenIntegrantesDelHogar();

    when(session.getAttribute("HOGAR_ID")).thenReturn(null);
    when(servicioHogar.obtenerHogar()).thenReturn(hogarMock);
    when(servicioRegistroGasto.obtenerIntegrantes(HOGAR_ID)).thenReturn(integrantesMock);
    when(servicioRegistroGasto.obtenerTodosLosGastos(HOGAR_ID)).thenReturn(Collections.emptyList());
    when(servicioCreditoTarea.obtenerTodasLasTareas()).thenReturn(Collections.emptyList());
    when(servicioLiquidacion.obtenerLiquidacionDelMes(any(Hogar.class), anyList()))
      .thenReturn(Collections.emptyList());

    // When
    ReporteMensual reporte = whenGenerarReporte(mesTest);

    // Then
    assertNotNull(reporte);
    verify(servicioHogar, atLeastOnce()).obtenerHogar();
  }

  // --- Punto 1: las tareas se filtran por hogar ---
  @Test
  public void dadoTareasDeOtroHogarCuandoGenerarReporteEntoncesNoAparecenEnElReporte() {
    // Given
    List<Usuario> integrantes = givenIntegrantesDelHogar();
    Usuario maria = integrantes.get(0);

    Usuario ajeno = new Usuario("Pedro", "pedro@otrohogar.com", "1234");
    ajeno.setId(99L);

    Tarea tareaDelHogar = givenTarea("Limpiar cocina", maria, EstadoTarea.COMPLETADA, 500.0);
    Tarea tareaDeOtroHogar = givenTarea("Cortar pasto", ajeno, EstadoTarea.COMPLETADA, 800.0);

    givenUnHogarConIntegrantesYTareas(integrantes, List.of(tareaDelHogar, tareaDeOtroHogar));

    // When
    ReporteMensual reporte = whenGenerarReporte(mesTest);

    // Then
    assertThat(reporte.getTareas(), hasSize(1));
    assertThat(reporte.getTareas().get(0).getTitulo(), equalTo("Limpiar cocina"));
  }

  // --- Punto 2: la liquidación usa el hogar real, sin modificarlo ---
  @Test
  public void dadoUnHogarRealCuandoGenerarReporteEntoncesLaLiquidacionUsaSusParametros() {
    // Given
    List<Usuario> integrantes = givenIntegrantesDelHogar();
    Hogar hogarReal = givenUnHogar();
    hogarReal.setPrecioHoraTarea(7000.0);
    hogarReal.setPisoMinimoHogar(60000.0);

    givenUnHogarConIntegrantesYTareas(integrantes, Collections.emptyList());
    when(servicioHogar.seleccionarHogarPorId(HOGAR_ID)).thenReturn(hogarReal);

    // When
    whenGenerarReporte(mesTest);

    // Then
    ArgumentCaptor<Hogar> captor = ArgumentCaptor.forClass(Hogar.class);
    verify(servicioLiquidacion).obtenerLiquidacionDelMes(captor.capture(), anyList());
    Hogar usado = captor.getValue();

    assertThat(usado.getId(), equalTo(HOGAR_ID));
    assertThat(usado.getPrecioHoraTarea(), equalTo(7000.0));
    assertThat(usado.getPisoMinimoHogar(), equalTo(60000.0));
    assertThat(usado.getIntegrantes(), hasSize(2));
    // No se toca el hogar real ni el hogar en memoria del servicio
    assertThat(usado, not(sameInstance(hogarReal)));
    assertThat(hogarReal.getIntegrantes(), empty());
    verify(servicioHogar, never()).obtenerHogar();
  }

  // --- Punto 3: el crédito del balance es solo del mes ---
  @Test
  public void dadoTareasRealizadasEnElMesCuandoGenerarReporteEntoncesElCreditoEsSoloDelMes() {
    // Given
    List<Usuario> integrantes = givenIntegrantesDelHogar();
    Usuario maria = integrantes.get(0);

    Tarea completada = givenTarea("Limpiar cocina", maria, EstadoTarea.COMPLETADA, 500.0);
    Tarea liquidada = givenTarea("Lavar ropa", maria, EstadoTarea.LIQUIDADA, 300.0);
    Tarea pendiente = givenTarea("Ordenar", maria, EstadoTarea.PENDIENTE, 1000.0);
    Tarea deOtroMes = givenTarea("Pintar", maria, EstadoTarea.COMPLETADA, 2000.0);
    deOtroMes.setFechaRegistro(LocalDateTime.of(2026, 9, 10, 10, 0));

    givenUnHogarConIntegrantesYTareas(
      integrantes,
      List.of(completada, liquidada, pendiente, deOtroMes)
    );

    // When
    ReporteMensual reporte = whenGenerarReporte(mesTest);

    // Then
    FilaBalance balanceMaria = reporte.getBalances().get(0);
    FilaBalance balanceJuan = reporte.getBalances().get(1);
    assertThat(balanceMaria.getCreditoTareas(), equalTo(800.0));
    assertThat(balanceJuan.getCreditoTareas(), equalTo(0.0));
    verify(servicioCreditoTarea, never()).obtenerCreditoAcumuladoPorUsuario(anyLong());
  }

  @Test
  public void dadoUnReporteMensualCuandoExportarCsvEntoncesRetornaStringConFormatoCsv() {
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

  private Tarea givenTarea(String titulo, Usuario realizador, EstadoTarea estado, Double credito) {
    Tarea tarea = new Tarea(titulo);
    tarea.setUsuarioRealizador(realizador);
    tarea.setEstado(estado);
    tarea.setValorCredito(credito);
    tarea.setFechaRegistro(LocalDateTime.of(2026, 10, 20, 9, 0));
    return tarea;
  }

  private void givenUnHogarConIntegrantesYTareas(List<Usuario> integrantes, List<Tarea> tareas) {
    when(session.getAttribute("HOGAR_ID")).thenReturn(HOGAR_ID);
    when(servicioRegistroGasto.obtenerIntegrantes(HOGAR_ID)).thenReturn(integrantes);
    when(servicioRegistroGasto.obtenerTodosLosGastos(HOGAR_ID)).thenReturn(Collections.emptyList());
    when(servicioCreditoTarea.obtenerTodasLasTareas()).thenReturn(tareas);
    when(servicioLiquidacion.obtenerLiquidacionDelMes(any(Hogar.class), anyList()))
      .thenReturn(Collections.emptyList());
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
