package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.ReporteMensual;
import com.tallerwebi.dominio.ServicioReporte;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.YearMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

public class ControladorReporteTest {

  private ControladorReporte controladorReporte;
  private ServicioReporte servicioReporteMock;
  private ReporteMensual reporteMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;

  @BeforeEach
  public void init() {
    servicioReporteMock = mock(ServicioReporte.class);
    reporteMock = mock(ReporteMensual.class);
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    when(requestMock.getSession()).thenReturn(sessionMock);
    controladorReporte = new ControladorReporte(servicioReporteMock);
  }

  @Test
  public void verReporteSiEsAdminDeberiaMostrarLaVistaReporte() {
    // preparacion
    when(sessionMock.getAttribute("ROL")).thenReturn("ADMIN");
    when(servicioReporteMock.generarReporte(YearMonth.of(2026, 9))).thenReturn(reporteMock);

    // ejecucion
    ModelAndView modelAndView = controladorReporte.verReporte("2026-09", requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("reporte"));
    assertThat(modelAndView.getModel().get("reporte"), equalTo(reporteMock));
  }

  @Test
  public void verReporteSiNoEsAdminDeberiaRedirigirAHome() {
    // preparacion
    when(sessionMock.getAttribute("ROL")).thenReturn("USER");

    // ejecucion
    ModelAndView modelAndView = controladorReporte.verReporte("2026-09", requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
  }

  @Test
  public void verReporteSinSesionDeberiaRedirigirALogin() {
    // ejecucion
    ModelAndView modelAndView = controladorReporte.verReporte("2026-09", requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void exportarSiEsAdminDeberiaDescargarElCsv() {
    // preparacion
    when(sessionMock.getAttribute("ROL")).thenReturn("ADMIN");
    when(servicioReporteMock.generarReporte(YearMonth.of(2026, 9))).thenReturn(reporteMock);
    when(servicioReporteMock.exportarCsv(reporteMock)).thenReturn("Reporte mensual;2026-09");

    // ejecucion
    ResponseEntity<byte[]> respuesta = controladorReporte.exportarCsv("2026-09", requestMock);

    // validacion
    assertThat(respuesta.getStatusCode().value(), equalTo(200));
    verify(servicioReporteMock, times(1)).exportarCsv(reporteMock);
  }

  @Test
  public void exportarSiNoEsAdminNoDeberiaGenerarElCsv() {
    // preparacion
    when(sessionMock.getAttribute("ROL")).thenReturn("USER");

    // ejecucion
    ResponseEntity<byte[]> respuesta = controladorReporte.exportarCsv("2026-09", requestMock);

    // validacion
    assertThat(respuesta.getStatusCode().value(), equalTo(302));
    verify(servicioReporteMock, times(0)).exportarCsv(reporteMock);
  }
}
