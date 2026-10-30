package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.PagoPendiente;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
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
@ContextConfiguration(classes = HibernateInfraestructuraTestConfig.class)
public class RepositorioPagoPendienteTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioPagoPendienteImpl repositorioPagoPendiente;
  private Hogar hogarPrueba;

  @BeforeEach
  public void setUp() {
    repositorioPagoPendiente = new RepositorioPagoPendienteImpl(sessionFactory);

    hogarPrueba = new Hogar();
    hogarPrueba.setNombre("Casa Quinta");
    sessionFactory.getCurrentSession().save(hogarPrueba);
  }

  @Test
  @Transactional
  @Rollback
  public void queSePuedaGuardarYBuscarPagoPendientePorId() {
    PagoPendiente pendiente = new PagoPendiente("Juan", "Maria", 1500.0, hogarPrueba);

    repositorioPagoPendiente.guardar(pendiente);
    sessionFactory.getCurrentSession().flush();

    PagoPendiente encontrado = repositorioPagoPendiente.buscarPorId(pendiente.getId());

    assertThat(encontrado, notNullValue());
    assertThat(encontrado.getEmisor(), equalTo("Juan"));
    assertThat(encontrado.getReceptor(), equalTo("Maria"));
    assertThat(encontrado.getMonto(), equalTo(1500.0));
  }

  @Test
  @Transactional
  @Rollback
  public void queSePuedanObtenerPendientesPorHogar() {
    PagoPendiente pendiente1 = new PagoPendiente("Juan", "Maria", 1000.0, hogarPrueba);
    PagoPendiente pendiente2 = new PagoPendiente("Pedro", "Sofia", 2500.0, hogarPrueba);

    repositorioPagoPendiente.guardar(pendiente1);
    repositorioPagoPendiente.guardar(pendiente2);
    sessionFactory.getCurrentSession().flush();

    List<PagoPendiente> pendientesDelHogar = repositorioPagoPendiente.obtenerPendientesPorHogar(
      hogarPrueba
    );

    assertThat(pendientesDelHogar, notNullValue());
    assertThat(pendientesDelHogar, hasSize(2));
    assertThat(pendientesDelHogar, containsInAnyOrder(pendiente1, pendiente2));
  }

  @Test
  @Transactional
  @Rollback
  public void queSePuedaBuscarPagoPendientePorDatosExactos() {
    PagoPendiente pendiente = new PagoPendiente("Juan", "Maria", 1200.0, hogarPrueba);
    repositorioPagoPendiente.guardar(pendiente);
    sessionFactory.getCurrentSession().flush();

    PagoPendiente encontrado = repositorioPagoPendiente.buscarPorDatos(
      "Juan",
      "Maria",
      1200.0,
      hogarPrueba
    );

    assertThat(encontrado, notNullValue());
    assertThat(encontrado.getId(), equalTo(pendiente.getId()));
  }

  @Test
  @Transactional
  @Rollback
  public void queRetorneNullSiNoEncuentraPorDatosExactos() {
    PagoPendiente encontrado = repositorioPagoPendiente.buscarPorDatos(
      "Inexistente",
      "Nadie",
      999.0,
      hogarPrueba
    );
    assertThat(encontrado, nullValue());
  }

  @Test
  @Transactional
  @Rollback
  public void queSePuedaEliminarUnPagoPendiente() {
    PagoPendiente pendiente = new PagoPendiente("Juan", "Maria", 800.0, hogarPrueba);
    repositorioPagoPendiente.guardar(pendiente);
    sessionFactory.getCurrentSession().flush();

    Long id = pendiente.getId();

    repositorioPagoPendiente.eliminar(pendiente);
    sessionFactory.getCurrentSession().flush();

    PagoPendiente encontrado = repositorioPagoPendiente.buscarPorId(id);
    assertThat(encontrado, nullValue());
  }

  @Test
  @Transactional
  @Rollback
  public void queSePuedaEliminarUnPagoPendientePorId() {
    PagoPendiente pendiente = new PagoPendiente("Juan", "Maria", 800.0, hogarPrueba);
    repositorioPagoPendiente.guardar(pendiente);
    sessionFactory.getCurrentSession().flush();

    Long id = pendiente.getId();

    repositorioPagoPendiente.eliminarPorId(id);
    sessionFactory.getCurrentSession().flush();

    PagoPendiente encontrado = repositorioPagoPendiente.buscarPorId(id);
    assertThat(encontrado, nullValue());
  }
}
