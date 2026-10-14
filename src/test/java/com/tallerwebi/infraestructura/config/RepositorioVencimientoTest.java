package com.tallerwebi.infraestructura.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.tallerwebi.dominio.RepositorioVencimiento;
import com.tallerwebi.dominio.Vencimiento;
import com.tallerwebi.infraestructura.RepositorioVencimientoImpl;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioVencimientoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioVencimiento repositorioVencimiento;

  @BeforeEach
  public void init() {
    repositorioVencimiento = new RepositorioVencimientoImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnVencimientoYGenerarleUnIdTest() {
    //Given
    Vencimiento vencimiento = this.givenTengoUnVencimiento("Luz", LocalDate.now().plusDays(5));

    //when
    this.cuandoGuardoUnVencimiento(vencimiento);

    //thenb entonces
    this.thenSeGuardoElVencimiento(vencimiento);
    assertThat(vencimiento.getId(), notNullValue());
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerSoloLosVencimientosDesdeUnaFechaIncluyendoLaMismaTest() {
    //given
    this.cuandoGuardoUnVencimiento(
        this.givenTengoUnVencimiento("Vencido1", LocalDate.now().minusDays(30))
      );
    this.cuandoGuardoUnVencimiento(
        this.givenTengoUnVencimiento("Vencido", LocalDate.now().minusDays(1))
      );
    this.cuandoGuardoUnVencimiento(this.givenTengoUnVencimiento("Hoy", LocalDate.now()));
    this.cuandoGuardoUnVencimiento(
        this.givenTengoUnVencimiento("Futuro", LocalDate.now().plusDays(3))
      );
    this.cuandoGuardoUnVencimiento(
        this.givenTengoUnVencimiento("Lejano", LocalDate.now().plusDays(10))
      );

    //when
    List<Vencimiento> obtenidos = this.whenObtengoLosVencimientosDesde(LocalDate.now());

    //then
    assertThat(obtenerTitulos(obtenidos), contains("Hoy", "Futuro", "Lejano"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerTodosLosVencimientosOrdenadosPorFechaTest() {
    //given
    this.cuandoGuardoUnVencimiento(
        this.givenTengoUnVencimiento("Gas", LocalDate.now().plusDays(20))
      );
    this.cuandoGuardoUnVencimiento(
        this.givenTengoUnVencimiento("Luz", LocalDate.now().plusDays(5))
      );
    this.cuandoGuardoUnVencimiento(
        this.givenTengoUnVencimiento("Internet", LocalDate.now().plusDays(9))
      );
    //wehn
    List<Vencimiento> obtenidos = this.whenObtengoTodosLosVencimientos();

    //then
    assertThat(obtenidos.size(), equalTo(3));
    assertThat(obtenidos.get(0).getTitulo(), equalTo("Luz"));
    assertThat(obtenidos.get(1).getTitulo(), equalTo("Internet"));
    assertThat(obtenidos.get(2).getTitulo(), equalTo("Gas"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaDevolverUnaListaVaciaSiNoHayVencimientosTest() {
    //given no le doy nada asi esta vacia
    // When
    List<Vencimiento> obtenidos = this.whenObtengoTodosLosVencimientos();

    // Then
    assertThat(obtenidos, empty());
  }

  private Vencimiento givenTengoUnVencimiento(String titulo, LocalDate fecha) {
    Vencimiento ven = new Vencimiento();
    ven.setTitulo(titulo);
    ven.setFechaVencimiento(fecha);
    return ven;
  }

  private void cuandoGuardoUnVencimiento(Vencimiento vencimiento) {
    repositorioVencimiento.guardar(vencimiento);
  }

  private Vencimiento thenSeGuardoElVencimiento(Vencimiento esperado) { //llama
    String query = "select v from Vencimiento v where v.titulo = :titulo";
    return this.sessionFactory.getCurrentSession()
      .createQuery(query, Vencimiento.class)
      .setParameter("titulo", esperado.getTitulo())
      .getSingleResult();
  }

  private List<Vencimiento> whenObtengoLosVencimientosDesde(LocalDate fecha) {
    return repositorioVencimiento.obtenerDesde(fecha);
  }

  private List<String> obtenerTitulos(List<Vencimiento> vencimientos) {
    List<String> titulos = new ArrayList<>();
    for (Vencimiento vencimiento : vencimientos) {
      titulos.add(vencimiento.getTitulo());
    }
    return titulos;
  }

  private List<Vencimiento> whenObtengoTodosLosVencimientos() {
    return repositorioVencimiento.obtenerTodosOrdenadosPorFecha();
  }
}
