package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.tallerwebi.dominio.EstadoPropuesta;
import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.Propuesta;
import com.tallerwebi.dominio.RepositorioPropuesta;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
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
public class RepositorioPropuestaTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioPropuesta repositorio;

  @BeforeEach
  public void init() {
    repositorio = new RepositorioPropuestaImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnaPropuestaYObtenerlaPorId() {
    Propuesta propuesta = crearPropuesta("Comprar heladera", 1L);

    repositorio.guardar(propuesta);

    assertThat(
      repositorio.obtenerPorId(propuesta.getId()).getTitulo(),
      equalTo("Comprar heladera")
    );
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaTraerSoloLasPropuestasDelHogar() {
    repositorio.guardar(crearPropuesta("Comprar heladera", 1L));
    repositorio.guardar(crearPropuesta("Pintar el living", 2L));

    List<Propuesta> resultado = repositorio.obtenerPropuestasPorHogar(1L);

    assertThat(resultado, hasSize(1));
    assertThat(resultado.get(0).getTitulo(), equalTo("Comprar heladera"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaContarSoloLosIntegrantesDelHogar() {
    Hogar casa1 = crearHogar("Casa 1", crearUsuario("eze@test.com"), crearUsuario("maca@test.com"));
    crearHogar("Casa 2", crearUsuario("pedro@test.com"));

    assertThat(repositorio.obtenerTotalIntegrantes(casa1.getId()), equalTo(2L));
  }

  private Propuesta crearPropuesta(String titulo, Long hogarId) {
    Propuesta propuesta = new Propuesta(
      titulo,
      "Descripcion de prueba",
      1L,
      LocalDate.now().plusDays(7),
      50.0,
      EstadoPropuesta.PENDIENTE
    );
    propuesta.setHogarId(hogarId);
    return propuesta;
  }

  private Usuario crearUsuario(String email) {
    Usuario usuario = new Usuario("Test", email, "123");
    sessionFactory.getCurrentSession().persist(usuario);
    return usuario;
  }

  private Hogar crearHogar(String nombre, Usuario... integrantes) {
    Hogar hogar = new Hogar(nombre);
    hogar.setIntegrantes(new ArrayList<>(List.of(integrantes)));
    sessionFactory.getCurrentSession().persist(hogar);
    return hogar;
  }
}
