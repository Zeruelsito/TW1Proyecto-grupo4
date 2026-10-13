package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.EstadoItemCompra;
import com.tallerwebi.dominio.ItemCompra;
import com.tallerwebi.dominio.RepositorioItemCompra;
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
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioItemCompraTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioItemCompra repositorioItemCompra;

  @BeforeEach
  public void init() {
    repositorioItemCompra = new RepositorioItemCompraImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnItemYPoderObtenerloPorId() {
    // preparacion
    ItemCompra item = new ItemCompra("Leche", 1L, EstadoItemCompra.PENDIENTE);

    // ejecucion
    repositorioItemCompra.guardar(item);
    ItemCompra obtenido = repositorioItemCompra.obtenerPorId(item.getId());

    // validacion
    assertThat(obtenido, is(notNullValue()));
    assertThat(obtenido.getNombre(), is(equalTo("Leche")));
    assertThat(obtenido.getIdUsuarioCreador(), is(equalTo(1L)));
    assertThat(obtenido.getEstado(), is(equalTo(EstadoItemCompra.PENDIENTE)));
  }

  @Test
  @Transactional
  public void obtenerPorIdDeUnItemInexistenteDeberiaDevolverNull() {
    ItemCompra obtenido = repositorioItemCompra.obtenerPorId(999L);

    assertThat(obtenido, is(nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void obtenerActivosDeberiaDejarAfueraLosArchivados() {
    // preparacion
    ItemCompra leche = dadoQueExisteUnItem("Leche", EstadoItemCompra.EN_CARRITO);
    ItemCompra pan = dadoQueExisteUnItem("Pan", EstadoItemCompra.PENDIENTE);
    dadoQueExisteUnItem("Yerba", EstadoItemCompra.ARCHIVADO);

    // ejecucion
    List<ItemCompra> activos = repositorioItemCompra.obtenerActivos();

    // validacion
    assertThat(activos, contains(leche, pan));
  }

  @Test
  @Transactional
  @Rollback
  public void obtenerPorEstadoDeberiaTraerSoloLosItemsDeEseEstado() {
    // preparacion
    ItemCompra leche = dadoQueExisteUnItem("Leche", EstadoItemCompra.EN_CARRITO);
    dadoQueExisteUnItem("Pan", EstadoItemCompra.PENDIENTE);
    ItemCompra huevos = dadoQueExisteUnItem("Huevos", EstadoItemCompra.EN_CARRITO);

    // ejecucion
    List<ItemCompra> enCarrito = repositorioItemCompra.obtenerPorEstado(
      EstadoItemCompra.EN_CARRITO
    );

    // validacion
    assertThat(enCarrito, contains(leche, huevos));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaEliminarUnItem() {
    // preparacion
    ItemCompra item = dadoQueExisteUnItem("Leche", EstadoItemCompra.PENDIENTE);
    Long id = item.getId();

    // ejecucion
    repositorioItemCompra.eliminar(item);

    // validacion
    assertThat(repositorioItemCompra.obtenerPorId(id), is(nullValue()));
  }

  private ItemCompra dadoQueExisteUnItem(String nombre, EstadoItemCompra estado) {
    ItemCompra item = new ItemCompra(nombre, 1L, estado);
    sessionFactory.getCurrentSession().persist(item);
    return item;
  }
}
