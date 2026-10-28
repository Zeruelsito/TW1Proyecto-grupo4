package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.ServicioBajaIntegrante;
import com.tallerwebi.dominio.SimulacionBaja;
import com.tallerwebi.dominio.excepcion.IntegranteConSaldoPendienteException;
import com.tallerwebi.dominio.excepcion.PagoNoCorrespondeALiquidacionException;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.excepcion.UsuarioYaDadoDeBajaException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorBajaIntegranteTest {

  private ControladorBajaIntegrante controladorBaja;
  private ServicioBajaIntegrante servicioBajaMock;

  @BeforeEach
  public void init() {
    servicioBajaMock = mock(ServicioBajaIntegrante.class);
    when(servicioBajaMock.obtenerIntegrantesActivos()).thenReturn(List.of());
    controladorBaja = new ControladorBajaIntegrante(servicioBajaMock);
  }

  @Test
  public void irABajaDeberiaMostrarLaVista() {
    // ejecucion
    ModelAndView modelAndView = controladorBaja.irABaja(null);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("baja-integrante"));
  }

  @Test
  public void simularDeberiaMostrarLaSimulacion() {
    // preparacion
    SimulacionBaja simulacion = mock(SimulacionBaja.class);
    when(servicioBajaMock.simularBaja(1L)).thenReturn(simulacion);

    // ejecucion
    ModelAndView modelAndView = controladorBaja.simular(1L, null);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("baja-integrante"));
    assertThat(modelAndView.getModel().get("simulacion"), equalTo(simulacion));
  }

  @Test
  public void simularConUsuarioInexistenteDeberiaMostrarError() {
    // preparacion
    when(servicioBajaMock.simularBaja(99L)).thenThrow(new UsuarioNoEncontrado());

    // ejecucion
    ModelAndView modelAndView = controladorBaja.simular(99L, null);

    // validacion
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("El integrante seleccionado no existe.")
    );
  }

  @Test
  public void confirmarDeberiaDarDeBajaYRedirigir() {
    // ejecucion
    ModelAndView modelAndView = controladorBaja.confirmar(1L);

    // validacion
    assertThat(
      modelAndView.getViewName(),
      equalToIgnoringCase("redirect:/hogar/baja?confirmada=true")
    );
    verify(servicioBajaMock, times(1)).confirmarBaja(1L);
  }

  @Test
  public void confirmarUnUsuarioYaDadoDeBajaDeberiaMostrarError() {
    // preparacion
    doThrow(new UsuarioYaDadoDeBajaException()).when(servicioBajaMock).confirmarBaja(1L);

    // ejecucion
    ModelAndView modelAndView = controladorBaja.confirmar(1L);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("baja-integrante"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Ese integrante ya fue dado de baja.")
    );
  }

  @Test
  public void confirmarConSaldoPendienteDeberiaMostrarErrorYLaSimulacion() {
    // preparacion
    SimulacionBaja simulacion = mock(SimulacionBaja.class);
    when(servicioBajaMock.simularBaja(1L)).thenReturn(simulacion);
    doThrow(new IntegranteConSaldoPendienteException()).when(servicioBajaMock).confirmarBaja(1L);

    // ejecucion
    ModelAndView modelAndView = controladorBaja.confirmar(1L);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("baja-integrante"));
    assertThat(modelAndView.getModel().get("simulacion"), equalTo(simulacion));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("No se puede dar de baja: el integrante todavía tiene saldo pendiente.")
    );
  }

  @Test
  public void registrarPagoDeberiaRegistrarloYVolverALaSimulacion() {
    // ejecucion
    ModelAndView modelAndView = controladorBaja.registrarPago(1L, "Juan", "Maria", 1000.0);

    // validacion
    verify(servicioBajaMock).registrarPago(1L, "Juan", "Maria", 1000.0);
    assertThat(
      modelAndView.getViewName(),
      equalToIgnoringCase("redirect:/hogar/baja/simular?usuarioId=1&pagoRegistrado=true")
    );
  }

  @Test
  public void registrarPagoQueNoCorrespondeDeberiaMostrarError() {
    // preparacion
    doThrow(new PagoNoCorrespondeALiquidacionException())
      .when(servicioBajaMock)
      .registrarPago(1L, "Juan", "Maria", 5.0);

    // ejecucion
    ModelAndView modelAndView = controladorBaja.registrarPago(1L, "Juan", "Maria", 5.0);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("baja-integrante"));
    assertThat(
      modelAndView.getModel().get("error"),
      equalTo("Ese pago no corresponde a la liquidación pendiente o ya fue registrado.")
    );
  }
}
