package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioReporteTest {

  private ServicioReporte servicioReporte;
  private ServicioRegistroGasto servicioRegistroGastoMock;
  private ServicioCreditoTarea servicioCreditoTareaMock;
  private ServicioLiquidacion servicioLiquidacionMock;

  @BeforeEach
  public void init() {
    servicioRegistroGastoMock = mock(ServicioRegistroGasto.class);
    servicioCreditoTareaMock = mock(ServicioCreditoTarea.class);
    servicioLiquidacionMock = mock(ServicioLiquidacion.class);
    servicioReporte =
      new ServicioReporteImpl(
        servicioRegistroGastoMock,
        servicioCreditoTareaMock,
        servicioLiquidacionMock
      );
  }

  @Test
  public void generarReporteDeberiaTraerSoloLosGastosDelMes() {
    // preparacion
    Gasto gastoSeptiembre = new Gasto(TipoGasto.FIJO, "Luz", 1000.0, 1L, "SERVICIOS");
    gastoSeptiembre.setFecha(LocalDateTime.of(2026, 9, 10, 12, 0));
    Gasto gastoAgosto = new Gasto(TipoGasto.FIJO, "Gas", 500.0, 1L, "SERVICIOS");
    gastoAgosto.setFecha(LocalDateTime.of(2026, 8, 10, 12, 0));

    when(servicioRegistroGastoMock.obtenerIntegrantes()).thenReturn(List.of());
    when(servicioRegistroGastoMock.obtenerTodosLosGastos())
      .thenReturn(List.of(gastoSeptiembre, gastoAgosto));
    when(servicioCreditoTareaMock.obtenerTodasLasTareas()).thenReturn(List.of());
    when(servicioLiquidacionMock.obtenerLiquidacionDelMes(any(Hogar.class), anyList()))
      .thenReturn(List.of());

    // ejecucion
    ReporteMensual reporte = servicioReporte.generarReporte(YearMonth.of(2026, 9));

    // validacion
    assertThat(reporte.getGastos().size(), equalTo(1));
    assertThat(reporte.getGastos().get(0).getDescripcion(), equalTo("Luz"));
  }

  @Test
  public void exportarCsvDeberiaTenerLasSeccionesDelReporte() {
    // preparacion
    ReporteMensual reporte = new ReporteMensual(
      YearMonth.of(2026, 9),
      List.of(),
      List.of(),
      List.of(),
      List.of(),
      Map.of()
    );

    // ejecucion
    String csv = servicioReporte.exportarCsv(reporte);

    // validacion
    assertThat(csv, containsString("GASTOS"));
    assertThat(csv, containsString("TAREAS"));
    assertThat(csv, containsString("BALANCES POR INTEGRANTE"));
    assertThat(csv, containsString("LIQUIDACION SUGERIDA"));
  }

  @Test
  public void exportarCsvDeberiaMostrarElGastoConElNombreDelPagador() {
    // preparacion
    Gasto gasto = new Gasto(TipoGasto.FIJO, "Luz", 1500.0, 1L, "SERVICIOS");
    ReporteMensual reporte = new ReporteMensual(
      YearMonth.of(2026, 9),
      List.of(gasto),
      List.of(),
      List.of(),
      List.of(),
      Map.of(1L, "Juan")
    );

    // ejecucion
    String csv = servicioReporte.exportarCsv(reporte);

    // validacion
    assertThat(csv, containsString("Luz;SERVICIOS;Fijo;Juan;1500,00"));
  }
}
