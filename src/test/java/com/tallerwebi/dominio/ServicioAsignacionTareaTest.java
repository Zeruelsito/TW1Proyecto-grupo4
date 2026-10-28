package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.excepcion.HogarSinIntegrantesException;
import com.tallerwebi.dominio.excepcion.TareaNoEncontradaException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioAsignacionTareaTest {

  private RepositorioTareaRepetitiva repositorioTareaRepetitiva;
  private RepositorioHogar repositorioHogar;
  private ServicioTareaRepetitiva servicio;
  private static final Long HOGAR_ID = 1L;

  @BeforeEach
  public void init() {
    repositorioTareaRepetitiva = mock(RepositorioTareaRepetitiva.class);
    repositorioHogar = mock(RepositorioHogar.class);
    servicio = new ServicioTareaRepetitivaImpl(repositorioTareaRepetitiva, repositorioHogar);
  }

  @Test
  public void debeAsignarLaTareaAlIntegranteConMENOSTareasPendientes() {
    Usuario ori = new Usuario();
    ori.setId(11L);
    Usuario pepe = new Usuario();
    pepe.setId(12L);

    TareaRepetitiva tarea1 = new TareaRepetitiva(1L, "compra semanal");
    TareaRepetitiva tarea2 = new TareaRepetitiva(2L, "comprar facturas ricas para el desayuno");
    TareaRepetitiva tarea3 = new TareaRepetitiva(3L, "cargar sube");

    when(repositorioTareaRepetitiva.obtenerPorHogar(HOGAR_ID))
      .thenReturn(List.of(tarea1, tarea2, tarea3));
    List<Usuario> integrantes = new ArrayList<>();
    integrantes.add(ori);
    integrantes.add(pepe);

    servicio.asignarEquitativamente(1L, integrantes, HOGAR_ID);
    servicio.asignarEquitativamente(2L, integrantes, HOGAR_ID);
    servicio.asignarEquitativamente(3L, integrantes, HOGAR_ID);

    assertThat(tarea1.getUsuarioAsignado(), equalTo(ori));
    assertThat(tarea2.getUsuarioAsignado(), equalTo(pepe));
    assertThat(tarea3.getUsuarioAsignado(), equalTo(ori));
  }

  @Test
  public void debeRotarElResponsableSiLaTareaYaEstabaAsignada() {
    Usuario ori = new Usuario();
    ori.setId(11L);
    Usuario pepe = new Usuario();
    pepe.setId(12L);

    TareaRepetitiva tareaA = new TareaRepetitiva(1L, "compra semanal");
    TareaRepetitiva tareaB = new TareaRepetitiva(2L, "comprar facturas ricas para el desayuno");

    when(repositorioTareaRepetitiva.obtenerPorHogar(HOGAR_ID)).thenReturn(List.of(tareaA, tareaB));

    List<Usuario> integrantes = new ArrayList<>();

    integrantes.add(ori);
    integrantes.add(pepe);
    servicio.asignarEquitativamente(1L, integrantes, HOGAR_ID);
    servicio.asignarEquitativamente(2L, integrantes, HOGAR_ID);
    servicio.asignarEquitativamente(1L, integrantes, HOGAR_ID);

    assertThat(tareaA.getUsuarioAsignado(), equalTo(pepe));
  }

  @Test
  public void noDebeAsignarUnaTareaQueNoExiste() {
    List<Usuario> integrantes = new ArrayList<>();
    integrantes.add(new Usuario());

    assertThrows(
      TareaNoEncontradaException.class,
      () -> servicio.asignarEquitativamente(1L, integrantes, HOGAR_ID)
    );
  }

  @Test
  public void noDebeAsignarTareasSiElHogarNoTieneIntegrantes() {
    List<Usuario> integrantes = new ArrayList<>();
    assertThrows(
      HogarSinIntegrantesException.class,
      () -> servicio.asignarEquitativamente(1L, integrantes, HOGAR_ID)
    );
  }

  @Test
  public void debeRegistrarUnaTareaVinculadaAlHogar() {
    Hogar hogar = new Hogar("casa");
    when(repositorioHogar.buscarPorId(HOGAR_ID)).thenReturn(hogar);
    TareaRepetitiva tarea = new TareaRepetitiva(1L, "sacar la basura");

    servicio.registrar(tarea, HOGAR_ID);

    assertThat(tarea.getHogar(), equalTo(hogar));
    verify(repositorioTareaRepetitiva).guardar(tarea);
  }

  @Test
  public void debeObtenerLasTareasDelHogar() {
    List<TareaRepetitiva> tareas = List.of(new TareaRepetitiva(1L, "compra semanal"));
    when(repositorioTareaRepetitiva.obtenerPorHogar(HOGAR_ID)).thenReturn(tareas);

    List<TareaRepetitiva> obtenidas = servicio.obtenerTodas(HOGAR_ID);

    assertThat(obtenidas, equalTo(tareas));
  }
}
