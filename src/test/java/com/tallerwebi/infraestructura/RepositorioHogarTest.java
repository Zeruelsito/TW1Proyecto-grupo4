package com.tallerwebi.infraestructura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tallerwebi.dominio.*;
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
public class RepositorioHogarTest {

  private RepositorioHogar repositorioHogar;

  @Autowired
  private SessionFactory sessionFactory;

  @BeforeEach
  public void init() {
    repositorioHogar = new RepositorioHogarImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaEncontrarHogarPorNombre() {
    Usuario usuario = givenUnUsuario("Eze", "eze@prueba.com", "12345678");
    Hogar hogar1 = givenUnHogar("Departamento Ramos", usuario);
    Hogar hogar2 = givenUnHogar("Casa", usuario);

    //when
    Hogar hogarBuscado = repositorioHogar.obtenerHogarPorNombre("Casa");

    assertEquals("Casa", hogarBuscado.getNombreHogar());
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaEncontrarLosHogaresDeUnUsuario() {
    Usuario usuario1 = givenUnUsuario("Eze", "eze@prueba.com", "12345678");
    Usuario usuario2 = givenUnUsuario("Maca", "maca@prueba.com", "98765432");
    Hogar hogar1 = givenUnHogar("Departamento Ramos", usuario1);
    Hogar hogar2 = givenUnHogar("Casa", usuario2);
    Hogar hogar3 = givenUnHogar("Departamento Moron", usuario2);

    //when
    List<Hogar> hogares = repositorioHogar.obtenerHogaresDeUsuario("maca@prueba.com");

    assertEquals(2, hogares.size());
    assertEquals(hogares.get(0).getUsuarioCreador().getEmail(), usuario2.getEmail());
    assertEquals(hogares.get(1).getUsuarioCreador().getEmail(), usuario2.getEmail());
  }

  private Usuario givenUnUsuario(String nombre, String email, String password) {
    Usuario usuario = new Usuario(nombre, email, password);
    sessionFactory.getCurrentSession().persist(usuario);
    return usuario;
  }

  private Hogar givenUnHogar(String nombre, Usuario usuario) {
    Hogar hogar = new Hogar(nombre);
    hogar.setUsuarioCreador(usuario);
    sessionFactory.getCurrentSession().persist(hogar);
    return hogar;
  }
}
