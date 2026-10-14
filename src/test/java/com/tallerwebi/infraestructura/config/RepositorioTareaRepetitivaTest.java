package com.tallerwebi.infraestructura.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.RepositorioTareaRepetitiva;
import com.tallerwebi.dominio.TareaRepetitiva;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.infraestructura.RepositorioTareaRepetitivaImpl;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioTareaRepetitivaTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioTareaRepetitiva repositorioTareaRepetitiva;

  @BeforeEach
  public void init() {
    repositorioTareaRepetitiva = new RepositorioTareaRepetitivaImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnaTareaRepetitivaYGenerarleUnId() {
    //given
    TareaRepetitiva tareaRepetitiva = crearTareaRepetitiva("compra semanal");
    //when
    repositorioTareaRepetitiva.guardar(tareaRepetitiva);
    //then
    assertThat(tareaRepetitiva.getId(), notNullValue());
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerTodasLasTareasRepetitivas() {
    //given
    givenTengoUnaTareaRepetitiva("compra semanal");
    givenTengoUnaTareaRepetitiva("cargar sube");
    givenTengoUnaTareaRepetitiva("pagar alquiler");
    //when
    List<TareaRepetitiva> obtenidas = repositorioTareaRepetitiva.obtenerTodos();

    assertThat(
      obtenerTitulos(obtenidas),
      contains("compra semanal", "cargar sube", "pagar alquiler")
    );
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaDevolverUnaListaVaciaSiNoHayTareasRepetitivas() {
    //given
    List<TareaRepetitiva> obtenidas = repositorioTareaRepetitiva.obtenerTodos();

    //then
    assertThat(obtenidas, empty());
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarUnaTareaRepetitivaPorID() {
    //goviven
    givenTengoUnaTareaRepetitiva("compra semanal");
    TareaRepetitiva sube = givenTengoUnaTareaRepetitiva("cargar sube");

    //when
    TareaRepetitiva obtenida = repositorioTareaRepetitiva.obtenerPorId(sube.getId());

    //then
    assertThat(obtenida.getTitulo(), equalTo("cargar sube"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaDevolverNullSiLaTareaRepetitivaNoExiste() {
    //when
    TareaRepetitiva obtenida = repositorioTareaRepetitiva.obtenerPorId(979L);
    //then
    assertThat(obtenida, nullValue());
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaContarLasTareasRepetitivasAsignadasAUnUsuario() {
    Usuario ori = givenTengoUnUsuario("ori");
    Usuario maca = givenTengoUnUsuario("maca");
    givenTengoUnaTareaRepetitivaAsignada("compra semanal", ori);
    givenTengoUnaTareaRepetitivaAsignada("cargar sube", ori);
    givenTengoUnaTareaRepetitivaAsignada("pagar alquiler", maca);
    givenTengoUnaTareaRepetitivaAsignada("sin asignar", null);

    //when
    int tareasDeOri = repositorioTareaRepetitiva.contarAsignadasAUsuario(ori);
    int tareasDeMaca = repositorioTareaRepetitiva.contarAsignadasAUsuario(maca);

    //then
    assertThat(tareasDeOri, equalTo(2));
    assertThat(tareasDeMaca, equalTo(1));
  }

  private Usuario givenTengoUnUsuario(String string) {
    Usuario usuario = new Usuario();
    usuario.setNombre(string);
    sessionFactory.getCurrentSession().persist(usuario);
    return usuario;
  }

  private TareaRepetitiva crearTareaRepetitiva(String titulo) {
    TareaRepetitiva tareaRepetitiva = new TareaRepetitiva();
    tareaRepetitiva.setTitulo(titulo);
    return tareaRepetitiva;
  }

  private TareaRepetitiva givenTengoUnaTareaRepetitiva(String titulo) {
    TareaRepetitiva tareaRepetitiva = crearTareaRepetitiva(titulo);
    sessionFactory.getCurrentSession().persist(tareaRepetitiva);
    return tareaRepetitiva;
  }

  private List<String> obtenerTitulos(List<TareaRepetitiva> obtenidas) {
    List<String> titulos = new ArrayList<>();
    for (TareaRepetitiva tareaRepetitiva : obtenidas) {
      titulos.add(tareaRepetitiva.getTitulo());
    }
    return titulos;
  }

  private TareaRepetitiva givenTengoUnaTareaRepetitivaAsignada(String titulo, Usuario usuario) {
    TareaRepetitiva tareaRepetitiva = crearTareaRepetitiva(titulo);
    tareaRepetitiva.setUsuarioAsignado(usuario);
    sessionFactory.getCurrentSession().persist(tareaRepetitiva);
    return tareaRepetitiva;
  }
}
