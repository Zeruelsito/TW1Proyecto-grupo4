package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Gasto;
import com.tallerwebi.dominio.ServicioRegistroGasto;
import com.tallerwebi.dominio.TipoGasto;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.DescripcionGastoVaciaException;
import com.tallerwebi.dominio.excepcion.MontoGastoInvalidoException;

import java.time.LocalDateTime;
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


@Test
public void dadoElFiltroProximosCuandoConsultoVencimientosEntoncesMuestraLosProximos() {
  List<Gasto> proximos = List.of(new Gasto("G-1", TipoGasto.FIJO, "Luz", 5000.0, "SERVICIOS"));
  when(servicioRegistroGasto.obtenerVencimientosProximos(any(LocalDateTime.class)))
    .thenReturn(proximos);

  ModelAndView mav = controladorRegistroGasto.irAVencimientos("proximos");

  assertThat(mav.getViewName(), equalTo("vencimientos"));
  assertEquals(proximos, mav.getModel().get("vencimientos"));
  assertEquals("proximos", mav.getModel().get("filtro"));
}

@Test
public void dadoElFiltroVencidosCuandoConsultoVencimientosEntoncesMuestraLosVencidos() {
  List<Gasto> vencidos = List.of(new Gasto("G-2", TipoGasto.FIJO, "Gas", 3000.0, "SERVICIOS"));
  when(servicioRegistroGasto.obtenerVencimientosVencidos(any(LocalDateTime.class)))
    .thenReturn(vencidos);

  ModelAndView mav = controladorRegistroGasto.irAVencimientos("vencidos");

  assertEquals(vencidos, mav.getModel().get("vencimientos"));
  assertEquals("vencidos", mav.getModel().get("filtro"));
}

@Test
public void dadoElFiltroTodosCuandoConsultoVencimientosEntoncesMuestraTodosLosPendientes() {
  List<Gasto> todos = List.of(new Gasto("G-3", TipoGasto.FIJO, "Agua", 2000.0, "SERVICIOS"));
  when(servicioRegistroGasto.obtenerVencimientosPendientes()).thenReturn(todos);

  ModelAndView mav = controladorRegistroGasto.irAVencimientos("todos");

  assertEquals(todos, mav.getModel().get("vencimientos"));
  assertEquals("todos", mav.getModel().get("filtro"));
}

@Test
public void dadoUnFiltroInvalidoCuandoConsultoVencimientosEntoncesUsaTodos() {
  ModelAndView mav = controladorRegistroGasto.irAVencimientos("cualquiera");

  assertEquals("todos", mav.getModel().get("filtro"));
}

  // Given/When/Then

  private void givenExistenGastosEnElSistema() {
    Usuario usuario = new Usuario("Juan", "juan@kumo.com", "1234");
    usuario.setId(1L);

    List<Gasto> listaGastos = new ArrayList<>();
    listaGastos.add(
      new Gasto(
        "G001",
        TipoGasto.OCASIONAL,
        "Compras supermercado",
        12000.0,
        usuario,
        "ALIMENTACION"
      )
    );
    when(servicioRegistroGasto.obtenerTodosLosGastos()).thenReturn(listaGastos);
  }

  private Gasto givenUnGastoValido() {
    Usuario usuario = new Usuario("Ezequiel", "ezequiel@kumo.com", "2222");
    usuario.setId(2L);
    return new Gasto(
      "G002",
      TipoGasto.RECURRENTE,
      "Internet y Cable",
      20000.0,
      usuario,
      "SERVICIOS"
    );
  }

  private Gasto givenUnGastoConMontoInvalido() {
    Usuario usuario = new Usuario("Ezequiel", "ezequiel@kumo.com", "2222");
    usuario.setId(2L);
    return new Gasto(
      "G003",
      TipoGasto.OCASIONAL,
      "Compras del chino",
      -500.0,
      usuario,
      "ALIMENTACION"
    );
  }

  private Gasto givenUnGastoConDescripcionVacia() {
    Usuario usuario = new Usuario("Ezequiel", "ezequiel@kumo.com", "2222");
    usuario.setId(2L);
    return new Gasto("G004", TipoGasto.OCASIONAL, "", 15000.0, usuario, "ALIMENTACION");
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
