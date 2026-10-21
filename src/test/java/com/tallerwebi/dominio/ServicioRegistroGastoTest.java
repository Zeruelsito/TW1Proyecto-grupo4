package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.excepcion.DescripcionGastoVaciaException;
import com.tallerwebi.dominio.excepcion.MontoGastoInvalidoException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioRegistroGastoTest {

  private RepositorioGasto repositorioGasto;
  private RepositorioUsuario repositorioUsuario;
  private ServicioRegistroGasto servicioRegistroGasto;

  private List<Gasto> gastosEnMemoria;

  @BeforeEach
  public void init() {
    // Mockeamos los repositorios que ahora inyecta el servicio
    repositorioGasto = mock(RepositorioGasto.class);
    repositorioUsuario = mock(RepositorioUsuario.class);

    servicioRegistroGasto = new ServicioRegistroGastoImpl(repositorioGasto, repositorioUsuario);
    gastosEnMemoria = new ArrayList<>();

    // Simulamos comportamiento básico para que el registro y listado funcionen en memoria durante el test
    when(repositorioGasto.obtenerTodos()).thenAnswer(invocation -> gastosEnMemoria);
    doAnswer(invocation -> {
        Gasto g = invocation.getArgument(0);
        gastosEnMemoria.add(g);
        return null;
      })
      .when(repositorioGasto)
      .guardar(any(Gasto.class));
  }

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
    List<Usuario> integrantesMock = givenCuatroIntegrantes();
    when(repositorioUsuario.obtenerIntegrantesDelHogar()).thenReturn(integrantesMock);

    List<Usuario> integrantes = servicioRegistroGasto.obtenerIntegrantes();

    assertThat(integrantes, hasSize(4));
    assertThat(integrantes.get(0).getEmail(), containsString("macarena"));
  }

  @Test
  public void debeSugerirComoProximoPagadorAlIntegranteConMenorMontoAcumulado() {
    // given
    List<Usuario> integrantesMock = givenCuatroIntegrantes();
    when(repositorioUsuario.obtenerIntegrantesDelHogar()).thenReturn(integrantesMock);

    Usuario macarena = integrantesMock.get(0);
    Gasto gastoMacarena = new Gasto(
      "G001",
      TipoGasto.OCASIONAL,
      "Comida",
      10000.0,
      macarena,
      "Varios"
    );
    servicioRegistroGasto.registrarGasto(gastoMacarena);

    // when
    String sugerencia = servicioRegistroGasto.obtenerSugerenciaProximoPagador();

    // then
    assertThat(sugerencia, equalToIgnoringCase("ezequiel"));
  }

  @Test
  public void dadaUnaListaDeGastosPuedoObtenerUnaListaFiltradaPorTipo() {
    //given
    List<Gasto> listaRecurrentes = givenUnaListaDeGastosRegistrados();
    when(repositorioGasto.obtenerPorTipo(TipoGasto.RECURRENTE)).thenReturn(listaRecurrentes);

    //when
    List<Gasto> listaGastosFiltrada = whenFiltroLosGastosPorTipo(TipoGasto.RECURRENTE);
    //then
    thenObtengoUnaListaFiltrada(listaGastosFiltrada);
  }

  @Test
  public void queSePuedaObtenerGastosFijosRecurrentes() {
    // given
    List<Gasto> listaFijosRecurrentes = givenUnaListaDeGastosRegistrados();
    when(repositorioGasto.obtenerGastosFijosRecurrentes()).thenReturn(listaFijosRecurrentes);

    // when
    List<Gasto> resultado = servicioRegistroGasto.obtenerGastosFijosRecurrentes();

    // then
    assertThat(resultado, equalTo(listaFijosRecurrentes));
    assertThat(resultado, hasSize(2));
    verify(repositorioGasto, times(1)).obtenerGastosFijosRecurrentes();
  }

  @Test
  public void queSePuedaActualizarGastoFijo() {
    // given
    Long id = 1L;
    Gasto gasto = new Gasto("G001", TipoGasto.FIJO, "Alquiler", 50000.0, null, "ALQUILER");
    gasto.setId(id);
    gasto.setPagado(true);

    when(repositorioGasto.buscarPorId(id)).thenReturn(gasto);

    // when
    LocalDateTime nuevaFecha = LocalDateTime.now();
    servicioRegistroGasto.actualizarGastoFijo(id, 60000.0, nuevaFecha);

    // then
    assertThat(gasto.getMonto(), equalTo(60000.0));
    assertThat(gasto.getPagado(), equalTo(false)); // Se resetea a pendiente para el nuevo ciclo
    assertThat(gasto.getFechaVencimiento(), equalTo(nuevaFecha));
    verify(repositorioGasto, times(1)).modificar(gasto);
  }

  @Test
  public void queSePuedaEliminarGasto() {
    // given
    Long id = 1L;

    // when
    servicioRegistroGasto.eliminarGasto(id);

    // then
    verify(repositorioGasto, times(1)).eliminar(id);
  }

  @Test
  public void queSePuedaMarcarGastoComoPagado() {
    // given
    Long gastoId = 1L;
    Long usuarioId = 2L;

    Gasto gasto = new Gasto("G001", TipoGasto.RECURRENTE, "Luz", 15000.0, null, "SERVICIOS");
    gasto.setId(gastoId);
    gasto.setPagado(false);

    List<Usuario> integrantesMock = givenCuatroIntegrantes();
    Usuario usuario = integrantesMock.get(1); // ezequiel

    when(repositorioGasto.buscarPorId(gastoId)).thenReturn(gasto);
    when(repositorioUsuario.buscarPorId(usuarioId)).thenReturn(usuario);

    // when
    servicioRegistroGasto.marcarComoPagado(gastoId, usuarioId);

    // then
    assertThat(gasto.getPagado(), equalTo(true));
    assertThat(gasto.getUsuarioPagador(), equalTo(usuario));
    verify(repositorioGasto, times(1)).modificar(gasto);
  }

  @Test
  public void dadoQueHayGastosPendientesConVencimientoCuandoLosPidoEntoncesDevuelveLosDelRepositorio() {
    // given
    Gasto luz = new Gasto("G-010", TipoGasto.FIJO, "Luz", 5000.0, "SERVICIOS");
    luz.setFechaVencimiento(LocalDateTime.now().plusDays(5));
    when(repositorioGasto.obtenerGastosPendientesConVencimiento()).thenReturn(List.of(luz));

    // when
    List<Gasto> resultado = servicioRegistroGasto.obtenerVencimientosPendientes();

    // then
    assertThat(resultado, hasSize(1));
    assertThat(resultado.get(0).getDescripcion(), equalTo("Luz"));
  }

  @Test
  public void dadaUnaFechaCuandoPidoLosVencimientosProximosEntoncesConsultaLosProximos30Dias() {
    LocalDateTime hoy = LocalDateTime.now();
    Gasto luz = new Gasto("G-010", TipoGasto.FIJO, "Luz", 5000.0, "SERVICIOS");
    when(repositorioGasto.obtenerPendientesConVencimientoEntre(hoy, hoy.plusDays(30)))
      .thenReturn(List.of(luz));

    List<Gasto> resultado = servicioRegistroGasto.obtenerVencimientosProximos(hoy);

    assertThat(resultado, hasSize(1));
    verify(repositorioGasto).obtenerPendientesConVencimientoEntre(hoy, hoy.plusDays(30));
  }

  @Test
  public void dadaUnaFechaCuandoPidoLosVencimientosVencidosEntoncesConsultaLosAnterioresAEsaFecha() {
    LocalDateTime hoy = LocalDateTime.now();
    //LocalDateTime hoy = LocalDateTime.of(2026, 10, 19, 10, 0);
    Gasto gas = new Gasto("G-011", TipoGasto.FIJO, "Gas", 3000.0, "SERVICIOS");
    when(repositorioGasto.obtenerPendientesVencidosAntesDe(hoy)).thenReturn(List.of(gas));

    List<Gasto> resultado = servicioRegistroGasto.obtenerVencimientosVencidos(hoy);

    assertThat(resultado, hasSize(1));
    verify(repositorioGasto).obtenerPendientesVencidosAntesDe(hoy);
  }

  private void thenObtengoUnaListaFiltrada(List<Gasto> listaFiltrada) {
    assertThat(listaFiltrada.size(), equalTo(2));
    assertThat(listaFiltrada.get(0).getTipoGasto(), equalTo(TipoGasto.RECURRENTE));
    assertThat(listaFiltrada.get(1).getTipoGasto(), equalTo(TipoGasto.RECURRENTE));
  }

  private List<Gasto> whenFiltroLosGastosPorTipo(TipoGasto tipo) {
    return servicioRegistroGasto.obtenerGastosPorTipo(tipo);
  }

  private List<Gasto> givenUnaListaDeGastosRegistrados() {
    Usuario u2 = new Usuario("ezequiel", "ezequiel@kumo.com", "2222");
    u2.setId(2L);

    Gasto gasto2 = new Gasto("G002", TipoGasto.RECURRENTE, "Factura luz", 90000.0, u2, "SERVICIOS");
    Gasto gasto3 = new Gasto(
      "G003",
      TipoGasto.RECURRENTE,
      "Factura agua",
      40000.0,
      u2,
      "SERVICIOS"
    );

    return List.of(gasto2, gasto3);
  }

  private List<Usuario> givenCuatroIntegrantes() {
    Usuario u1 = new Usuario("macarena", "macarena@kumo.com", "1111");
    u1.setId(1L);
    Usuario u2 = new Usuario("ezequiel", "ezequiel@kumo.com", "2222");
    u2.setId(2L);
    Usuario u3 = new Usuario("oriana", "oriana@kumo.com", "3333");
    u3.setId(3L);
    Usuario u4 = new Usuario("dylan", "dylan@kumo.com", "4444");
    u4.setId(4L);
    return List.of(u1, u2, u3, u4);
  }

  private Gasto givenUnGastoValido() {
    Usuario u2 = new Usuario("ezequiel", "ezequiel@kumo.com", "2222");
    u2.setId(2L);
    return new Gasto("G001", TipoGasto.OCASIONAL, "Compras del chino", 25000.0, u2, "ALIMENTACION");
  }

  private Gasto givenUnGastoConMontoInvalido() {
    Usuario u2 = new Usuario("ezequiel", "ezequiel@kumo.com", "2222");
    u2.setId(2L);
    return new Gasto("G001", TipoGasto.OCASIONAL, "Compras del chino", -500.0, u2, "ALIMENTACION");
  }

  private Gasto givenUnGastoConDescripcionVacia() {
    Usuario u2 = new Usuario("ezequiel", "ezequiel@kumo.com", "2222");
    u2.setId(2L);
    return new Gasto("G001", TipoGasto.OCASIONAL, "", 15000.0, u2, "ALIMENTACION");
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
