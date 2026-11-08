package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.PropuestaNoExiste;
import com.tallerwebi.dominio.excepcion.SinPermisoParaCancelarException;
import com.tallerwebi.dominio.excepcion.UsuarioYaVoto;
import com.tallerwebi.dominio.excepcion.VotacionCerradaException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioVotacionTest {

  private static final Long HOGAR_ID = 3L;
  private static final Long PROPUESTA_ID = 5L;

  private ServicioVotacion servicio;
  private RepositorioVoto repositorioVoto;
  private RepositorioPropuesta repositorioPropuesta;
  private RepositorioUsuario repositorioUsuario;
  private Propuesta propuesta;

  @BeforeEach
  public void init() {
    repositorioVoto = mock(RepositorioVoto.class);
    repositorioPropuesta = mock(RepositorioPropuesta.class);
    servicio = new ServicioVotacion(repositorioVoto, repositorioPropuesta, repositorioUsuario);

    propuesta =
      new Propuesta(
        "Comprar heladera",
        "La vieja no enfria",
        1L,
        LocalDate.now().plusDays(7),
        50.0,
        EstadoPropuesta.PENDIENTE
      );
    propuesta.setId(PROPUESTA_ID);
    propuesta.setHogarId(HOGAR_ID);
    when(repositorioPropuesta.obtenerPorId(PROPUESTA_ID)).thenReturn(propuesta);
  }

  // --- Guardar ---

  @Test
  public void deberiaGuardarUnaPropuestaValida() {
    servicio.guardarPropuesta(propuesta);

    verify(repositorioPropuesta).guardar(propuesta);
  }

  @Test
  public void noDeberiaGuardarUnaPropuestaSinHogar() {
    propuesta.setHogarId(null);

    assertThrows(PropuestaNoExiste.class, () -> servicio.guardarPropuesta(propuesta));
  }

  @Test
  public void enUnHogarDeTresConUnSoloVotoAFavorSiguePendiente() {
    dadoQueElHogarTiene(3, 1, 0);
 
    servicio.votar(2L, HOGAR_ID, PROPUESTA_ID, true, null);
 
    assertThat(propuesta.getEstado(), equalTo(EstadoPropuesta.PENDIENTE));
  }
 
  @Test
  public void siLosQueFaltanVotarAlcanzanParaElQuorumSiguePendiente() {
    dadoQueElHogarTiene(5, 2, 1);
    propuesta.setPorcentajeQuorum(70.0);
 
    servicio.votar(2L, HOGAR_ID, PROPUESTA_ID, false, null);
 
    assertThat(propuesta.getEstado(), equalTo(EstadoPropuesta.PENDIENTE));
  }

  @Test
  public void unaPropuestaVencidaSinQuorumQuedaCancelada() {
    propuesta.setFechaLimite(LocalDate.now().minusDays(1));
    when(repositorioPropuesta.obtenerPropuestasPorHogar(HOGAR_ID)).thenReturn(List.of(propuesta));
    dadoQueElHogarTiene(4, 1, 0);

    servicio.obtenerPropuestasDelHogar(HOGAR_ID);

    assertThat(propuesta.getEstado(), equalTo(EstadoPropuesta.CANCELADA));
  }

  // --- Votar ---

  @Test
  public void noSePuedeVotarUnaPropuestaDeOtroHogar() {
    assertThrows(PropuestaNoExiste.class, () -> servicio.votar(1L, 99L, PROPUESTA_ID, true, null));
  }

  @Test
  public void noSePuedeVotarUnaPropuestaCerrada() {
    propuesta.setEstado(EstadoPropuesta.APROBADA);

    assertThrows(
      VotacionCerradaException.class,
      () -> servicio.votar(1L, HOGAR_ID, PROPUESTA_ID, true, null)
    );
  }

  @Test
  public void noSePuedeVotarDosVeces() {
    when(repositorioVoto.existeVoto(1L, PROPUESTA_ID)).thenReturn(true);

    assertThrows(UsuarioYaVoto.class, () -> servicio.votar(1L, HOGAR_ID, PROPUESTA_ID, true, null));
  }

  @Test
  public void siempreSeRequiereAlMenosUnVoto() {
    assertThat(servicio.calcularVotosRequeridos(3, 10.0), equalTo(1L));
  }

  @Test
  public void enUnHogarDeTresConUnVotoAFavorSeApruebaConQuorumDel50() {
    dadoQueElHogarTiene(3, 1, 0);

    servicio.votar(2L, HOGAR_ID, PROPUESTA_ID, true, null);

    assertThat(propuesta.getEstado(), equalTo(EstadoPropuesta.APROBADA));
  }

  @Test
  public void siLosQueFaltanVotarAlcanzanParaElQuorumSiguePendiente() {
    dadoQueElHogarTiene(5, 1, 2);
    propuesta.setPorcentajeQuorum(70.0);

    servicio.votar(2L, HOGAR_ID, PROPUESTA_ID, false, null);

    assertThat(propuesta.getEstado(), equalTo(EstadoPropuesta.PENDIENTE));
  }

  @Test
  public void siLosQueFaltanVotarNoAlcanzanParaElQuorumSeRechaza() {
    dadoQueElHogarTiene(5, 1, 3);
    propuesta.setPorcentajeQuorum(70.0);

    servicio.votar(2L, HOGAR_ID, PROPUESTA_ID, false, null);

    assertThat(propuesta.getEstado(), equalTo(EstadoPropuesta.RECHAZADA));
  }

  @Test
  public void elCreadorPuedeCancelarSuPropuesta() {
    servicio.cancelarPropuesta(1L, HOGAR_ID, PROPUESTA_ID, false);

    assertThat(propuesta.getEstado(), equalTo(EstadoPropuesta.CANCELADA));
  }

  @Test
  public void unAdminPuedeCancelarUnaPropuestaAjena() {
    servicio.cancelarPropuesta(2L, HOGAR_ID, PROPUESTA_ID, true);

    assertThat(propuesta.getEstado(), equalTo(EstadoPropuesta.CANCELADA));
  }

  @Test
  public void otroIntegranteNoPuedeCancelarLaPropuesta() {
    assertThrows(
      SinPermisoParaCancelarException.class,
      () -> servicio.cancelarPropuesta(2L, HOGAR_ID, PROPUESTA_ID, false)
    );
    assertThat(propuesta.getEstado(), equalTo(EstadoPropuesta.PENDIENTE));
  }

  @Test
  public void noSePuedeCancelarUnaPropuestaQueYaNoEstaPendiente() {
    propuesta.setEstado(EstadoPropuesta.APROBADA);

    assertThrows(
      VotacionCerradaException.class,
      () -> servicio.cancelarPropuesta(1L, HOGAR_ID, PROPUESTA_ID, false)
    );
  }

  @Test
  public void noSePuedeCancelarUnaPropuestaDeOtroHogar() {
    assertThrows(
      PropuestaNoExiste.class,
      () -> servicio.cancelarPropuesta(1L, 99L, PROPUESTA_ID, true)
    );
  }

  private void dadoQueElHogarTiene(long integrantes, long aFavor, long enContra) {
    when(repositorioPropuesta.obtenerTotalIntegrantes(HOGAR_ID)).thenReturn(integrantes);
    when(repositorioVoto.obtenerVotosAfirmativosPorPropuesta(PROPUESTA_ID)).thenReturn(aFavor);
    when(repositorioVoto.obtenerVotosNegativosPorPropuesta(PROPUESTA_ID)).thenReturn(enContra);
  }
}
