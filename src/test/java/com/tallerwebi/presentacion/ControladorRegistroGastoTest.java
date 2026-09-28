package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Gasto;
import com.tallerwebi.dominio.ServicioRegistroGasto;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.DescripcionGastoVaciaException;
import com.tallerwebi.dominio.excepcion.MontoGastoInvalidoException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorRegistroGastoTest {

  private ServicioRegistroGasto servicioRegistroGasto;
  private ControladorRegistroGasto controladorRegistroGasto;

  @BeforeEach
  public void init() {
    servicioRegistroGasto = mock(ServicioRegistroGasto.class);
    controladorRegistroGasto = new ControladorRegistroGasto(servicioRegistroGasto);
  }

  @Test
  public void dadoQueExistenGastosRegistradosCuandoConsultoLaVistaGastosEntoncesDevuelveLaVistaConLaLista() {
    // given
    givenExistenGastosEnElSistema();

    // when
    ModelAndView mav = whenIrAGastos();

    // then
    thenLaVistaEs("gastos", mav);
    thenElModeloContieneLaListaYUnObjetoGasto(mav);
  }

  @Test
  public void dadoUnGastoValidoCuandoSeGuardaEntoncesRedirigeAGastos() {
    // given
    Gasto gastoValido = givenUnGastoValido();

    // when
    ModelAndView mav = whenRegistroUn(gastoValido);

    // then
    thenSeLlamaAlServicioYRedirige(gastoValido, mav);
  }

  @Test
  public void dadoUnGastoSinDescripcionSeMuestraAlUsuarioUnMensajeDeError() {
    // given
    Gasto gastoInvalido = givenUnGastoConDescripcionVacia();
    doThrow(new DescripcionGastoVaciaException())
      .when(servicioRegistroGasto)
      .registrarGasto(gastoInvalido);

    // when
    ModelAndView mav = whenRegistroUn(gastoInvalido);

    // then
    thenLaVistaEs("gastos", mav);
    thenElMensajeDeErrorEs("La descripción del gasto no puede estar vacía.", mav);
  }

  @Test
  public void dadoUnGastoConMontoInvalidoSeMuestraAlUsuarioUnMensajeDeError() {
    // given
    Gasto gastoInvalido = givenUnGastoConMontoInvalido();
    doThrow(new MontoGastoInvalidoException())
      .when(servicioRegistroGasto)
      .registrarGasto(gastoInvalido);

    // when
    ModelAndView mav = whenRegistroUn(gastoInvalido);

    // then
    thenLaVistaEs("gastos", mav);
    thenElMensajeDeErrorEs("El monto del gasto debe ser mayor a cero.", mav);
  }

  // Given/When/Then

  private void givenExistenGastosEnElSistema() {
    List<Gasto> listaGastos = new ArrayList<>();
    listaGastos.add(new Gasto("Compras supermercado", 12000.0, 1L, "ALIMENTACION"));
    when(servicioRegistroGasto.obtenerTodosLosGastos()).thenReturn(listaGastos);
  }

  private Gasto givenUnGastoValido() {
    return new Gasto("Internet y Cable", 20000.0, 2L, "SERVICIOS");
  }

  private Gasto givenUnGastoConMontoInvalido() {
    return new Gasto("Compras del chino", -500.0, 2L, "ALIMENTACION");
  }

  private Gasto givenUnGastoConDescripcionVacia() {
    return new Gasto("", 15000.0, 2L, "ALIMENTACION");
  }

  private ModelAndView whenIrAGastos() {
    return controladorRegistroGasto.irAGastos();
  }

  private ModelAndView whenRegistroUn(Gasto gasto) {
    return controladorRegistroGasto.registrarGasto(gasto);
  }

  private void thenLaVistaEs(String nombreEsperadoDeVista, ModelAndView mav) {
    assertThat(mav.getViewName(), equalTo(nombreEsperadoDeVista));
  }

  private void thenElModeloContieneLaListaYUnObjetoGasto(ModelAndView mav) {
    assertThat(mav.getModel().get("listaGastos"), is(notNullValue()));
    assertThat(mav.getModel().get("gasto"), is(notNullValue()));
  }

  private void thenSeLlamaAlServicioYRedirige(Gasto gastoValido, ModelAndView mav) {
    verify(servicioRegistroGasto).registrarGasto(gastoValido);
    assertThat(mav.getViewName(), equalTo("redirect:/gastos"));
  }

  private void thenElMensajeDeErrorEs(String mensajeErrorEsperado, ModelAndView mav) {
    String mensajeError = (String) mav.getModel().get("mensajeError");
    assertThat(mensajeError, equalTo(mensajeErrorEsperado));
  }
}
