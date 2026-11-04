package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.PagoPendiente;
import com.tallerwebi.dominio.RepositorioPagoPendiente;
import com.tallerwebi.dominio.Usuario;
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

  private RepositorioPagoPendiente repositorioPagoPendiente;

  @Autowired
  private SessionFactory sessionFactory;

  @BeforeEach
  public void init() {
    repositorioPagoPendiente = new RepositorioPagoPendienteImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaPoderGuardarYBuscarPagoPendientePorId() {
    // Given
    Hogar hogar = new Hogar();
    hogar.setPisoMinimoHogar(1000.0);
    sessionFactory.getCurrentSession().persist(hogar);

    Usuario emisor = new Usuario("Emisor", "emisor@kumo.com", "1234");
    Usuario receptor = new Usuario("Receptor", "receptor@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(emisor);
    sessionFactory.getCurrentSession().persist(receptor);

    PagoPendiente pagoPendiente = new PagoPendiente(emisor, receptor, 1200.0, hogar);

    // When
    repositorioPagoPendiente.guardar(pagoPendiente);
    PagoPendiente encontrado = repositorioPagoPendiente.buscarPorId(pagoPendiente.getId());

    // Then
    assertThat(encontrado, notNullValue());
    assertThat(encontrado.getMonto(), is(1200.0));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerPendientesPorHogar() {
    // Given
    Hogar hogar = new Hogar();
    hogar.setPisoMinimoHogar(1000.0);
    sessionFactory.getCurrentSession().persist(hogar);

    Usuario emisor = new Usuario("Emisor", "emisor@kumo.com", "1234");
    Usuario receptor = new Usuario("Receptor", "receptor@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(emisor);
    sessionFactory.getCurrentSession().persist(receptor);

    PagoPendiente pagoPendiente = new PagoPendiente(emisor, receptor, 1200.0, hogar);
    repositorioPagoPendiente.guardar(pagoPendiente);

    // When
    List<PagoPendiente> pendientes = repositorioPagoPendiente.obtenerPendientesPorHogar(hogar);

    // Then
    assertThat(pendientes, notNullValue());
    assertThat(pendientes, hasSize(1));
    assertThat(pendientes.get(0).getHogar().getId(), is(hogar.getId()));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaPoderBuscarPorDatosExactos() {
    // Given
    Hogar hogar = new Hogar();
    hogar.setPisoMinimoHogar(1000.0);
    sessionFactory.getCurrentSession().persist(hogar);

    Usuario emisor = new Usuario("Emisor", "emisor@kumo.com", "1234");
    Usuario receptor = new Usuario("Receptor", "receptor@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(emisor);
    sessionFactory.getCurrentSession().persist(receptor);

    PagoPendiente pagoPendiente = new PagoPendiente(emisor, receptor, 1200.0, hogar);
    repositorioPagoPendiente.guardar(pagoPendiente);

    // When
    PagoPendiente encontrado = repositorioPagoPendiente.buscarPorDatos(
      emisor,
      receptor,
      1200.0,
      hogar
    );

    // Then
    assertThat(encontrado, notNullValue());
    assertThat(encontrado.getId(), is(pagoPendiente.getId()));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaRetornarNullSiBuscaPorDatosQueNoExisten() {
    // Given
    Hogar hogar = new Hogar();
    hogar.setPisoMinimoHogar(1000.0);
    sessionFactory.getCurrentSession().persist(hogar);

    Usuario emisor = new Usuario("Emisor", "emisor@kumo.com", "1234");
    Usuario receptor = new Usuario("Receptor", "receptor@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(emisor);
    sessionFactory.getCurrentSession().persist(receptor);

    // When
    PagoPendiente encontrado = repositorioPagoPendiente.buscarPorDatos(
      emisor,
      receptor,
      9999.0,
      hogar
    );

    // Then
    assertThat(encontrado, nullValue());
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaPoderEliminarUnPagoPendienteDirectamente() {
    // Given
    Hogar hogar = new Hogar();
    hogar.setPisoMinimoHogar(1000.0);
    sessionFactory.getCurrentSession().persist(hogar);

    Usuario emisor = new Usuario("Emisor", "emisor@kumo.com", "1234");
    Usuario receptor = new Usuario("Receptor", "receptor@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(emisor);
    sessionFactory.getCurrentSession().persist(receptor);

    PagoPendiente pagoPendiente = new PagoPendiente(emisor, receptor, 1200.0, hogar);
    repositorioPagoPendiente.guardar(pagoPendiente);
    Long id = pagoPendiente.getId();

    // When
    repositorioPagoPendiente.eliminar(pagoPendiente);
    PagoPendiente encontrado = repositorioPagoPendiente.buscarPorId(id);

    // Then
    assertThat(encontrado, nullValue());
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaPoderEliminarUnPagoPendientePorId() {
    // Given
    Hogar hogar = new Hogar();
    hogar.setPisoMinimoHogar(1000.0);
    sessionFactory.getCurrentSession().persist(hogar);

    Usuario emisor = new Usuario("Emisor", "emisor@kumo.com", "1234");
    Usuario receptor = new Usuario("Receptor", "receptor@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(emisor);
    sessionFactory.getCurrentSession().persist(receptor);

    PagoPendiente pagoPendiente = new PagoPendiente(emisor, receptor, 1200.0, hogar);
    repositorioPagoPendiente.guardar(pagoPendiente);
    Long id = pagoPendiente.getId();

    // When
    repositorioPagoPendiente.eliminarPorId(id);
    PagoPendiente encontrado = repositorioPagoPendiente.buscarPorId(id);

    // Then
    assertThat(encontrado, nullValue());
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaRetornarNullSiElPagoPendienteNoExistePorId() {
    PagoPendiente encontrado = repositorioPagoPendiente.buscarPorId(999L);
    assertThat(encontrado, nullValue());
  }
}
