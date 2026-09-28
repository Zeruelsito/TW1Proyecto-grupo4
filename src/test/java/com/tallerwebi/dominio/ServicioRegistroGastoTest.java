package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tallerwebi.dominio.excepcion.DescripcionGastoVaciaException;
import com.tallerwebi.dominio.excepcion.MontoGastoInvalidoException;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ServicioRegistroGastoTest {

  ServicioRegistroGasto servicioRegistroGasto = new ServicioRegistroGastoImpl();

  @Test
  public void dadoUnGastoValidoCuandoLoRegistroEntoncesSeGuardaEnLaLista() {
    //preparacion - given
    Gasto gastoValido = givenUnGastoValido();
    //ejecucion - when
    whenRegistroUn(gastoValido);
    //validacion - then
    thenElGastoSeGuardaEnLaLista(gastoValido);
  }

  @Test
  public void dadoUnGastoConMontoInvalidoCuandoLoRegistroEntoncesLanzaExcepcion() {
    // given
    Gasto gastoConMontoInvalido = givenUnGastoConMontoInvalido();
    // when - then
    assertThrows(MontoGastoInvalidoException.class, () -> whenRegistroUn(gastoConMontoInvalido));
  }

  @Test
  public void dadoUnGastoConDescripcionVaciaCuandoLoRegistroEntoncesLanzaExcepcion() {
    // given
    Gasto gastoConDescripcionVacia = givenUnGastoConDescripcionVacia();
    // when - then
    assertThrows(
      DescripcionGastoVaciaException.class,
      () -> whenRegistroUn(gastoConDescripcionVacia)
    );
  }

  @Test
  public void debeObtenerLaListaDeIntegrantesCorrectamente() {
    List<Usuario> integrantes = servicioRegistroGasto.obtenerIntegrantes();

    assertThat(integrantes, hasSize(4));
    assertThat(integrantes.get(0).getEmail(), containsString("macarena"));
  }

  @Test
  public void debeSugerirComoProximoPagadorAlIntegranteConMenorMontoAcumulado() {
    // given
    Gasto gastoMacarena = new Gasto("Comida", 10000.0, 1L, "Varios");
    servicioRegistroGasto.registrarGasto(gastoMacarena);

    // when
    String sugerencia = servicioRegistroGasto.obtenerSugerenciaProximoPagador();

    // then
    assertThat(sugerencia, equalToIgnoringCase("ezequiel"));
  }

  private Gasto givenUnGastoValido() {
    return new Gasto("Compras del chino", 25000.0, 2L, "ALIMENTACION");
  }

  private Gasto givenUnGastoConMontoInvalido() {
    return new Gasto("Compras del chino", -500.0, 2L, "ALIMENTACION");
  }

  private Gasto givenUnGastoConDescripcionVacia() {
    return new Gasto("", 15000.0, 2L, "ALIMENTACION");
  }

  private void whenRegistroUn(Gasto gasto) {
    servicioRegistroGasto.registrarGasto(gasto);
  }

  private void thenElGastoSeGuardaEnLaLista(Gasto gastoValido) {
    List<Gasto> listaGastos = servicioRegistroGasto.obtenerTodosLosGastos();
    assertNotNull(listaGastos, "La lista de gastos no debería ser nula");
    assertEquals(1, listaGastos.size(), "Debería haber exactamente 1 gasto registrado");
    assertEquals(gastoValido.getDescripcion(), listaGastos.get(0).getDescripcion());
  }
}
