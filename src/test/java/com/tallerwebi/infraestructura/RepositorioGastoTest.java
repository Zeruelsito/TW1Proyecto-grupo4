package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tallerwebi.dominio.Gasto;
import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.RepositorioGasto;
import com.tallerwebi.dominio.TipoGasto;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
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
  public void queSePuedaGuardarUnGasto() {
    // given
    Usuario usuario = new Usuario("Macarena", "maca@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(usuario);

    Hogar hogar = new Hogar("Casa Test");
    sessionFactory.getCurrentSession().persist(hogar);

    Gasto gasto = new Gasto("G001", TipoGasto.OCASIONAL, "Cena", 1500.0, usuario, "ALIMENTACION");
    gasto.setHogar(hogar);

    // when
    repositorioGasto.guardar(gasto);

    // then
    Gasto recuperado = sessionFactory.getCurrentSession().get(Gasto.class, gasto.getId());
    assertNotNull(recuperado);
    assertEquals("Cena", recuperado.getDescripcion());
    assertEquals(1500.0, recuperado.getMonto());
    assertNotNull(recuperado.getHogar());
  }

  @Test
  @Transactional
  @Rollback
  public void queSePuedaBuscarUnGastoPorCodigo() {
    // given
    Usuario usuario = new Usuario("Macarena", "maca@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(usuario);

    Hogar hogar = new Hogar("Casa Test");
    sessionFactory.getCurrentSession().persist(hogar);

    Gasto gasto = new Gasto("G001", TipoGasto.OCASIONAL, "Cena", 1500.0, usuario, "ALIMENTACION");
    gasto.setHogar(hogar);
    repositorioGasto.guardar(gasto);

    // when
    Gasto recuperado = repositorioGasto.buscarPorCodigo("G001");

    // then
    assertNotNull(recuperado);
    assertEquals("G001", recuperado.getCodigo());
    assertEquals("Cena", recuperado.getDescripcion());
    assertEquals(1500.0, recuperado.getMonto());
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerElNombreDelUsuarioPagadorDelGasto() {
    Usuario usuario = new Usuario("Macarena", "maca@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(usuario);

    Hogar hogar = new Hogar("Casa Test");
    sessionFactory.getCurrentSession().persist(hogar);

    Gasto gasto = givenUnGasto("G001", 10000.0, usuario, hogar);
    // when
    String nombreObtenido = repositorioGasto.obtenerNombreDeUsuarioPagadorPorCodigoGasto("G001");
    // then
    assertThat(nombreObtenido, equalTo("Macarena"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaPoderGuardarYObtenerTodosLosGastosPorHogar() {
    Usuario usuario = new Usuario("Maria", "maria@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(usuario);

    Hogar hogar = new Hogar("Casa Test");
    sessionFactory.getCurrentSession().persist(hogar);

    Gasto gasto = new Gasto(
      "G001",
      TipoGasto.OCASIONAL,
      "Supermercado",
      5000.0,
      usuario,
      "ALIMENTACION"
    );
    gasto.setHogar(hogar);
    repositorioGasto.guardar(gasto);

    List<Gasto> gastos = repositorioGasto.obtenerTodosPorHogar(hogar.getId());
    assertThat(gastos.size(), equalTo(1));
    assertThat(gastos.get(0).getDescripcion(), equalTo("Supermercado"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerGastosFiltradosPorTipoYHogar() {
    Usuario usuario = new Usuario("Maria", "maria@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(usuario);

    Hogar hogar = new Hogar("Casa Test");
    sessionFactory.getCurrentSession().persist(hogar);

    Gasto gastoOcasional = new Gasto(
      "G001",
      TipoGasto.OCASIONAL,
      "Supermercado",
      5000.0,
      usuario,
      "VARIOS"
    );
    gastoOcasional.setHogar(hogar);

    Gasto gastoFijo = new Gasto("G002", TipoGasto.FIJO, "Alquiler", 50000.0, usuario, "VIVIENDA");
    gastoFijo.setHogar(hogar);

    sessionFactory.getCurrentSession().persist(gastoOcasional);
    sessionFactory.getCurrentSession().persist(gastoFijo);

    List<Gasto> gastosOcasionales = repositorioGasto.obtenerPorTipo(
      TipoGasto.OCASIONAL,
      hogar.getId()
    );

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

    Hogar hogar = new Hogar("Casa Test");
    sessionFactory.getCurrentSession().persist(hogar);

    Gasto gasto = new Gasto("G999", TipoGasto.OCASIONAL, "Internet", 12500.0, usuario, "SERVICIOS");
    gasto.setHogar(hogar);
    sessionFactory.getCurrentSession().persist(gasto);

    Double monto = repositorioGasto.obtenerMontoPorCodigo("G999");

    assertThat(monto, equalTo(12500.0));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerSoloLosPendientesQueVencenEnLosProximos30DiasOrdenadosPorFechaPorHogar() {
    LocalDateTime hoy = LocalDateTime.now();
    Usuario usuario = new Usuario("Maria", "maria@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(usuario);

    Hogar hogar = new Hogar("Casa Test");
    sessionFactory.getCurrentSession().persist(hogar);

    givenUnGastoConVencimiento("G-101", hoy.plusDays(20), false, usuario, hogar); // entra
    givenUnGastoConVencimiento("G-102", hoy.plusDays(5), false, usuario, hogar); // entra (primero)
    givenUnGastoConVencimiento("G-103", hoy.plusDays(31), false, usuario, hogar); // afuera: muy lejos
    givenUnGastoConVencimiento("G-104", hoy.minusDays(1), false, usuario, hogar); // afuera: vencido
    givenUnGastoConVencimiento("G-105", hoy.plusDays(3), true, usuario, hogar); // afuera: ya pagado
    givenUnGastoConVencimiento("G-106", null, false, usuario, hogar); // afuera: sin fecha

    List<Gasto> resultado = repositorioGasto.obtenerPendientesConVencimientoEntre(
      hoy,
      hoy.plusDays(30),
      hogar.getId()
    );

    assertThat(resultado.size(), equalTo(2));
    assertThat(resultado.get(0).getCodigo(), equalTo("G-102"));
    assertThat(resultado.get(1).getCodigo(), equalTo("G-101"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerSoloLosPendientesYaVencidosPorHogar() {
    LocalDateTime hoy = LocalDateTime.now();
    Usuario usuario = new Usuario("Maria", "maria@kumo.com", "1234");
    sessionFactory.getCurrentSession().persist(usuario);

    Hogar hogar = new Hogar("Casa Test");
    sessionFactory.getCurrentSession().persist(hogar);

    givenUnGastoConVencimiento("G-201", hoy.minusDays(2), false, usuario, hogar); // entra
    givenUnGastoConVencimiento("G-202", hoy.minusDays(1), true, usuario, hogar); // afuera: pagado
    givenUnGastoConVencimiento("G-203", hoy.plusDays(1), false, usuario, hogar); // afuera: futuro

    List<Gasto> resultado = repositorioGasto.obtenerPendientesVencidosAntesDe(hoy, hogar.getId());

    assertThat(resultado.size(), equalTo(1));
    assertThat(resultado.get(0).getCodigo(), equalTo("G-201"));
  }

  private Gasto givenUnGastoConVencimiento(
    String codigo,
    LocalDateTime vencimiento,
    boolean pagado,
    Usuario usuario,
    Hogar hogar
  ) {
    Gasto gasto = new Gasto(codigo, TipoGasto.FIJO, "Servicio", 1000.0, usuario, "SERVICIOS");
    gasto.setFechaVencimiento(vencimiento);
    gasto.setPagado(pagado);
    gasto.setHogar(hogar);
    sessionFactory.getCurrentSession().persist(gasto);
    return gasto;
  }

  private Gasto givenUnGasto(String codigo, Double monto, Usuario usuario, Hogar hogar) {
    Gasto gastoNuevo = new Gasto(
      codigo,
      TipoGasto.OCASIONAL,
      "Cena",
      monto,
      usuario,
      "ALIMENTACION"
    );
    gastoNuevo.setHogar(hogar);
    sessionFactory.getCurrentSession().persist(gastoNuevo);
    return gastoNuevo;
  }
}
