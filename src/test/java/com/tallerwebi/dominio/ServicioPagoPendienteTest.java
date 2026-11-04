package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ServicioPagoPendienteTest {

  private RepositorioPagoPendiente repositorioPagoPendienteMock;
  private ServicioPagoPendiente servicioPagoPendiente;

  private Hogar hogarMock;
  private Usuario emisorMock;
  private Usuario receptorMock;
  private PagoPendiente pagoPendienteMock;

  @BeforeEach
  public void setUp() {
    repositorioPagoPendienteMock = Mockito.mock(RepositorioPagoPendiente.class);
    servicioPagoPendiente = new ServicioPagoPendienteImpl(repositorioPagoPendienteMock);

    hogarMock = new Hogar();
    hogarMock.setId(1L);

    emisorMock = new Usuario();
    emisorMock.setId(10L);
    emisorMock.setNombre("Carlos");

    receptorMock = new Usuario();
    receptorMock.setId(20L);
    receptorMock.setNombre("Juan");

    pagoPendienteMock = new PagoPendiente(emisorMock, receptorMock, 1500.0, hogarMock);
    pagoPendienteMock.setId(100L);
  }

  @Test
  public void queSePuedaVerificarSiExisteUnPagoPendienteYRetorneTrue() {
    // Given
    when(repositorioPagoPendienteMock.buscarPorDatos(emisorMock, receptorMock, 1500.0, hogarMock))
      .thenReturn(pagoPendienteMock);

    // When
    boolean existe = servicioPagoPendiente.existePendiente(
      emisorMock,
      receptorMock,
      1500.0,
      hogarMock
    );

    // Then
    assertThat(existe, is(true));
    verify(repositorioPagoPendienteMock, times(1))
      .buscarPorDatos(emisorMock, receptorMock, 1500.0, hogarMock);
  }

  @Test
  public void queRetorneFalseSiNoExisteElPagoPendiente() {
    // Given
    when(repositorioPagoPendienteMock.buscarPorDatos(emisorMock, receptorMock, 1500.0, hogarMock))
      .thenReturn(null);

    // When
    boolean existe = servicioPagoPendiente.existePendiente(
      emisorMock,
      receptorMock,
      1500.0,
      hogarMock
    );

    // Then
    assertThat(existe, is(false));
  }

  @Test
  public void queSePuedaEliminarUnPagoPendientePorId() {
    // Given
    Long idAEliminar = 100L;
    doNothing().when(repositorioPagoPendienteMock).eliminarPorId(idAEliminar);

    // When
    servicioPagoPendiente.eliminarPorId(idAEliminar);

    // Then
    verify(repositorioPagoPendienteMock, times(1)).eliminarPorId(idAEliminar);
  }

  @Test
  public void queSePuedaBuscarUnPagoPendientePorId() {
    // Given
    Long idABuscar = 100L;
    when(repositorioPagoPendienteMock.buscarPorId(idABuscar)).thenReturn(pagoPendienteMock);

    // When
    PagoPendiente encontrado = servicioPagoPendiente.buscarPorId(idABuscar);

    // Then
    assertThat(encontrado, notNullValue());
    assertThat(encontrado.getId(), is(idABuscar));
    verify(repositorioPagoPendienteMock, times(1)).buscarPorId(idABuscar);
  }

  @Test
  public void queSePuedaGuardarUnPagoPendiente() {
    // Given
    doNothing().when(repositorioPagoPendienteMock).guardar(pagoPendienteMock);

    // When
    servicioPagoPendiente.guardarPendiente(pagoPendienteMock);

    // Then
    verify(repositorioPagoPendienteMock, times(1)).guardar(pagoPendienteMock);
  }

  @Test
  public void queSePuedanObtenerTodosLosPendientesDeUnHogar() {
    // Given
    List<PagoPendiente> listaEsperada = Arrays.asList(pagoPendienteMock);
    when(repositorioPagoPendienteMock.obtenerPendientesPorHogar(hogarMock))
      .thenReturn(listaEsperada);

    // When
    List<PagoPendiente> resultados = servicioPagoPendiente.obtenerPendientes(hogarMock);

    // Then
    assertThat(resultados, notNullValue());
    assertThat(resultados, hasSize(1));
    assertThat(resultados.get(0), is(pagoPendienteMock));
    verify(repositorioPagoPendienteMock, times(1)).obtenerPendientesPorHogar(hogarMock);
  }

  @Test
  public void queSePuedaEliminarUnPagoPendienteExistentePorSusDatos() {
    // Given
    when(repositorioPagoPendienteMock.buscarPorDatos(emisorMock, receptorMock, 1500.0, hogarMock))
      .thenReturn(pagoPendienteMock);
    doNothing().when(repositorioPagoPendienteMock).eliminar(pagoPendienteMock);

    // When
    servicioPagoPendiente.eliminarPendiente(emisorMock, receptorMock, 1500.0, hogarMock);

    // Then
    verify(repositorioPagoPendienteMock, times(1))
      .buscarPorDatos(emisorMock, receptorMock, 1500.0, hogarMock);
    verify(repositorioPagoPendienteMock, times(1)).eliminar(pagoPendienteMock);
  }

  @Test
  public void queNoHagaNadaSiIntentaEliminarUnPagoPendienteQueNoExiste() {
    // Given
    when(repositorioPagoPendienteMock.buscarPorDatos(emisorMock, receptorMock, 1500.0, hogarMock))
      .thenReturn(null);

    // When
    servicioPagoPendiente.eliminarPendiente(emisorMock, receptorMock, 1500.0, hogarMock);

    // Then
    verify(repositorioPagoPendienteMock, times(1))
      .buscarPorDatos(emisorMock, receptorMock, 1500.0, hogarMock);
    verify(repositorioPagoPendienteMock, never()).eliminar(any());
  }
}
