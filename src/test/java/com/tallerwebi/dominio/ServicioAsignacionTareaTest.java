package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

  @Test
  public void debeGuardarComoPlantillaUnTituloNuevo() {
    when(repositorioHogar.buscarPorId(HOGAR_ID)).thenReturn(new Hogar("casa"));

    servicio.guardarComoPlantilla("Regar las plantas", HOGAR_ID);

    verify(repositorioTareaRepetitiva).guardar(any(TareaRepetitiva.class));
  }

  @Test
  public void noDebeGuardarComoPlantillaUnTituloVacio() {
    servicio.guardarComoPlantilla("   ", HOGAR_ID);

    verify(repositorioTareaRepetitiva, never()).guardar(any(TareaRepetitiva.class));
  }

  @Test
  public void noDebeGuardarComoPlantillaUnTituloQueYaExiste() {
    when(repositorioTareaRepetitiva.obtenerPorHogar(HOGAR_ID))
      .thenReturn(List.of(new TareaRepetitiva(1L, "Regar las plantas")));

    servicio.guardarComoPlantilla("regar las plantas", HOGAR_ID);

    verify(repositorioTareaRepetitiva, never()).guardar(any(TareaRepetitiva.class));
  }

  @Test
  public void debeGuardarComoPlantillaPersonalConSuPropietario() {
    Usuario propietario = new Usuario();
    propietario.setId(11L);
    when(repositorioHogar.buscarPorId(HOGAR_ID)).thenReturn(new Hogar("casa"));

    servicio.guardarComoPlantillaPersonal("lavar mi ropa", HOGAR_ID, propietario);

    verify(repositorioTareaRepetitiva)
      .guardar(
        argThat(plantilla ->
          plantilla.getAlcance() == AlcanceTarea.PERSONAL &&
          propietario.equals(plantilla.getPropietario())
        )
      );
  }

  @Test
  public void obtenerTodasDebeDevolverSoloLasTareasDelHogar() {
    TareaRepetitiva delHogar = new TareaRepetitiva(1L, "Limpiar el baño");
    TareaRepetitiva personal = new TareaRepetitiva(2L, "Lavar mi ropa");
    personal.setAlcance(AlcanceTarea.PERSONAL);

    when(repositorioTareaRepetitiva.obtenerPorHogar(HOGAR_ID))
      .thenReturn(List.of(delHogar, personal));

    List<TareaRepetitiva> tareas = servicio.obtenerTodas(HOGAR_ID);

    assertThat(tareas, equalTo(List.of(delHogar)));
  }

  @Test
  public void obtenerCatalogoDebeIncluirLasDelHogarYLasPersonalesDelUsuario() {
    Usuario ori = new Usuario();
    ori.setId(11L);
    Usuario pepe = new Usuario();
    pepe.setId(12L);

    TareaRepetitiva delHogar = new TareaRepetitiva(1L, "Limpiar el baño");
    TareaRepetitiva tareaA = new TareaRepetitiva(2L, "Lavar mi ropa");
    TareaRepetitiva tareaB = new TareaRepetitiva(3L, "Estudiar");

    tareaA.setAlcance(AlcanceTarea.PERSONAL);
    tareaA.setPropietario(ori);

    tareaB.setAlcance(AlcanceTarea.PERSONAL);
    tareaB.setPropietario(pepe);

    when(repositorioTareaRepetitiva.obtenerPorHogar(HOGAR_ID))
      .thenReturn(List.of(delHogar, tareaA, tareaB));

    List<TareaRepetitiva> catalogo = servicio.obtenerCatalogo(HOGAR_ID, 11L);

    assertThat(catalogo, equalTo(List.of(delHogar, tareaA)));
  }

  @Test
  public void obtenerTodasNoDebeIncluirLasPlantillasQuitadas() {
    TareaRepetitiva activa = new TareaRepetitiva(1L, "Limpiar el baño");
    TareaRepetitiva quitada = new TareaRepetitiva(2L, "Regar las plantas");
    quitada.setActiva(false);
    when(repositorioTareaRepetitiva.obtenerPorHogar(HOGAR_ID)).thenReturn(List.of(activa, quitada));

    List<TareaRepetitiva> tareas = servicio.obtenerTodas(HOGAR_ID);

    assertThat(tareas, equalTo(List.of(activa)));
  }

  @Test
  public void debeRotarAunqueElUsuarioAsignadoSeaOtraInstanciaConElMismoId() {
    Usuario oriDeLaLista = new Usuario();
    oriDeLaLista.setId(11L);
    Usuario pepe = new Usuario();
    pepe.setId(12L);
    Usuario oriDeLaBase = new Usuario();
    oriDeLaBase.setId(11L);

    TareaRepetitiva tarea = new TareaRepetitiva(1L, "compra semanal");
    tarea.setUsuarioAsignado(oriDeLaBase);
    when(repositorioTareaRepetitiva.obtenerPorHogar(HOGAR_ID)).thenReturn(List.of(tarea));

    servicio.asignarEquitativamente(1L, List.of(oriDeLaLista, pepe), HOGAR_ID);

    assertThat(tarea.getUsuarioAsignado(), equalTo(pepe));
  }

  @Test
  public void debeQuitarUnaPlantillaMarcandolaComoInactiva() {
    TareaRepetitiva plantilla = new TareaRepetitiva(1L, "Regar las plantas");
    when(repositorioTareaRepetitiva.obtenerPorHogar(HOGAR_ID)).thenReturn(List.of(plantilla));

    servicio.quitarPlantilla(1L, HOGAR_ID, 11L);

    assertThat(plantilla.isActiva(), equalTo(false));
  }

  @Test
  public void noDebeQuitarUnaPlantillaQueNoExiste() {
    assertThrows(
      TareaNoEncontradaException.class,
      () -> servicio.quitarPlantilla(99L, HOGAR_ID, 11L)
    );
  }

  @Test
  public void noDebeQuitarUnaPlantillaPersonalDeOtroUsuario() {
    Usuario propietario = new Usuario();
    propietario.setId(11L);
    TareaRepetitiva personal = new TareaRepetitiva(1L, "Lavar mi ropa");
    personal.setAlcance(AlcanceTarea.PERSONAL);
    personal.setPropietario(propietario);
    when(repositorioTareaRepetitiva.obtenerPorHogar(HOGAR_ID)).thenReturn(List.of(personal));

    assertThrows(
      TareaNoEncontradaException.class,
      () -> servicio.quitarPlantilla(1L, HOGAR_ID, 12L)
    );
    assertThat(personal.isActiva(), equalTo(true));
  }
}
