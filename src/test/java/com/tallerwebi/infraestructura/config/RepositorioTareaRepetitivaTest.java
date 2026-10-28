package com.tallerwebi.infraestructura.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.FrecuenciaTarea;
import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.RepositorioTareaRepetitiva;
import com.tallerwebi.dominio.TareaRepetitiva;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.infraestructura.RepositorioTareaRepetitivaImpl;
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
    TareaRepetitiva compra = givenTengoUnaTareaRepetitiva("compra semanal");
    TareaRepetitiva sube = givenTengoUnaTareaRepetitiva("cargar sube");
    TareaRepetitiva alquiler = givenTengoUnaTareaRepetitiva("pagar alquiler");
    //when
    List<TareaRepetitiva> obtenidas = repositorioTareaRepetitiva.obtenerTodos();

    assertThat(obtenidas.size(), equalTo(3));
    assertThat(obtenidas.get(0), equalTo(compra));
    assertThat(obtenidas.get(1), equalTo(sube));
    assertThat(obtenidas.get(2), equalTo(alquiler));
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
    TareaRepetitiva sube = givenTengoUnaTareaRepetitiva("cargar sube");

    //when
    TareaRepetitiva obtenida = repositorioTareaRepetitiva.obtenerPorId(sube.getId());

    //then
    assertThat(obtenida, equalTo(sube));
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
  public void deberiaContarLasTareasRepetitivasAsignadasAUnUsuario() { //FUNCIONA EN TODOS LOS HOGARES, NO SOLO EN EL HOGAR DEL USUARIO
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

  //
  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarYRecuperarUnaTareaConFrecuenciaValorPorHoraYHogar() {
    Hogar hogar = givenTengoUnHogar("casa de ori", 1000.00);
    TareaRepetitiva tarea = givenTengoUnaTareaRepetitiva("compra diaria");
    tarea.setFrecuencia(FrecuenciaTarea.DIARIA);
    tarea.setValorPorHora(1500.00);
    tarea.setHogar(hogar);

    TareaRepetitiva obtenida = repositorioTareaRepetitiva.obtenerPorId(tarea.getId());

    assertThat(obtenida.getFrecuencia(), equalTo(FrecuenciaTarea.DIARIA));
    assertThat(obtenida.getValorPorHora(), equalTo(1500.00));
    assertThat(obtenida.getHogar().getId(), equalTo(hogar.getId()));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerElValorPorHoraPropioDeLaTareaOElDelHogarSiNoTiene() {
    Hogar hogar = givenTengoUnHogar("casa de ori", 1000.00);
    TareaRepetitiva tarea = givenTengoUnaTareaRepetitiva("compra diaria");
    tarea.setHogar(hogar);

    Double valorCompra = repositorioTareaRepetitiva
      .obtenerPorId(tarea.getId())
      .obtenerValorPorHora();

    assertThat(valorCompra, equalTo(1000.00));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerSoloLasTareasRepetitivasDeUnHogarOrdenadasPorId() {
    Hogar hogarA = givenTengoUnHogar("casa de ori", 1000.00);
    Hogar hogarB = givenTengoUnHogar("depa de ori", 500.00);
    TareaRepetitiva compra = givenTengoUnaTareaRepetitiva("compra diaria");
    compra.setHogar(hogarA);
    TareaRepetitiva alquiler = givenTengoUnaTareaRepetitiva("pagar alquiler");
    alquiler.setHogar(hogarB);
    TareaRepetitiva sube = givenTengoUnaTareaRepetitiva("cargar sube");
    sube.setHogar(hogarA);

    List<TareaRepetitiva> obtenidas = repositorioTareaRepetitiva.obtenerPorHogar(hogarA.getId());

    assertThat(obtenidas.size(), equalTo(2));
    assertThat(obtenidas.get(0), equalTo(compra));
    assertThat(obtenidas.get(1), equalTo(sube));
  }

  private Hogar givenTengoUnHogar(String nombre, Double precioHora) {
    Hogar hogar = new Hogar(nombre);
    hogar.setPrecioHoraTarea(precioHora);
    sessionFactory.getCurrentSession().persist(hogar);
    return hogar;
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

  private TareaRepetitiva givenTengoUnaTareaRepetitivaAsignada(String titulo, Usuario usuario) {
    TareaRepetitiva tareaRepetitiva = crearTareaRepetitiva(titulo);
    tareaRepetitiva.setUsuarioAsignado(usuario);
    sessionFactory.getCurrentSession().persist(tareaRepetitiva);
    return tareaRepetitiva;
  }
}
