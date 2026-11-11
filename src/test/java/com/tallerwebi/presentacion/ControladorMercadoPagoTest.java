package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.tallerwebi.dominio.*;
import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.ModelAndView;

public class ControladorMercadoPagoTest {

  private ControladorMercadoPago controladorMercadoPago;
  private ServicioMercadoPago servicioMercadoPagoMock;
  private ServicioPagoRealizado servicioPagoRealizadoMock;
  private ServicioPagoPendiente servicioPagoPendienteMock;
  private ServicioBajaIntegrante servicioBajaIntegranteMock;
  private HttpSession sessionMock;

  private Hogar hogarTest;
  private Usuario emisorTest;
  private Usuario receptorTest;

  @BeforeEach
  public void setUp() {
    servicioMercadoPagoMock = Mockito.mock(ServicioMercadoPago.class);
    servicioPagoRealizadoMock = Mockito.mock(ServicioPagoRealizado.class);
    servicioPagoPendienteMock = Mockito.mock(ServicioPagoPendiente.class);
    servicioBajaIntegranteMock = Mockito.mock(ServicioBajaIntegrante.class);
    sessionMock = Mockito.mock(HttpSession.class);

    controladorMercadoPago = new ControladorMercadoPago(servicioMercadoPagoMock);

    // Inyectamos manualmente los mocks mediante reflexión o setters si fuera necesario,
    // pero como el controlador usa @Autowired en campos privados, los asignamos con Mockito si disponemos de soporte,
    // o bien podemos instanciar un helper o usar ReflectionTestUtils. Aquí usaremos un enfoque limpio con ReflectionTestUtils o asignación si la clase lo permite.
    org.springframework.test.util.ReflectionTestUtils.setField(
      controladorMercadoPago,
      "servicioPagoRealizado",
      servicioPagoRealizadoMock
    );
    org.springframework.test.util.ReflectionTestUtils.setField(
      controladorMercadoPago,
      "servicioPagoPendiente",
      servicioPagoPendienteMock
    );
    org.springframework.test.util.ReflectionTestUtils.setField(
      controladorMercadoPago,
      "servicioBajaIntegrante",
      servicioBajaIntegranteMock
    );
    org.springframework.test.util.ReflectionTestUtils.setField(
      controladorMercadoPago,
      "session",
      sessionMock
    );

    // Datos base
    emisorTest = new Usuario();
    emisorTest.setId(1L);
    emisorTest.setNombre("Carlos");

    receptorTest = new Usuario();
    receptorTest.setId(2L);
    receptorTest.setNombre("Juan");

    hogarTest = new Hogar();
    hogarTest.setId(10L);
    hogarTest.setIntegrantes(Arrays.asList(emisorTest, receptorTest));
  }

  @Test
  public void queSePuedaIniciarUnPagoExitosamenteYRetorneRedireccionSandbox() throws Exception {
    // Given
    Preference preferenceMock = Mockito.mock(Preference.class);
    when(preferenceMock.getSandboxInitPoint())
      .thenReturn("https://sandbox.mercadopago.com/checkout");
    when(servicioMercadoPagoMock.crearUrl(anyString(), any())).thenReturn(preferenceMock);

    // When
    ModelAndView mav = controladorMercadoPago.pago("Carlos", "Juan", 1500.0);

    // Then
    assertThat(mav, notNullValue());
    assertThat(mav.getViewName(), is("redirect:https://sandbox.mercadopago.com/checkout"));
    verify(sessionMock, times(1)).setAttribute("emisorPago", "Carlos");
    verify(sessionMock, times(1)).setAttribute("receptorPago", "Juan");
    verify(sessionMock, times(1)).setAttribute("montoPago", 1500.0);
  }

  @Test
  public void queManejeExcepcionMPApiExceptionAlIniciarPago() throws Exception {
    // Given
    when(servicioMercadoPagoMock.crearUrl(anyString(), any()))
      .thenThrow(Mockito.mock(MPApiException.class));

    // When
    ModelAndView mav = controladorMercadoPago.pago("Carlos", "Juan", 1500.0);

    // Then
    assertThat(mav, notNullValue());
    assertThat(
      mav.getViewName(),
      is("redirect:/liquidacion?error=Error+al+conectar+con+Mercado+Pago")
    );
  }

  @Test
  public void queManejeExcepcionMPExceptionGeneralAlIniciarPago() throws Exception {
    // Given
    when(servicioMercadoPagoMock.crearUrl(anyString(), any()))
      .thenThrow(new MPException("Error general"));

    // When
    ModelAndView mav = controladorMercadoPago.pago("Carlos", "Juan", 1500.0);

    // Then
    assertThat(mav, notNullValue());
    assertThat(
      mav.getViewName(),
      is("redirect:/liquidacion?error=Error+al+conectar+con+Mercado+Pago")
    );
  }

  @Test
  public void queProCESEUnPagoHogarNormalExitosoEnPagoExitoso() {
    // Given
    when(sessionMock.getAttribute("bajaUsuarioIdPago")).thenReturn(null);
    when(sessionMock.getAttribute("hogarActivoEnSession")).thenReturn(hogarTest);
    when(sessionMock.getAttribute("montoPago")).thenReturn(1500.0);
    when(sessionMock.getAttribute("emisorPago")).thenReturn("Carlos");
    when(sessionMock.getAttribute("receptorPago")).thenReturn("Juan");

    // When
    ModelAndView mav = controladorMercadoPago.pagoExitoso("approved");

    // Then
    assertThat(mav, notNullValue());
    assertThat(mav.getViewName(), is("redirect:/liquidacion?exito=El+pago+se+realizo+con+exito"));
    verify(servicioPagoRealizadoMock, times(1)).registrarPagoExitoso(any(PagoRealizado.class));
  }

