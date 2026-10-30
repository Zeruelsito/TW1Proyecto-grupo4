package com.tallerwebi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.PagoPendiente;
import com.tallerwebi.dominio.RepositorioPagoPendiente;
import com.tallerwebi.dominio.ServicioPagoPendienteImpl;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ServicioPagoPendienteTest {

  private RepositorioPagoPendiente repositorioPagoPendienteMock;
  private ServicioPagoPendienteImpl servicioPagoPendiente;
  private Hogar hogarPrueba;

  @BeforeEach
  public void setUp() {
    repositorioPagoPendienteMock = mock(RepositorioPagoPendiente.class);
    servicioPagoPendiente = new ServicioPagoPendienteImpl(repositorioPagoPendienteMock);

    hogarPrueba = new Hogar();
    hogarPrueba.setNombre("Casa Quinta");
  }

  @Test
  public void queRetorneTrueSiExisteUnPagoPendiente() {
    PagoPendiente pendienteMock = new PagoPendiente("Juan", "Maria", 1000.0, hogarPrueba);
    when(repositorioPagoPendienteMock.buscarPorDatos("Juan", "Maria", 1000.0, hogarPrueba))
      .thenReturn(pendienteMock);

    boolean existe = servicioPagoPendiente.existePendiente("Juan", "Maria", 1000.0, hogarPrueba);

    assertThat(existe, is(true));
    verify(repositorioPagoPendienteMock, times(1))
      .buscarPorDatos("Juan", "Maria", 1000.0, hogarPrueba);
  }

  @Test
  public void queRetorneFalseSiNoExisteUnPagoPendiente() {
    when(repositorioPagoPendienteMock.buscarPorDatos("Juan", "Maria", 1000.0, hogarPrueba))
      .thenReturn(null);

    boolean existe = servicioPagoPendiente.existePendiente("Juan", "Maria", 1000.0, hogarPrueba);

    assertThat(existe, is(false));
    verify(repositorioPagoPendienteMock, times(1))
      .buscarPorDatos("Juan", "Maria", 1000.0, hogarPrueba);
  }

  @Test
  public void queSePuedaEliminarUnPagoPendientePorId() {
    Long id = 1L;

    servicioPagoPendiente.eliminarPorId(id);

    verify(repositorioPagoPendienteMock, times(1)).eliminarPorId(id);
  }

  @Test
  public void queSePuedaBuscarUnPagoPendientePorId() {
    Long id = 1L;
    PagoPendiente pendienteMock = new PagoPendiente("Juan", "Maria", 1500.0, hogarPrueba);
    when(repositorioPagoPendienteMock.buscarPorId(id)).thenReturn(pendienteMock);

    PagoPendiente encontrado = servicioPagoPendiente.buscarPorId(id);

    assertThat(encontrado, notNullValue());
    assertThat(encontrado.getEmisor(), equalTo("Juan"));
    verify(repositorioPagoPendienteMock, times(1)).buscarPorId(id);
  }

  @Test
  public void queSePuedaGuardarUnPagoPendiente() {
    PagoPendiente pendiente = new PagoPendiente("Juan", "Maria", 2000.0, hogarPrueba);

    servicioPagoPendiente.guardarPendiente(pendiente);

    verify(repositorioPagoPendienteMock, times(1)).guardar(pendiente);
  }

  @Test
  public void queSePuedanObtenerTodosLosPendientesDeUnHogar() {
    PagoPendiente p1 = new PagoPendiente("Juan", "Maria", 1000.0, hogarPrueba);
    PagoPendiente p2 = new PagoPendiente("Pedro", "Sofia", 500.0, hogarPrueba);
    List<PagoPendiente> listaMock = Arrays.asList(p1, p2);

    when(repositorioPagoPendienteMock.obtenerPendientesPorHogar(hogarPrueba)).thenReturn(listaMock);

    List<PagoPendiente> resultado = servicioPagoPendiente.obtenerPendientes(hogarPrueba);

    assertThat(resultado, notNullValue());
    assertThat(resultado, hasSize(2));
    assertThat(resultado, containsInAnyOrder(p1, p2));
    verify(repositorioPagoPendienteMock, times(1)).obtenerPendientesPorHogar(hogarPrueba);
  }

  @Test
  public void queSePuedaEliminarUnPagoPendientePorDatosSiExiste() {
    PagoPendiente pendienteMock = new PagoPendiente("Juan", "Maria", 1200.0, hogarPrueba);
    when(repositorioPagoPendienteMock.buscarPorDatos("Juan", "Maria", 1200.0, hogarPrueba))
      .thenReturn(pendienteMock);

    servicioPagoPendiente.eliminarPendiente("Juan", "Maria", 1200.0, hogarPrueba);

    verify(repositorioPagoPendienteMock, times(1))
      .buscarPorDatos("Juan", "Maria", 1200.0, hogarPrueba);
    verify(repositorioPagoPendienteMock, times(1)).eliminar(pendienteMock);
  }

  @Test
  public void queNoIntenteEliminarSiElPendienteNoExisteAlBuscarPorDatos() {
    when(repositorioPagoPendienteMock.buscarPorDatos("Juan", "Maria", 1200.0, hogarPrueba))
      .thenReturn(null);

    servicioPagoPendiente.eliminarPendiente("Juan", "Maria", 1200.0, hogarPrueba);

    verify(repositorioPagoPendienteMock, times(1))
      .buscarPorDatos("Juan", "Maria", 1200.0, hogarPrueba);
    verify(repositorioPagoPendienteMock, never()).eliminar(any());
  }
}
