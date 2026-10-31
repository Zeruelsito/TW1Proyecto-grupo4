package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.tallerwebi.dominio.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

public class ControladorMercadoPagoTest {

  private ControladorMercadoPago controladorMercadoPago;
  private ServicioMercadoPago servicioMercadoPagoMock;
  private ServicioPagoRealizado servicioPagoRealizadoMock;
  private ServicioPagoPendiente servicioPagoPendienteMock;
  private ServicioBajaIntegrante servicioBajaIntegranteMock;
  private MockHttpSession session;

  @BeforeEach
  public void setUp() {
    servicioMercadoPagoMock = mock(ServicioMercadoPago.class);
    servicioPagoRealizadoMock = mock(ServicioPagoRealizado.class);
    servicioPagoPendienteMock = mock(ServicioPagoPendiente.class);

    controladorMercadoPago = new ControladorMercadoPago(servicioMercadoPagoMock);

    // Inyectamos los mocks restantes usando reflexión o simulando la sesión
    // Nota: Como servicioPagoRealizado y servicioPagoPendiente tienen @Autowired,
    // podemos asignarlos manualmente aquí para el test unitario puro.
    setPrivateField(controladorMercadoPago, "servicioPagoRealizado", servicioPagoRealizadoMock);
    setPrivateField(controladorMercadoPago, "servicioPagoPendiente", servicioPagoPendienteMock);

    session = new MockHttpSession();
    setPrivateField(controladorMercadoPago, "session", session);
  }

  @Test
  public void queSePuedaIniciarUnPagoExitosamente() throws Exception {
    Preference mockPreference = mock(Preference.class);
    when(mockPreference.getSandboxInitPoint())
      .thenReturn("https://sandbox.mercadopago.com.ar/checkout/init?pref_id=123");
    when(servicioMercadoPagoMock.crearUrl(eq("Transferencia"), any(BigDecimal.class)))
      .thenReturn(mockPreference);

    ModelAndView mav = controladorMercadoPago.pago("Juan", "Maria", 1000.0);

    assertThat(
      mav.getViewName(),
      equalTo("redirect:https://sandbox.mercadopago.com.ar/checkout/init?pref_id=123")
    );
    assertThat(session.getAttribute("emisorPago"), equalTo("Juan"));
    assertThat(session.getAttribute("receptorPago"), equalTo("Maria"));
    assertThat(session.getAttribute("montoPago"), equalTo(1000.0));

    verify(servicioMercadoPagoMock, times(1)).crearUrl(eq("Transferencia"), any(BigDecimal.class));
  }

  @Test
  public void queManejeErrorMPApiExceptionAlIniciarPago() throws Exception {
    MPApiException mockException = mock(MPApiException.class);
    when(servicioMercadoPagoMock.crearUrl(anyString(), any(BigDecimal.class)))
      .thenThrow(mockException);

    ModelAndView mav = controladorMercadoPago.pago("Juan", "Maria", 1000.0);

    assertThat(mav.getViewName(), equalTo("errorControladorMP"));
    assertThat(mav.getModel().containsKey("error"), is(true));
  }

  @Test
  public void queProceseElPagoExitosoYRegistreEnServicio() throws Exception {
    Hogar hogarMock = new Hogar();
    session.setAttribute("hogarActivoEnSession", hogarMock);
    session.setAttribute("emisorPago", "Juan");
    session.setAttribute("receptorPago", "Maria");
    session.setAttribute("montoPago", 1500.0);

    ModelAndView mav = controladorMercadoPago.pagoExitoso("123456", "approved");

    assertThat(mav.getViewName(), equalTo("succesControladorMP"));
    verify(servicioPagoRealizadoMock, times(1)).registrarPagoExitoso(any(PagoRealizado.class));
  }

  @Test
  public void queProceseUnPagoPendienteYRedirijaALiquidacion() throws Exception {
    Hogar hogarMock = new Hogar();
    session.setAttribute("hogarActivoEnSession", hogarMock);
    session.setAttribute("emisorPago", "Juan");
    session.setAttribute("receptorPago", "Maria");
    session.setAttribute("montoPago", 2000.0);

    ModelAndView mav = controladorMercadoPago.pagoPendiente();

    assertThat(mav.getViewName(), equalTo("redirect:/liquidacion"));
    verify(servicioPagoPendienteMock, times(1)).guardarPendiente(any(PagoPendiente.class));
  }

  @Test
  public void quePermitaConfirmarUnPagoManualYLoConviertaEnRealizado() throws Exception {
    Hogar hogarMock = new Hogar();
    session.setAttribute("hogarActivoEnSession", hogarMock);

    PagoPendiente pendienteMock = new PagoPendiente("Juan", "Maria", 1000.0, hogarMock);
    when(servicioPagoPendienteMock.buscarPorId(1L)).thenReturn(pendienteMock);

    ModelAndView mav = controladorMercadoPago.confirmarPagoManual(1L);

    assertThat(mav.getViewName(), equalTo("redirect:/liquidacion"));
    verify(servicioPagoPendienteMock, times(1)).buscarPorId(1L);
    verify(servicioPagoRealizadoMock, times(1)).registrarPagoExitoso(any(PagoRealizado.class));
    verify(servicioPagoPendienteMock, times(1)).eliminarPorId(1L);
  }

  @Test
  public void quePermitaDevolverUnPagoASugeridasEliminandoElPendiente() throws Exception {
    Hogar hogarMock = new Hogar();
    session.setAttribute("hogarActivoEnSession", hogarMock);

    ModelAndView mav = controladorMercadoPago.devolverASugeridas(5L);

    assertThat(mav.getViewName(), equalTo("redirect:/liquidacion"));
    verify(servicioPagoPendienteMock, times(1)).eliminarPorId(5L);
  }