  @Test
  public void queGuardePagoPendienteCorrectamenteEnEndpointPending() {
    // Given
    when(sessionMock.getAttribute("hogarActivoEnSession")).thenReturn(hogarTest);
    when(sessionMock.getAttribute("emisorPago")).thenReturn("Carlos");
    when(sessionMock.getAttribute("receptorPago")).thenReturn("Juan");
    when(sessionMock.getAttribute("montoPago")).thenReturn(1500.0);

    // When
    ModelAndView mav = controladorMercadoPago.pagoPendiente();

    // Then
    assertThat(mav, notNullValue());
    assertThat(mav.getViewName(), is("redirect:/liquidacion"));
    verify(servicioPagoPendienteMock, times(1)).guardarPendiente(any(PagoPendiente.class));
  }

  @Test
  public void queConfirmePagoManualYEliminePendiente() {
    // Given
    Long pendienteId = 55L;
    PagoPendiente pendienteMock = new PagoPendiente(emisorTest, receptorTest, 1500.0, hogarTest);
    pendienteMock.setId(pendienteId);

    when(sessionMock.getAttribute("hogarActivoEnSession")).thenReturn(hogarTest);
    when(servicioPagoPendienteMock.buscarPorId(pendienteId)).thenReturn(pendienteMock);

    // When
    ModelAndView mav = controladorMercadoPago.confirmarPagoManual(pendienteId);

    // Then
    assertThat(mav, notNullValue());
    assertThat(mav.getViewName(), is("redirect:/liquidacion"));
    verify(servicioPagoRealizadoMock, times(1)).registrarPagoExitoso(any(PagoRealizado.class));
    verify(servicioPagoPendienteMock, times(1)).eliminarPorId(pendienteId);
  }

  @Test
  public void queDevuelvaASugeridasEliminandoElPendientePorId() {
    // Given
    Long pendienteId = 55L;
    when(sessionMock.getAttribute("hogarActivoEnSession")).thenReturn(hogarTest);

    // When
    ModelAndView mav = controladorMercadoPago.devolverASugeridas(pendienteId);

    // Then
    assertThat(mav, notNullValue());
    assertThat(mav.getViewName(), is("redirect:/liquidacion"));
    verify(servicioPagoPendienteMock, times(1)).eliminarPorId(pendienteId);
  }

  @Test
  public void pagoBajaValidoDeberiaIrAMercadoPagoYGuardarLaBajaEnSesion() throws Exception {
    dadoQueLaBajaTieneLaTransferencia(true);
    Preference preferenceMock = Mockito.mock(Preference.class);
    when(preferenceMock.getSandboxInitPoint()).thenReturn("https://sandbox.mp/checkout");
    when(servicioMercadoPagoMock.crearUrl(anyString(), any())).thenReturn(preferenceMock);

    ModelAndView mav = controladorMercadoPago.pagoBaja(
      new DatosPagoBaja(5L, "Carlos", "Juan", 1500.0)
    );

    assertThat(mav.getViewName(), is("redirect:https://sandbox.mp/checkout"));
    verify(sessionMock).setAttribute("bajaUsuarioIdPago", 5L);
  }

  @Test
  public void pagoBajaConTransferenciaQueNoCorrespondeNoDeberiaIrAMercadoPago() throws Exception {
    dadoQueLaBajaTieneLaTransferencia(false);

    ModelAndView mav = controladorMercadoPago.pagoBaja(
      new DatosPagoBaja(5L, "Carlos", "Juan", 1500.0)
    );

    assertThat(mav.getViewName(), is("redirect:/hogar/baja/simular?usuarioId=5"));
    verify(servicioMercadoPagoMock, never()).crearUrl(anyString(), any());
  }

  @Test
  public void pagoBajaAprobadoDeberiaRegistrarElPago() {
    when(sessionMock.getAttribute("bajaUsuarioIdPago")).thenReturn(5L);
    when(sessionMock.getAttribute("hogarActivoEnSession")).thenReturn(hogarTest);
    when(sessionMock.getAttribute("emisorPago")).thenReturn("Carlos");
    when(sessionMock.getAttribute("receptorPago")).thenReturn("Juan");
    when(sessionMock.getAttribute("montoPago")).thenReturn(1500.0);

    controladorMercadoPago.pagoExitoso("approved");

    verify(servicioBajaIntegranteMock).registrarPago(5L, emisorTest, receptorTest, 1500.0);
  }

  private void dadoQueLaBajaTieneLaTransferencia(boolean laTiene) {
    SimulacionBaja simulacionMock = Mockito.mock(SimulacionBaja.class);
    when(simulacionMock.tieneTransferenciaPendiente(emisorTest, receptorTest, 1500.0))
      .thenReturn(laTiene);
    when(sessionMock.getAttribute("hogarActivoEnSession")).thenReturn(hogarTest);
    when(servicioBajaIntegranteMock.simularBaja(5L)).thenReturn(simulacionMock);
  }
}
