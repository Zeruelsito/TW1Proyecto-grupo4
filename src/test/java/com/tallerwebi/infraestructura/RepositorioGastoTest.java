package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tallerwebi.dominio.Gasto;
import com.tallerwebi.dominio.RepositorioGasto;
import com.tallerwebi.dominio.TipoGasto;
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
public class RepositorioGastoTest {

  private RepositorioGasto repositorioGasto;

  @Autowired
  private SessionFactory sessionFactory;

  @BeforeEach
  public void init() {
    repositorioGasto = new RepositorioGastoImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarYBuscarPorCodigo() {
    Usuario usuario = new Usuario("Macarena", "maca@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(usuario);
    givenUnGasto("G001", 1500.0, usuario);

    //when
    Gasto recuperado = repositorioGasto.buscarPorCodigo("G001");

    assertNotNull(recuperado);
    assertEquals("Cena", recuperado.getDescripcion());
    assertEquals(1500.0, recuperado.getMonto());
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerElNombreDelUsuarioPagadorDelGasto() {
    Usuario usuario = new Usuario("Macarena", "maca@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(usuario);
    Gasto gasto = givenUnGasto("G001", 10000.0, usuario);
    //when
    String nombreObtenido = repositorioGasto.obtenerNombreDeUsuarioPagadorPorCodigoGasto("G001");
    //then
    assertThat(nombreObtenido, equalTo("Macarena"));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnUsuarioDeberiaObtenerUnaListaDeSusGastos() {
    Usuario usuario = new Usuario("Macarena", "maca@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(usuario);
    Usuario usuario2 = new Usuario("Ezequiel", "eze@kumo.com", "5678");
    sessionFactory.getCurrentSession().persist(usuario2);
    Gasto gasto = givenUnGasto("G001", 10000.0, usuario);
    Gasto gasto2 = givenUnGasto("G002", 5000.0, usuario);
    Gasto gasto3 = givenUnGasto("G003", 20000.0, usuario);
    Gasto gasto4 = givenUnGasto("G004", 40000.0, usuario2);
    //when
    //List<Gasto> gastosDeUsuario = repositorioGasto.obtenerListaDeGastosPorUsuario(usuario);

  }

  @Test
  @Transactional
  @Rollback
  public void deberiaPoderGuardarYObtenerTodosLosGastos() {
    Usuario usuario = new Usuario("Maria", "maria@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(usuario);

    Gasto gasto = new Gasto(
      "G001",
      TipoGasto.OCASIONAL,
      "Supermercado",
      5000.0,
      usuario,
      "ALIMENTACION"
    );
    repositorioGasto.guardar(gasto);

    List<Gasto> gastos = repositorioGasto.obtenerTodos();
    assertThat(gastos.size(), equalTo(1));
    assertThat(gastos.get(0).getDescripcion(), equalTo("Supermercado"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerGastosFiltradosPorTipo() {
    Usuario usuario = new Usuario("Maria", "maria@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(usuario);

    Gasto gastoOcasional = new Gasto(
      "G001",
      TipoGasto.OCASIONAL,
      "Supermercado",
      5000.0,
      usuario,
      "VARIOS"
    );
    Gasto gastoFijo = new Gasto("G002", TipoGasto.FIJO, "Alquiler", 50000.0, usuario, "VIVIENDA");

    sessionFactory.getCurrentSession().persist(gastoOcasional);
    sessionFactory.getCurrentSession().persist(gastoFijo);

    List<Gasto> gastosOcasionales = repositorioGasto.obtenerPorTipo(TipoGasto.OCASIONAL);

    assertThat(gastosOcasionales.size(), equalTo(1));
    assertThat(gastosOcasionales.get(0).getCodigo(), equalTo("G001"));
    assertThat(gastosOcasionales.get(0).getTipoGasto(), equalTo(TipoGasto.OCASIONAL));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerElMontoDeUnGastoPorSuCodigo() {
    Usuario usuario = new Usuario("Juan", "juan@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(usuario);

    Gasto gasto = new Gasto("G999", TipoGasto.OCASIONAL, "Internet", 12500.0, usuario, "SERVICIOS");
    sessionFactory.getCurrentSession().persist(gasto);

    Double monto = repositorioGasto.obtenerMontoPorCodigo("G999");

    assertThat(monto, equalTo(12500.0));
  }

  private Gasto givenUnGasto(String codigo, Double monto, Usuario usuario) {
    Gasto gastoNuevo = new Gasto(
      codigo,
      TipoGasto.OCASIONAL,
      "Cena",
      monto,
      usuario,
      "ALIMENTACION"
    );
    repositorioGasto.guardar(gastoNuevo);
    return gastoNuevo;
  }
}
