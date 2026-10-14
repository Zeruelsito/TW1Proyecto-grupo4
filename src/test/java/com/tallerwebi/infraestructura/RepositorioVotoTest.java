package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.tallerwebi.dominio.RepositorioVoto;
import com.tallerwebi.dominio.Voto;
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
public class RepositorioVotoTest {

  private static final Long PROPUESTA = 100L;
  private static final Long OTRA_PROPUESTA = 200L;
  private static final Long USUARIO_1 = 1L;
  private static final Long USUARIO_2 = 2L;
  private static final Long USUARIO_3 = 3L;

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioVoto repositorioVoto;

  @BeforeEach
  public void init() {
    repositorioVoto = new RepositorioVotoImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnVotoYPoderObtenerloPorId() {
    // preparacion
    Voto voto = new Voto(USUARIO_1, PROPUESTA, true, "De acuerdo");

    // ejecucion
    repositorioVoto.guardar(voto);
    Voto obtenido = repositorioVoto.obtenerPorId(voto.getIdVoto());

    // validacion
    assertThat(obtenido, is(notNullValue()));
    assertThat(obtenido.getIdUsuario(), is(equalTo(USUARIO_1)));
    assertThat(obtenido.getIdPropuesta(), is(equalTo(PROPUESTA)));
    assertThat(obtenido.isEsAfirmativo(), is(true));
    assertThat(obtenido.getComentario(), is(equalTo("De acuerdo")));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerSoloLosVotosDeUnaPropuesta() {
    // preparacion
    Voto voto1 = dadoQueExisteUnVoto(USUARIO_1, PROPUESTA, true);
    Voto voto2 = dadoQueExisteUnVoto(USUARIO_2, PROPUESTA, false);
    dadoQueExisteUnVoto(USUARIO_1, OTRA_PROPUESTA, true);

    // ejecucion
    List<Voto> votos = repositorioVoto.obtenerVotosPorPropuesta(PROPUESTA);

    // validacion
    assertThat(votos, containsInAnyOrder(voto1, voto2));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerTodosLosVotos() {
    // preparacion
    Voto voto1 = dadoQueExisteUnVoto(USUARIO_1, PROPUESTA, true);
    Voto voto2 = dadoQueExisteUnVoto(USUARIO_2, OTRA_PROPUESTA, false);

    // ejecucion
    List<Voto> votos = repositorioVoto.obtenerTodosLosVotos();

    // validacion
    assertThat(votos, hasItems(voto1, voto2));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerSoloLosVotosDeUnUsuario() {
    // preparacion
    Voto voto1 = dadoQueExisteUnVoto(USUARIO_1, PROPUESTA, true);
    Voto voto2 = dadoQueExisteUnVoto(USUARIO_1, OTRA_PROPUESTA, false);
    dadoQueExisteUnVoto(USUARIO_2, PROPUESTA, true);

    // ejecucion
    List<Voto> votos = repositorioVoto.obtenerVotosPorUsuario(USUARIO_1);

    // validacion
    assertThat(votos, containsInAnyOrder(voto1, voto2));
  }

  @Test
  @Transactional
  @Rollback
  public void existeVotoDeberiaSerVerdaderoSiElUsuarioYaVotoEsaPropuesta() {
    dadoQueExisteUnVoto(USUARIO_1, PROPUESTA, true);

    assertThat(repositorioVoto.existeVoto(USUARIO_1, PROPUESTA), is(true));
  }

  @Test
  @Transactional
  @Rollback
  public void existeVotoDeberiaSerFalsoSiElUsuarioNoVotoEsaPropuesta() {
    dadoQueExisteUnVoto(USUARIO_1, OTRA_PROPUESTA, true);

    assertThat(repositorioVoto.existeVoto(USUARIO_1, PROPUESTA), is(false));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaContarVotosAfirmativosYNegativosDeUnaPropuesta() {
    // preparacion
    dadoQueExisteUnVoto(USUARIO_1, PROPUESTA, true);
    dadoQueExisteUnVoto(USUARIO_2, PROPUESTA, true);
    dadoQueExisteUnVoto(USUARIO_3, PROPUESTA, false);
    dadoQueExisteUnVoto(USUARIO_1, OTRA_PROPUESTA, false);

    // ejecucion
    Long afirmativos = repositorioVoto.obtenerVotosAfirmativosPorPropuesta(PROPUESTA);
    Long negativos = repositorioVoto.obtenerVotosNegativosPorPropuesta(PROPUESTA);

    // validacion
    assertThat(afirmativos, is(equalTo(2L)));
    assertThat(negativos, is(equalTo(1L)));
  }

  private Voto dadoQueExisteUnVoto(Long idUsuario, Long idPropuesta, boolean esAfirmativo) {
    Voto voto = new Voto(idUsuario, idPropuesta, esAfirmativo, "comentario");
    sessionFactory.getCurrentSession().persist(voto);
    return voto;
  }
}
