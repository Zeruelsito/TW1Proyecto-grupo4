package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.EstadoPropuesta;
import com.tallerwebi.dominio.Propuesta;
import com.tallerwebi.dominio.RepositorioPropuesta;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
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
public class RepositorioPropuestaTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioPropuesta repositorioPropuesta;

  @BeforeEach
  public void init() {
    repositorioPropuesta = new RepositorioPropuestaImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnaPropuestaYPoderObtenerlaPorId() {
    // preparacion
    Propuesta propuesta = dadoQueTengoUnaPropuesta("Comprar heladera");

    // ejecucion
    repositorioPropuesta.guardar(propuesta);
    Propuesta obtenida = repositorioPropuesta.obtenerPorId(propuesta.getId());

    // validacion
    assertThat(obtenida, is(notNullValue()));
    assertThat(obtenida.getTitulo(), is(equalTo("Comprar heladera")));
    assertThat(obtenida.getEstado(), is(equalTo(EstadoPropuesta.PENDIENTE)));
  }

  @Test
  @Transactional
  public void obtenerPorIdDeUnaPropuestaInexistenteDeberiaDevolverNull() {
    Propuesta obtenida = repositorioPropuesta.obtenerPorId(999L);

    assertThat(obtenida, is(nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerTodasLasPropuestasGuardadas() {
    // preparacion
    Propuesta heladera = dadoQueTengoUnaPropuesta("Comprar heladera");
    Propuesta pintura = dadoQueTengoUnaPropuesta("Pintar el living");
    repositorioPropuesta.guardar(heladera);
    repositorioPropuesta.guardar(pintura);

    // ejecucion
    List<Propuesta> todas = repositorioPropuesta.obtenerTodasLasPropuestas();

    // validacion
    assertThat(todas, hasItems(heladera, pintura));
  }

  @Test
  @Transactional
  @Rollback
  public void obtenerTotalIntegrantesDeberiaContarLosUsuariosGuardados() {
    // preparacion
    long antes = repositorioPropuesta.obtenerTotalIntegrantes();
    sessionFactory.getCurrentSession().persist(new Usuario("Eze", "eze@test.com", "123"));
    sessionFactory.getCurrentSession().persist(new Usuario("Maca", "maca@test.com", "123"));

    // ejecucion
    long despues = repositorioPropuesta.obtenerTotalIntegrantes();

    // validacion
    assertThat(despues, is(equalTo(antes + 2)));
  }

  private Propuesta dadoQueTengoUnaPropuesta(String titulo) {
    return new Propuesta(
      titulo,
      "Descripcion de prueba",
      1L,
      LocalDate.now().plusDays(7),
      50.0,
      EstadoPropuesta.PENDIENTE
    );
  }
}
