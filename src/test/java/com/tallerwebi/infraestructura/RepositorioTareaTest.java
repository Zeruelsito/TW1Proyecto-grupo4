package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.RepositorioTarea;
import com.tallerwebi.dominio.Tarea;
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
public class RepositorioTareaTest {

  private RepositorioTarea repositorioTarea;

  @Autowired
  private SessionFactory sessionFactory;

  @BeforeEach
  public void init() {
    repositorioTarea = new RepositorioTareaImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaPoderGuardarYObtenerTareasConSuUsuarioAsociado() {
    // Given
    Usuario usuario = new Usuario("Maria", "maria@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(usuario);

    Tarea tarea = new Tarea("Limpiar cocina", 2.0, 1000.0, usuario);

    // When
    repositorioTarea.guardar(tarea);

    // Then
    List<Tarea> tareas = repositorioTarea.obtenerTodas();
    assertThat(tareas, hasSize(1));
    assertThat(tareas.get(0).getTitulo(), equalTo("Limpiar cocina"));
    assertThat(tareas.get(0).getUsuarioRealizador().getNombre(), equalTo("Maria"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaPoderGuardarYObtenerTodasLasTareas() {
    Usuario usuario = new Usuario("Maria", "maria@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(usuario);

    Tarea tarea = new Tarea("Limpiar cocina", 2.0, 1000.0, usuario);
    repositorioTarea.guardar(tarea);

    List<Tarea> tareas = repositorioTarea.obtenerTodas();
    assertThat(tareas, hasSize(1));
    assertThat(tareas.get(0).getTitulo(), equalTo("Limpiar cocina"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaPoderBuscarUnaTareaPorSuId() {
    // Given
    Usuario usuario = new Usuario("Maria", "maria@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(usuario);

    Tarea tarea = new Tarea("Pintar living", 4.0, 1000.0, usuario);
    repositorioTarea.guardar(tarea);

    // When
    Tarea tareaEncontrada = repositorioTarea.buscarPorId(tarea.getId());

    // Then
    assertThat(tareaEncontrada, notNullValue());
    assertThat(tareaEncontrada.getTitulo(), equalTo("Pintar living"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerSoloLasTareasAsignadasAUnUsuarioEspecifico() {
    // Given
    Usuario usuario1 = new Usuario("Maria", "maria@kumo.com", "1234");
    Usuario usuario2 = new Usuario("Juan", "juan@kumo.com", "5678");
    sessionFactory.getCurrentSession().persist(usuario1);
    sessionFactory.getCurrentSession().persist(usuario2);

    Tarea tarea1 = new Tarea("Limpiar cocina", 2.0, 1000.0, usuario1);
    Tarea tarea2 = new Tarea("Sacar basura", 1.0, 1000.0, usuario2);

    repositorioTarea.guardar(tarea1);
    repositorioTarea.guardar(tarea2);

    // When
    List<Tarea> tareasDeMaria = repositorioTarea.obtenerPorUsuario(usuario1);

    // Then
    assertThat(tareasDeMaria, hasSize(1));
    assertThat(tareasDeMaria.get(0).getTitulo(), equalTo("Limpiar cocina"));
    assertThat(tareasDeMaria.get(0).getUsuarioRealizador().getNombre(), equalTo("Maria"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerTareasFiltradasPorUsuario() {
    Usuario usuario1 = new Usuario("Maria", "maria@kumo.com", "1234");
    Usuario usuario2 = new Usuario("Carlos", "carlos@kumo.com", "5678");
    sessionFactory.getCurrentSession().persist(usuario1);
    sessionFactory.getCurrentSession().persist(usuario2);

    Tarea tarea1 = new Tarea("Tarea de Maria", 2.0, 1000.0, usuario1);
    Tarea tarea2 = new Tarea("Tarea de Carlos", 3.0, 1000.0, usuario2);
    repositorioTarea.guardar(tarea1);
    repositorioTarea.guardar(tarea2);

    List<Tarea> tareasMaria = repositorioTarea.obtenerPorUsuario(usuario1);
    assertThat(tareasMaria, hasSize(1));
    assertThat(tareasMaria.get(0).getTitulo(), equalTo("Tarea de Maria"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaRetornarListaVaciaSiElUsuarioNoTieneTareas() {
    Usuario usuario = new Usuario("Ana", "ana@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(usuario);

    List<Tarea> tareas = repositorioTarea.obtenerPorUsuario(usuario);
    assertThat(tareas, empty());
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaRetornarNullSiLaTareaNoExistePorId() {
    Tarea tareaEncontrada = repositorioTarea.buscarPorId(999L);
    assertThat(tareaEncontrada, nullValue());
  }
}
