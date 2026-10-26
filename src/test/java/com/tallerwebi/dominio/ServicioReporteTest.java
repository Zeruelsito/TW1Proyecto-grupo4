package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpSession;
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
  private ServicioHogar servicioHogarMock;
  private HttpSession sessionMock;

  private static final Long HOGAR_ID = 1L;

  @BeforeEach
  public void init() {
    servicioRegistroGastoMock = mock(ServicioRegistroGasto.class);
    servicioCreditoTareaMock = mock(ServicioCreditoTarea.class);
    servicioLiquidacionMock = mock(ServicioLiquidacion.class);
    servicioHogarMock = mock(ServicioHogar.class);
    sessionMock = mock(HttpSession.class);

    servicioReporte =
      new ServicioReporteImpl(
        servicioRegistroGastoMock,
        servicioCreditoTareaMock,
        servicioLiquidacionMock,
        servicioHogarMock,
        sessionMock
      );
  }

  @Test
  public void generarReporteDeberiaTraerSoloLosGastosDelMes() {
    // preparacion[cite: 6]
    Usuario usuario = new Usuario("Juan", "juan@kumo.com", "1234");
    usuario.setId(1L);

    Hogar hogar = new Hogar("Casa Test");
    hogar.setId(HOGAR_ID);

    when(sessionMock.getAttribute("HOGAR_ID")).thenReturn(HOGAR_ID);
    when(servicioHogarMock.obtenerHogar()).thenReturn(hogar);

    Gasto gastoSeptiembre = new Gasto("G001", TipoGasto.FIJO, "Luz", 1000.0, usuario, "SERVICIOS");
    gastoSeptiembre.setFecha(LocalDateTime.of(2026, 9, 10, 12, 0));
    Gasto gastoAgosto = new Gasto("G002", TipoGasto.FIJO, "Gas", 500.0, usuario, "SERVICIOS");
    gastoAgosto.setFecha(LocalDateTime.of(2026, 8, 10, 12, 0));

    when(servicioRegistroGastoMock.obtenerIntegrantes(anyLong())).thenReturn(List.of());
    when(servicioRegistroGastoMock.obtenerTodosLosGastos(anyLong()))
      .thenReturn(List.of(gastoSeptiembre, gastoAgosto));
    when(servicioCreditoTareaMock.obtenerTodasLasTareas()).thenReturn(List.of());
    when(servicioLiquidacionMock.obtenerLiquidacionDelMes(any(Hogar.class), anyList()))
      .thenReturn(List.of());

    // ejecucion[cite: 6]
    ReporteMensual reporte = servicioReporte.generarReporte(YearMonth.of(2026, 9));

    // validacion[cite: 6]
    assertThat(reporte.getGastos().size(), equalTo(1));
    assertThat(reporte.getGastos().get(0).getDescripcion(), equalTo("Luz"));
  }

  @Test
  public void exportarCsvDeberiaTenerLasSeccionesDelReporte() {
    // preparacion[cite: 6]
    ReporteMensual reporte = new ReporteMensual(
      YearMonth.of(2026, 9),
      List.of(),
      List.of(),
      List.of(),
      List.of(),
      Map.of()
    );

    // ejecucion[cite: 6]
    String csv = servicioReporte.exportarCsv(reporte);

    // validacion[cite: 6]
    assertThat(csv, containsString("GASTOS"));
    assertThat(csv, containsString("TAREAS"));
    assertThat(csv, containsString("BALANCES POR INTEGRANTE"));
    assertThat(csv, containsString("LIQUIDACION SUGERIDA"));
  }

  @Test
  public void exportarCsvDeberiaMostrarElGastoConElNombreDelPagador() {
    // preparacion[cite: 6]
    Usuario usuario = new Usuario("Juan", "juan@kumo.com", "1234");
    usuario.setId(1L);

    Gasto gasto = new Gasto("G001", TipoGasto.FIJO, "Luz", 1500.0, usuario, "SERVICIOS");
    ReporteMensual reporte = new ReporteMensual(
      YearMonth.of(2026, 9),
      List.of(gasto),
      List.of(),
      List.of(),
      List.of(),
      Map.of(1L, "Juan")
    );

    // ejecucion[cite: 6]
    String csv = servicioReporte.exportarCsv(reporte);

    // validacion[cite: 6]
    assertThat(csv, containsString("Luz;SERVICIOS;Fijo;Juan;1500,00"));
  }
}
