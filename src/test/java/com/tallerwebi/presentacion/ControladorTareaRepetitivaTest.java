package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.ServicioRegistroGasto;
import com.tallerwebi.dominio.ServicioTareaRepetitiva;
import com.tallerwebi.dominio.TareaRepetitiva;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.HogarSinIntegrantesException;
import com.tallerwebi.dominio.excepcion.TareaNoEncontradaException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorTareaRepetitivaTest {

  private ServicioTareaRepetitiva servicioTareaRepetitiva;
  private ServicioRegistroGasto servicioRegistroGasto;
  private ControladorTareaRepetitiva controladorTareaRepetitiva;

  @BeforeEach
  public void init() {
    servicioTareaRepetitiva = mock(ServicioTareaRepetitiva.class);
    servicioRegistroGasto = mock(ServicioRegistroGasto.class);
    controladorTareaRepetitiva =
      new ControladorTareaRepetitiva(servicioTareaRepetitiva, servicioRegistroGasto);
  }

  @Test
  public void dadoQueExistenTareasRepetitivasCuandoConsultoLaVistaEntoncesDevuelveLaVistaConLasTareasYLosIntegrantes() {
    // given dado que existen tareas repetitivas e integrantes
    givenExistenTareasRepetitivasEIntegrantes();

    // when
    ModelAndView mav = whenIrATareasRepetitivas();

    // then
    thenLaVistaEs("tareas-repetitivas", mav);
    thenElModeloContieneLasTareasYLosIntegrantes(mav);
  }

  @Test
  public void dadaUnaTareaExistenteCuandoLaRotoEntoncesLlamaAlServicioYRedirigeATareasRepetitivas() {
    // given
    List<Usuario> integrantes = givenExistenIntegrantes();

    // when
    ModelAndView mav = whenRotoLaTarea(1L);

    // then
    thenSeAsignoLaTareaYRedirige(1L, integrantes, mav);
  }

  private List<Usuario> givenExistenIntegrantes() {
    List<Usuario> integrantes = new ArrayList<>();
    integrantes.add(new Usuario("ori", "o1@email.com", "3333"));
    integrantes.add(new Usuario("ori2", "o2@email.com", "4444"));
    when(servicioRegistroGasto.obtenerIntegrantes()).thenReturn(integrantes);
    return integrantes;
  }

  @Test
  public void dadaUnaTareaInexistenteCuandoLaRotoEntoncesMuestraUnMensajeDeError() {
    // given
    List<Usuario> integrantes = givenExistenIntegrantes();
    givenElServicioNoEncuentraLaTarea(99L, integrantes);

    //when
    ModelAndView mav = whenRotoLaTarea(99L);

    //then
    thenLaVistaEs("tareas-repetitivas", mav);
    thenElMensajeDeErrorEs("La tarea seleccionada no existe.", mav);
    thenElModeloContieneLasTareasYLosIntegrantes(mav);
  }

  @Test
  public void dadoUnHogarSinIntegrantesCuandoRotoUnaTareaEntoncesMuestraUnMensajeDeError() {
    // given
    givenElHogarNoTieneIntegrantes(1L);

    // when
    ModelAndView mav = whenRotoLaTarea(1L);

    // then
    thenLaVistaEs("tareas-repetitivas", mav);
    thenElMensajeDeErrorEs("El hogar no tiene integrantes para asignar la tarea.", mav);
    thenElModeloContieneLasTareasYLosIntegrantes(mav);
  }

  private void givenElHogarNoTieneIntegrantes(Long tareaId) {
    List<Usuario> integrantes = new ArrayList<>();
    when(servicioRegistroGasto.obtenerIntegrantes()).thenReturn(integrantes);
    doThrow(new HogarSinIntegrantesException())
      .when(servicioTareaRepetitiva)
      .asignarEquitativamente(tareaId, integrantes);
  }

  private void givenElServicioNoEncuentraLaTarea(Long tareaId, List<Usuario> integrantes) {
    doThrow(new TareaNoEncontradaException())
      .when(servicioTareaRepetitiva)
      .asignarEquitativamente(tareaId, integrantes);
  }

  private void thenElMensajeDeErrorEs(String mensajeErrorEsperado, ModelAndView mav) {
    String mensajeError = (String) mav.getModel().get("mensajeError");
    assertThat(mensajeError, equalTo(mensajeErrorEsperado));
  }

  private ModelAndView whenRotoLaTarea(Long tareaId) {
    return controladorTareaRepetitiva.rotarTarea(tareaId);
  }

  private void thenSeAsignoLaTareaYRedirige(
    Long tareaId,
    List<Usuario> integrantes,
    ModelAndView mav
  ) {
    verify(servicioTareaRepetitiva).asignarEquitativamente(tareaId, integrantes);
    assertThat(mav.getViewName(), equalTo("redirect:/tareas-repetitivas"));
  }

  // Given/When/Then

  private void givenExistenTareasRepetitivasEIntegrantes() {
    List<TareaRepetitiva> tareas = new ArrayList<>();
    tareas.add(new TareaRepetitiva(1L, "compra semanal"));
    when(servicioTareaRepetitiva.obtenerTodas()).thenReturn(tareas);

    List<Usuario> integrantes = new ArrayList<>();
    integrantes.add(new Usuario("ori", "o1@email.com", "3333"));
    when(servicioRegistroGasto.obtenerIntegrantes()).thenReturn(integrantes);
  }

  private ModelAndView whenIrATareasRepetitivas() {
    return controladorTareaRepetitiva.irATareasRepetitivas();
  }

  private void thenLaVistaEs(String nombreEsperadoDeVista, ModelAndView mav) {
    assertThat(mav.getViewName(), equalTo(nombreEsperadoDeVista));
  }

  private void thenElModeloContieneLasTareasYLosIntegrantes(ModelAndView mav) {
    assertThat(mav.getModel().get("tareasRepetitivas"), is(notNullValue()));
    assertThat(mav.getModel().get("integrantes"), is(notNullValue()));
  }
}
