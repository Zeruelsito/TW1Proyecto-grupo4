package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
 
import com.tallerwebi.dominio.enums.EstadoPropuesta;
import com.tallerwebi.dominio.excepcion.PropuestaNoExiste;
import com.tallerwebi.dominio.excepcion.UsuarioYaVoto;
import com.tallerwebi.dominio.repository.RepositorioPropuesta;
import com.tallerwebi.dominio.repository.RepositorioVoto;
import com.tallerwebi.dominio.service.ServicioVotacion;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
 
public class ServicioVotacionTest {
 
  private ServicioVotacion servicioVotacion;
  private RepositorioVoto repositorioVotoMock;
  private RepositorioPropuesta repositorioPropuestaMock;
  private Propuesta propuesta;
 
  @BeforeEach
  public void init() {
    repositorioVotoMock = mock(RepositorioVoto.class);
    repositorioPropuestaMock = mock(RepositorioPropuesta.class);
    servicioVotacion = new ServicioVotacion(repositorioVotoMock, repositorioPropuestaMock);
 
    propuesta = new Propuesta(
      "Comprar heladera",
      "La vieja no enfria",
      1L,
      LocalDate.now().plusDays(7),
      50.0,
      EstadoPropuesta.PENDIENTE
    );
    propuesta.setId(5L);
  }
 
  @Test
  public void guardarPropuestaValidaDeberiaGuardarlaComoPendiente() {
    // ejecucion
    servicioVotacion.guardarPropuesta(propuesta);
 
    // validacion
    assertThat(propuesta.getEstado(), equalTo(EstadoPropuesta.PENDIENTE));
    verify(repositorioPropuestaMock, times(1)).guardar(propuesta);
  }
 
  @Test
  public void guardarPropuestaSinTituloDeberiaLanzarExcepcion() {
    // preparacion
    propuesta.setTitulo("");
 
    // ejecucion y validacion
    assertThrows(PropuestaNoExiste.class, () -> servicioVotacion.guardarPropuesta(propuesta));
    verify(repositorioPropuestaMock, times(0)).guardar(propuesta);
  }
 
  @Test
  public void guardarPropuestaConQuorumInvalidoDeberiaLanzarExcepcion() {
    // preparacion
    propuesta.setPorcentajeQuorum(0);
 
    // ejecucion y validacion
    assertThrows(PropuestaNoExiste.class, () -> servicioVotacion.guardarPropuesta(propuesta));
  }
 
  @Test
  public void guardarPropuestaConFechaPasadaDeberiaLanzarExcepcion() {
    // preparacion
    propuesta.setFechaLimite(LocalDate.now().minusDays(1));
 
    // ejecucion y validacion
    assertThrows(PropuestaNoExiste.class, () -> servicioVotacion.guardarPropuesta(propuesta));
  }
 
  @Test
  public void votarPropuestaQueNoExisteDeberiaLanzarExcepcion() {
    // preparacion
    when(repositorioPropuestaMock.obtenerPorId(5L)).thenReturn(null);
 
    // ejecucion y validacion
    assertThrows(PropuestaNoExiste.class, () -> servicioVotacion.votar(1L, 5L, true, null));
  }
 
  @Test
  public void votarDosVecesDeberiaLanzarExcepcion() {
    // preparacion
    when(repositorioPropuestaMock.obtenerPorId(5L)).thenReturn(propuesta);
    when(repositorioVotoMock.existeVoto(1L, 5L)).thenReturn(true);
 
    // ejecucion y validacion
    assertThrows(UsuarioYaVoto.class, () -> servicioVotacion.votar(1L, 5L, true, null));
  }
 
  @Test
  public void votarAFavorHastaLlegarAlQuorumDeberiaAprobarLaPropuesta() {
    // preparacion: 4 integrantes con quorum 50% -> se necesitan 2 votos a favor
    when(repositorioPropuestaMock.obtenerPorId(5L)).thenReturn(propuesta);
    when(repositorioPropuestaMock.obtenerTotalIntegrantes()).thenReturn(4L);
    when(repositorioVotoMock.obtenerVotosAfirmativosPorPropuesta(5L)).thenReturn(2L);
    when(repositorioVotoMock.obtenerVotosNegativosPorPropuesta(5L)).thenReturn(0L);
 
    // ejecucion
    servicioVotacion.votar(1L, 5L, true, "Dale");
 
    // validacion
    assertThat(propuesta.getEstado(), equalTo(EstadoPropuesta.APROBADA));
    verify(repositorioVotoMock, times(1)).guardar(any(Voto.class));
  }
 
  @Test
  public void votarEnContraSinPosibilidadDeQuorumDeberiaRechazarLaPropuesta() {
    // preparacion: 4 integrantes, se necesitan 2 a favor y ya hay 3 en contra
    when(repositorioPropuestaMock.obtenerPorId(5L)).thenReturn(propuesta);
    when(repositorioPropuestaMock.obtenerTotalIntegrantes()).thenReturn(4L);
    when(repositorioVotoMock.obtenerVotosAfirmativosPorPropuesta(5L)).thenReturn(0L);
    when(repositorioVotoMock.obtenerVotosNegativosPorPropuesta(5L)).thenReturn(3L);
 
    // ejecucion
    servicioVotacion.votar(1L, 5L, false, null);
 
    // validacion
    assertThat(propuesta.getEstado(), equalTo(EstadoPropuesta.RECHAZADA));
  }
}
 