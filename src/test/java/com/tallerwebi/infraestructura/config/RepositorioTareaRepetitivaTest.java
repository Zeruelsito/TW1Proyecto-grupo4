package com.tallerwebi.infraestructura.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.AlcanceTarea;
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

  @Test
  public void deberiaConsiderarDelHogarUnaTareaSinAlcance() {
    TareaRepetitiva tarea = crearTareaRepetitiva("compra semanal");

    AlcanceTarea alcance = tarea.getAlcance();

    assertThat(alcance, equalTo(AlcanceTarea.HOGAR));
  }

  @Test
  public void deberiaSerVisibleSoloParaSuPropietarioUnaTareaPersonal() {
    Usuario propietario = new Usuario();
    propietario.setId(11L);
    TareaRepetitiva tarea = crearTareaRepetitiva("lavar mi ropa");
    tarea.setAlcance(AlcanceTarea.PERSONAL);
    tarea.setPropietario(propietario);

    boolean visibleParaElPropietario = tarea.esVisiblePara(11L);
    boolean visibleParaOtro = tarea.esVisiblePara(12L);

    assertThat(visibleParaElPropietario, equalTo(true));
    assertThat(visibleParaOtro, equalTo(false));
  }

  // agrego

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerLasDelHogarYLasPersonalesDelUsuarioSinTraerOtrasNiInactivas() {
    Hogar hogarA = givenTengoUnHogar("depto 1", 1000.00);
    Hogar hogarB = givenTengoUnHogar("depto 2", 1000.00);
    Usuario ori = givenTengoUnUsuario("ori");
    Usuario dylan = givenTengoUnUsuario("dylan");
    
    TareaRepetitiva delHogar = givenTengoUnaTareaEnHogar("comprar manteles", hogarA);
    TareaRepetitiva deOri = givenTengoUnaTareaPersonal("lavar mi ropa", hogarA, ori);
    givenTengoUnaTareaPersonal("estudiar", hogarA, dylan);
    givenTengoUnaTareaEnHogar("pagar alquiler", hogarB);
    TareaRepetitiva inactiva = givenTengoUnaTareaEnHogar("regar plantas", hogarA);
    inactiva.setActiva(false);

    List<TareaRepetitiva> obtenidas = repositorioTareaRepetitiva.obtenerPorHogarYUsuario(
      hogarA.getId(),
      ori.getId());

    assertThat(obtenidas.size(), equalTo(2));
    assertThat(obtenidas.get(0), equalTo(delHogar));
    assertThat(obtenidas.get(1), equalTo(deOri));
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaVerUnaTareaDeHogarDeOtroHogar() {
    Hogar hogarA = givenTengoUnHogar("depto 1", 1000.00);
    Hogar hogarB = givenTengoUnHogar("depto 2", 1000.00);

    Usuario ori = givenTengoUnUsuario("ori");
    givenTengoUnaTareaEnHogar("comprar manteles", hogarA);

    List<TareaRepetitiva> obtenidas = repositorioTareaRepetitiva.obtenerPorHogarYUsuario(hogarB.getId(), ori.getId()
    );

    assertThat(obtenidas, empty());
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerPorHogarUsuarioYTituloUnaTareaDelHogar() {
    Hogar hogar = givenTengoUnHogar("depto 1", 1000.00);
    Usuario ori = givenTengoUnUsuario("ori");
    TareaRepetitiva manteles = givenTengoUnaTareaEnHogar("comprar manteles", hogar);

    TareaRepetitiva obtenida = repositorioTareaRepetitiva.obtenerPorHogarUsuarioYTitulo(
      hogar.getId(),
      ori.getId(),
      "comprar manteles"
    );

    assertThat(obtenida, equalTo(manteles));
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaObtenerPorTituloUnaTareaDeOtroHogarNiPersonalDeOtroUsuario() {
    Hogar hogarA = givenTengoUnHogar("depto 1", 1000.00);
    Hogar hogarB = givenTengoUnHogar("depto 2", 1000.00);
    Usuario ori = givenTengoUnUsuario("ori");
    Usuario dylan = givenTengoUnUsuario("dylan");
    givenTengoUnaTareaEnHogar("comprar manteles", hogarB);
    givenTengoUnaTareaPersonal("comprar pinceles", hogarA, dylan);

    TareaRepetitiva deOtroHogar = repositorioTareaRepetitiva.obtenerPorHogarUsuarioYTitulo(
      hogarA.getId(),
      ori.getId(),
      "comprar manteles"
    );
    TareaRepetitiva deOtroUsuario = repositorioTareaRepetitiva.obtenerPorHogarUsuarioYTitulo(
      hogarA.getId(),
      ori.getId(),
      "comprar pinceles"
    );

    assertThat(deOtroHogar, nullValue());
    assertThat(deOtroUsuario, nullValue());
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaObtenerPorTituloUnaTareaInactiva() {
    Hogar hogar = givenTengoUnHogar("depto", 1000.00);
    Usuario ori = givenTengoUnUsuario("ori");
    TareaRepetitiva quitada = givenTengoUnaTareaEnHogar("comprar manteles", hogar);
    quitada.setActiva(false);

    TareaRepetitiva obtenida = repositorioTareaRepetitiva.obtenerPorHogarUsuarioYTitulo(
      hogar.getId(),
      ori.getId(),
      "comprar manteles"
    );

    assertThat(obtenida, nullValue());
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
    tareaRepetitiva.setActiva(true);
    sessionFactory.getCurrentSession().persist(tareaRepetitiva);
    return tareaRepetitiva;
  }

  private TareaRepetitiva givenTengoUnaTareaEnHogar(String titulo, Hogar hogar) {
    TareaRepetitiva tarea = givenTengoUnaTareaRepetitiva(titulo);
    tarea.setHogar(hogar);
    tarea.setAlcance(AlcanceTarea.HOGAR);
    return tarea;
  }

  private TareaRepetitiva givenTengoUnaTareaPersonal(
    String titulo,
    Hogar hogar,
    Usuario propietario
  ) {
    TareaRepetitiva tarea = givenTengoUnaTareaEnHogar(titulo, hogar);
    tarea.setAlcance(AlcanceTarea.PERSONAL);
    tarea.setPropietario(propietario);
    return tarea;
  }

  private TareaRepetitiva givenTengoUnaTareaRepetitivaAsignada(String titulo, Usuario usuario) {
    TareaRepetitiva tareaRepetitiva = crearTareaRepetitiva(titulo);
    tareaRepetitiva.setUsuarioAsignado(usuario);
    sessionFactory.getCurrentSession().persist(tareaRepetitiva);
    return tareaRepetitiva;
  }
}
