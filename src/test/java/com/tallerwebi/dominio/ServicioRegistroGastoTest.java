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
import static org.mockito.ArgumentMatchers.eq;
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
  private RepositorioHogar repositorioHogar;
  private ServicioRegistroGasto servicioRegistroGasto;

  private List<Gasto> gastosEnMemoria;
  private static final Long HOGAR_ID = 1L;

  @BeforeEach
  public void init() {
    repositorioGasto = mock(RepositorioGasto.class);
    repositorioUsuario = mock(RepositorioUsuario.class);
    repositorioHogar = mock(RepositorioHogar.class);

    servicioRegistroGasto =
      new ServicioRegistroGastoImpl(repositorioGasto, repositorioUsuario, repositorioHogar);
    gastosEnMemoria = new ArrayList<>();

    // Simulamos comportamiento adaptado al hogarId
    when(repositorioGasto.obtenerTodosPorHogar(eq(HOGAR_ID)))
      .thenAnswer(invocation -> gastosEnMemoria);
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
    Gasto gastoValido = givenUnGastoValido();
    whenRegistroUn(gastoValido);
    thenElGastoSeGuardaEnLaLista(gastoValido);
  }

  @Test
  public void dadoUnGastoConMontoInvalidoCuandoLoRegistroEntoncesLanzaExcepcion() {
    Gasto gastoConMontoInvalido = givenUnGastoConMontoInvalido();
    assertThrows(MontoGastoInvalidoException.class, () -> whenRegistroUn(gastoConMontoInvalido));
  }

  @Test
  public void dadoUnGastoConDescripcionVaciaCuandoLoRegistroEntoncesLanzaExcepcion() {
    Gasto gastoConDescripcionVacia = givenUnGastoConDescripcionVacia();
    assertThrows(
      DescripcionGastoVaciaException.class,
      () -> whenRegistroUn(gastoConDescripcionVacia)
    );
  }

  @Test
  public void debeObtenerLaListaDeIntegrantesCorrectamente() {
    List<Usuario> integrantesMock = givenCuatroIntegrantes();
    // CORREGIDO: Se pasa HOGAR_ID al método del repositorio
    when(repositorioUsuario.obtenerIntegrantesDelHogar(HOGAR_ID)).thenReturn(integrantesMock);

    List<Usuario> integrantes = servicioRegistroGasto.obtenerIntegrantes(HOGAR_ID);

    assertThat(integrantes, hasSize(4));
    assertThat(integrantes.get(0).getEmail(), containsString("macarena"));
  }

  @Test
  public void debeSugerirComoProximoPagadorAlIntegranteConMenorMontoAcumulado() {
    List<Usuario> integrantesMock = givenCuatroIntegrantes();
    // CORREGIDO: Se pasa HOGAR_ID al método del repositorio
    when(repositorioUsuario.obtenerIntegrantesDelHogar(HOGAR_ID)).thenReturn(integrantesMock);

    Usuario macarena = integrantesMock.get(0);
    Gasto gastoMacarena = new Gasto(
      "G001",
      TipoGasto.OCASIONAL,
      "Comida",
      10000.0,
      macarena,
      "Varios"
    );
    servicioRegistroGasto.registrarGasto(gastoMacarena, HOGAR_ID);

    String sugerencia = servicioRegistroGasto.obtenerSugerenciaProximoPagador(HOGAR_ID);

    assertThat(sugerencia, equalToIgnoringCase("ezequiel"));
  }

  @Test
  public void dadaUnaListaDeGastosPuedoObtenerUnaListaFiltradaPorTipo() {
    List<Gasto> listaRecurrentes = givenUnaListaDeGastosRegistrados();
    when(repositorioGasto.obtenerPorTipo(TipoGasto.RECURRENTE, HOGAR_ID))
      .thenReturn(listaRecurrentes);

    List<Gasto> listaGastosFiltrada = whenFiltroLosGastosPorTipo(TipoGasto.RECURRENTE);
    thenObtengoUnaListaFiltrada(listaGastosFiltrada);
  }

  @Test
  public void queSePuedaObtenerGastosFijosRecurrentes() {
    List<Gasto> listaFijosRecurrentes = givenUnaListaDeGastosRegistrados();
    when(repositorioGasto.obtenerGastosFijosRecurrentes(HOGAR_ID))
      .thenReturn(listaFijosRecurrentes);

    List<Gasto> resultado = servicioRegistroGasto.obtenerGastosFijosRecurrentes(HOGAR_ID);

    assertThat(resultado, equalTo(listaFijosRecurrentes));
    assertThat(resultado, hasSize(2));
    verify(repositorioGasto, times(1)).obtenerGastosFijosRecurrentes(HOGAR_ID);
  }

  @Test
  public void queSePuedaActualizarGastoFijo() {
    Long id = 1L;
    Gasto gasto = new Gasto("G001", TipoGasto.FIJO, "Alquiler", 50000.0, null, "ALQUILER");
    gasto.setId(id);
    gasto.setPagado(true);

    when(repositorioGasto.buscarPorId(id)).thenReturn(gasto);

    LocalDateTime nuevaFecha = LocalDateTime.now();
    servicioRegistroGasto.actualizarGastoFijo(id, 60000.0, nuevaFecha);

    assertThat(gasto.getMonto(), equalTo(60000.0));
    assertThat(gasto.getPagado(), equalTo(false));
    assertThat(gasto.getFechaVencimiento(), equalTo(nuevaFecha));
    verify(repositorioGasto, times(1)).modificar(gasto);
  }

  @Test
  public void queSePuedaEliminarGasto() {
    Long id = 1L;
    servicioRegistroGasto.eliminarGasto(id);
    verify(repositorioGasto, times(1)).eliminar(id);
  }

  @Test
  public void queSePuedaMarcarGastoComoPagado() {
    Long gastoId = 1L;
    Long usuarioId = 2L;

    Gasto gasto = new Gasto("G001", TipoGasto.RECURRENTE, "Luz", 15000.0, null, "SERVICIOS");
    gasto.setId(gastoId);
    gasto.setPagado(false);

    List<Usuario> integrantesMock = givenCuatroIntegrantes();
    Usuario usuario = integrantesMock.get(1);

    when(repositorioGasto.buscarPorId(gastoId)).thenReturn(gasto);
    when(repositorioUsuario.buscarPorId(usuarioId)).thenReturn(usuario);

    servicioRegistroGasto.marcarComoPagado(gastoId, usuarioId);

    assertThat(gasto.getPagado(), equalTo(true));
    assertThat(gasto.getUsuarioPagador(), equalTo(usuario));
    verify(repositorioGasto, times(1)).modificar(gasto);
  }

  @Test
  public void dadoQueHayGastosPendientesConVencimientoCuandoLosPidoEntoncesDevuelveLosDelRepositorio() {
    Gasto luz = new Gasto("G-010", TipoGasto.FIJO, "Luz", 5000.0, "SERVICIOS");
    luz.setFechaVencimiento(LocalDateTime.now().plusDays(5));
    when(repositorioGasto.obtenerGastosPendientesConVencimiento(HOGAR_ID)).thenReturn(List.of(luz));

    List<Gasto> resultado = servicioRegistroGasto.obtenerVencimientosPendientes(HOGAR_ID);

    assertThat(resultado, hasSize(1));
    assertThat(resultado.get(0).getDescripcion(), equalTo("Luz"));
  }

  @Test
  public void dadaUnaFechaКогдаPidoLosVencimientosProximosEntoncesConsultaLosProximos30Dias() {
    LocalDateTime hoy = LocalDateTime.now();
    Gasto luz = new Gasto("G-010", TipoGasto.FIJO, "Luz", 5000.0, "SERVICIOS");
    when(
      repositorioGasto.obtenerPendientesConVencimientoEntre(
        eq(hoy),
        any(LocalDateTime.now().getClass()),
        eq(HOGAR_ID)
      )
    )
      .thenReturn(List.of(luz));

    List<Gasto> resultado = servicioRegistroGasto.obtenerVencimientosProximos(hoy, HOGAR_ID);

    assertThat(resultado, hasSize(1));
    verify(repositorioGasto)
      .obtenerPendientesConVencimientoEntre(
        eq(hoy),
        any(LocalDateTime.now().getClass()),
        eq(HOGAR_ID)
      );
  }

  @Test
  public void dadaUnaFechaCuandoPidoLosVencimientosVencidosEntoncesConsultaLosAnterioresAEsaFecha() {
    LocalDateTime hoy = LocalDateTime.now();
    Gasto gas = new Gasto("G-011", TipoGasto.FIJO, "Gas", 3000.0, "SERVICIOS");
    when(repositorioGasto.obtenerPendientesVencidosAntesDe(hoy, HOGAR_ID)).thenReturn(List.of(gas));

    List<Gasto> resultado = servicioRegistroGasto.obtenerVencimientosVencidos(hoy, HOGAR_ID);

    assertThat(resultado, hasSize(1));
    verify(repositorioGasto).obtenerPendientesVencidosAntesDe(hoy, HOGAR_ID);
  }

  private void thenObtengoUnaListaFiltrada(List<Gasto> listaFiltrada) {
    assertThat(listaFiltrada.size(), equalTo(2));
    assertThat(listaFiltrada.get(0).getTipoGasto(), equalTo(TipoGasto.RECURRENTE));
    assertThat(listaFiltrada.get(1).getTipoGasto(), equalTo(TipoGasto.RECURRENTE));
  }

  private List<Gasto> whenFiltroLosGastosPorTipo(TipoGasto tipo) {
    return servicioRegistroGasto.obtenerGastosPorTipo(tipo, HOGAR_ID);
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
    servicioRegistroGasto.registrarGasto(gasto, HOGAR_ID);
  }

  private void thenElGastoSeGuardaEnLaLista(Gasto gastoValido) {
    List<Gasto> listaGastos = servicioRegistroGasto.obtenerTodosLosGastos(HOGAR_ID);
    assertNotNull(listaGastos, "La lista de gastos no debería ser nula");
    assertEquals(1, listaGastos.size(), "Debería haber exactamente 1 gasto registrado");
    assertEquals(gastoValido.getDescripcion(), listaGastos.get(0).getDescripcion());
  }
}
