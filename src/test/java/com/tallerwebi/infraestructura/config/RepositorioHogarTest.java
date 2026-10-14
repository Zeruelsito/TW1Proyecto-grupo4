package com.tallerwebi.infraestructura.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.CambioParametro;
import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.RepositorioHogar;
import com.tallerwebi.infraestructura.RepositorioHogarImpl;
import jakarta.transaction.Transactional;
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
public class RepositorioHogarTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioHogar repositorioHogar;

  @BeforeEach
  public void init() {
    repositorioHogar = new RepositorioHogarImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnHogarYGenerarleUnId() {
    Hogar hogar = this.dadoQueTengoUnHogar("Departamento en Lomas de Zamora");

    this.cuandoGuardoUnHogar(hogar);

    this.entoncesSeGuardoElHogar("Departamento en Lomas de Zamora");
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarUnHogarPorId() {
    Hogar guardado = this.dadoQueExisteElHogar("departamento Palermo");

    Hogar obtenido = this.cuandoBuscoElHogarPorId(guardado.getId());

    assertThat(obtenido.getNombreHogar(), is(equalTo("departamento Palermo")));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaDevolverNuloSiElHogarNoExiste() {
    Hogar obtenido = this.cuandoBuscoElHogarPorId(99L);
    assertThat(obtenido, is(nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerTodosLosHogaresOrdenadosPorId() {
    this.dadoQueExisteElHogar("casa");
    this.dadoQueExisteElHogar("departamento");

    List<Hogar> obtenidos = this.cuandoObtengoTodosLosHogares();

    //then
    assertThat(obtenerNombres(obtenidos), contains("casa", "departamento"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarElHistorialDeCambiosJuntoConElHogar() {
    Hogar hogar = this.dadoQueTengoUnHogar("departamento palermo");
    hogar.getHistorial().add(new CambioParametro("precio hora", 1000.0, 1500.0));

    this.cuandoGuardoUnHogar(hogar);

    Hogar obtenido = this.cuandoBuscoElHogarPorId(hogar.getId());
    this.entoncesElHistorialTieneUnCAmbio(obtenido, "precio hora", 1000.0, 1500.0);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaPersistirLosCambiosHechosSobreUnHogarYaGuardado() {
    Hogar hogar = this.dadoQueExisteElHogar("departamento palermmo");
    hogar.setPrecioHoraTarea(3000.0);
    hogar.getHistorial().add(new CambioParametro("precio hora", 1600.0, 3000.0));

    Hogar obtenido = this.cuandoBuscoElHogarPorId(hogar.getId());
    assertThat(obtenido.getPrecioHoraTarea(), is(equalTo(3000.0)));
    this.entoncesElHistorialTieneUnCAmbio(obtenido, "precio hora", 1600.0, 3000.0);
  }

  private Hogar dadoQueTengoUnHogar(String nombre) {
    return new Hogar(nombre);
  }

  private void cuandoGuardoUnHogar(Hogar hogar) {
    repositorioHogar.guardar(hogar);
  }

  private void entoncesSeGuardoElHogar(String nombre) {
    String query = "select h from Hogar h where h.nombreHogar = :nombre";
    Hogar obtenido =
      this.sessionFactory.getCurrentSession()
        .createQuery(query, Hogar.class)
        .setParameter("nombre", nombre)
        .getSingleResult();

    assertThat(obtenido.getId(), is(notNullValue()));
    assertThat(obtenido.getNombreHogar(), is(equalTo(nombre)));
    assertThat(obtenido.getPrecioHoraTarea(), is(equalTo(0.0)));
    assertThat(obtenido.getPisoMinimoHogar(), is(equalTo(0.0)));
  }

  private Hogar dadoQueExisteElHogar(String nombre) {
    Hogar hogar = this.dadoQueTengoUnHogar(nombre);
    this.sessionFactory.getCurrentSession().persist(hogar);
    return hogar;
  }

  private Hogar cuandoBuscoElHogarPorId(Long id) {
    return repositorioHogar.buscarPorId(id);
  }

  private List<Hogar> cuandoObtengoTodosLosHogares() {
    return repositorioHogar.obtenerTodos();
  }

  private List<String> obtenerNombres(List<Hogar> hogares) {
    List<String> nombres = new ArrayList<>();
    for (Hogar hogar : hogares) {
      nombres.add(hogar.getNombreHogar());
    }
    return nombres;
  }

  private void entoncesElHistorialTieneUnCAmbio(Hogar hogar,String parametro, double anterior, double nuevo
  ) {
    assertThat(hogar.getHistorial().size(), is(equalTo(1)));
    assertThat(hogar.getHistorial().get(0).getParametro(), is(equalTo(parametro)));
    assertThat(hogar.getHistorial().get(0).getValorAnterior(), is(equalTo(anterior)));
    assertThat(hogar.getHistorial().get(0).getValorNuevo(), is(equalTo(nuevo)));
  }
}
