package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tallerwebi.dominio.excepcion.HogarSinIntegrantesException;
import com.tallerwebi.dominio.excepcion.TareaNoEncontradaException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ServicioAsignacionTareaTest {

  @Test
  public void debeAsignarLaTareaAlIntegranteConMENOSTareasPendientes() {
    ServicioTareaRepetitiva servicio = new ServicioTareaRepetitivaImpl();

    Usuario ori = new Usuario();
    ori.setId(11L);
    Usuario pepe = new Usuario();
    pepe.setId(12L);

    TareaRepetitiva tarea1 = new TareaRepetitiva(1L, "compra semanal");
    servicio.registrar(tarea1);
    TareaRepetitiva tarea2 = new TareaRepetitiva(2L, "comprar facturas ricas para el desayuno");
    servicio.registrar(tarea2);
    TareaRepetitiva tarea3 = new TareaRepetitiva(3L, "cargar sube");
    servicio.registrar(tarea3);

    List<Usuario> integrantes = new ArrayList<>();
    integrantes.add(ori);
    integrantes.add(pepe);

    servicio.asignarEquitativamente(1L, integrantes);
    servicio.asignarEquitativamente(2L, integrantes); //si es empate gana el primero, osea en teste caso ganaria ori
    servicio.asignarEquitativamente(3L, integrantes); //por eso ori tendria 2 y pepe 1

    assertThat(tarea1.getUsuarioAsignado(), equalTo(ori));
    assertThat(tarea2.getUsuarioAsignado(), equalTo(pepe));
    assertThat(tarea3.getUsuarioAsignado(), equalTo(ori));
  }

  @Test
  public void debeRotarElResponsableSiLaTareaYaEstabaAsignada() {
    ServicioTareaRepetitiva servicio = new ServicioTareaRepetitivaImpl();

    Usuario ori = new Usuario();
    ori.setId(11L);
    Usuario pepe = new Usuario();
    pepe.setId(12L);

    TareaRepetitiva tareaA = new TareaRepetitiva(1L, "compra semanal");
    servicio.registrar(tareaA);
    TareaRepetitiva tareaB = new TareaRepetitiva(2L, "comprar facturas ricas para el desayuno");
    servicio.registrar(tareaB);

    List<Usuario> integrantes = new ArrayList<>();
    integrantes.add(ori);
    integrantes.add(pepe);

    servicio.asignarEquitativamente(1L, integrantes);
    servicio.asignarEquitativamente(2L, integrantes);
    servicio.asignarEquitativamente(1L, integrantes);

    assertThat(tareaA.getUsuarioAsignado(), equalTo(pepe));
  }

  @Test
  public void noDebeAsignarUnaTareaQueNoExiste() {
    ServicioTareaRepetitiva servicio = new ServicioTareaRepetitivaImpl();
    List<Usuario> integrantes = new ArrayList<>();
    integrantes.add(new Usuario());

    assertThrows(
      TareaNoEncontradaException.class,
      () -> servicio.asignarEquitativamente(1L, integrantes)
    );
  }

  @Test
  public void noDebeAsignarTareasSiElHogarNoTieneIntegrantes() {
    ServicioTareaRepetitiva servicio = new ServicioTareaRepetitivaImpl();
    servicio.registrar(new TareaRepetitiva(1L, "compra semanal"));

    assertThrows(
      HogarSinIntegrantesException.class,
      () -> servicio.asignarEquitativamente(1L, new ArrayList<>())
    );
  }
}