  @Test
  public void queRecibaUnWebhookDeMercadoPagoCorrectamente() throws Exception {
    Map<String, Object> payload = new HashMap<>();
    payload.put("type", "payment");
    payload.put("action", "payment.created");

    Map<String, Object> data = new HashMap<>();
    data.put("id", "123456789");
    payload.put("data", data);

    // Interceptamos el 'new PaymentClient()' para evitar llamadas reales a la API
    try (
      org.mockito.MockedConstruction<com.mercadopago.client.payment.PaymentClient> mocked =
        org.mockito.Mockito.mockConstruction(
          com.mercadopago.client.payment.PaymentClient.class,
          (mock, context) -> {
            Payment mockPayment = mock(Payment.class);
            when(mockPayment.getStatus()).thenReturn("approved");
            when(mock.get(anyLong())).thenReturn(mockPayment);
          }
        )
    ) {
      var response = controladorMercadoPago.recibirWebhook(payload);

      assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
      assertThat(response.getBody(), equalTo("OK"));
    }
  }


  
  @Test
  public void queUnPagoDeBajaValidoRedirijaAMercadoPagoYGuardeElUsuarioEnSesion() throws Exception {
    SimulacionBaja simulacion = mock(SimulacionBaja.class);
    when(simulacion.tieneTransferenciaPendiente("Juan", "Maria", 1000.0)).thenReturn(true);
    when(servicioBajaIntegranteMock.simularBaja(7L)).thenReturn(simulacion);
    Preference mockPreference = mock(Preference.class);
    when(mockPreference.getInitPoint()).thenReturn("https://sandbox.mp/pref");
    when(servicioMercadoPagoMock.crearUrl(eq("Transferencia"), any(BigDecimal.class)))
      .thenReturn(mockPreference);
 
    ModelAndView mav = controladorMercadoPago.pagoBaja(7L, "Juan", "Maria", 1000.0);
 
    assertThat(mav.getViewName(), equalTo("redirect:https://sandbox.mp/pref"));
    assertThat(session.getAttribute("bajaUsuarioIdPago"), equalTo(7L));
  }
 
  @Test
  public void queUnPagoDeBajaQueNoCorrespondeNoVayaAMercadoPago() throws Exception {
    SimulacionBaja simulacion = mock(SimulacionBaja.class);
    when(simulacion.tieneTransferenciaPendiente(anyString(), anyString(), anyDouble())).thenReturn(false);
    when(servicioBajaIntegranteMock.simularBaja(7L)).thenReturn(simulacion);
 
    ModelAndView mav = controladorMercadoPago.pagoBaja(7L, "Juan", "Maria", 1000.0);
 
    assertThat(mav.getViewName(), equalTo("redirect:/hogar/baja/simular?usuarioId=7"));
    verify(servicioMercadoPagoMock, never()).crearUrl(anyString(), any(BigDecimal.class));
  }
 
  @Test
  public void queUnPagoExitosoDeBajaSeRegistreEnLaBajaYVuelvaALaSimulacion() throws Exception {
    session.setAttribute("bajaUsuarioIdPago", 7L);
    session.setAttribute("emisorPago", "Juan");
    session.setAttribute("receptorPago", "Maria");
    session.setAttribute("montoPago", 1000.0);
 
    ModelAndView mav = controladorMercadoPago.pagoExitoso("123", "approved");
 
    assertThat(
      mav.getViewName(),
      equalTo("redirect:/hogar/baja/simular?usuarioId=7&pagoRegistrado=true")
    );
    verify(servicioBajaIntegranteMock, times(1)).registrarPago(7L, "Juan", "Maria", 1000.0);
    verify(servicioPagoRealizadoMock, never()).registrarPagoExitoso(any(PagoRealizado.class));
    assertThat(session.getAttribute("bajaUsuarioIdPago"), is(nullValue()));
  }
 
  @Test
  public void queUnPagoDeBajaNoAprobadoNoSeRegistre() throws Exception {
    session.setAttribute("bajaUsuarioIdPago", 7L);
    session.setAttribute("emisorPago", "Juan");
    session.setAttribute("receptorPago", "Maria");
    session.setAttribute("montoPago", 1000.0);
 
    ModelAndView mav = controladorMercadoPago.pagoExitoso("987654", "rejected");
 
    assertThat(mav.getViewName(), equalTo("redirect:/hogar/baja/simular?usuarioId=7"));
    verify(servicioBajaIntegranteMock, never())
      .registrarPago(anyLong(), anyString(), anyString(), anyDouble());
  }
 
  @Test
  public void queUnPagoNormalLimpieElUsuarioDeBajaDeLaSesion() throws Exception {
    session.setAttribute("bajaUsuarioIdPago", 7L);
    Preference mockPreference = mock(Preference.class);
    when(mockPreference.getSandboxInitPoint()).thenReturn("https://sandbox.mp/pref");
    when(servicioMercadoPagoMock.crearUrl(eq("Transferencia"), any(BigDecimal.class)))
      .thenReturn(mockPreference);
 
    controladorMercadoPago.pago("Juan", "Maria", 1000.0);
 
    assertThat(session.getAttribute("bajaUsuarioIdPago"), is(nullValue()));
  }
 

  // Método auxiliar para inyectar campos privados (simulando @Autowired sin levantar Spring Context)
  private void setPrivateField(Object target, String fieldName, Object value) {
    try {
      java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
