package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Propuesta;
import com.tallerwebi.dominio.ServicioVotacion;
import com.tallerwebi.dominio.excepcion.PropuestaNoExiste;
import com.tallerwebi.dominio.excepcion.UsuarioYaVoto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

public class ControladorVotacionTest {

  private static final Long USUARIO_ID = 1L;
  private static final Long HOGAR_ID = 7L;

  private ServicioVotacion servicioVotacionMock;
  private ControladorVotacion controlador;
  private MockHttpSession session;
  private RedirectAttributesModelMap redirectAttributes;

  @BeforeEach
  public void init() {
    servicioVotacionMock = mock(ServicioVotacion.class);
    controlador = new ControladorVotacion(servicioVotacionMock);
    redirectAttributes = new RedirectAttributesModelMap();

    session = new MockHttpSession();
    session.setAttribute("USUARIO_ID", USUARIO_ID);
    session.setAttribute("HOGAR_ID", HOGAR_ID);
  }

  @Test
  public void deberiaMostrarLasPropuestasDelHogarDelUsuario() {
    ModelAndView mav = controlador.irAPropuestas(session, redirectAttributes);

    assertThat(mav.getViewName(), equalTo("propuestas"));
    verify(servicioVotacionMock).obtenerPropuestasDelHogar(HOGAR_ID);
  }

  @Test
  public void deberiaGuardarLaPropuestaConElHogarYElCreadorDeLaSesion() {
    controlador.guardarPropuesta(new DatosPropuesta(), session, redirectAttributes);

    ArgumentCaptor<Propuesta> captor = ArgumentCaptor.forClass(Propuesta.class);
    verify(servicioVotacionMock).guardarPropuesta(captor.capture());
    assertThat(captor.getValue().getHogarId(), equalTo(HOGAR_ID));
    assertThat(captor.getValue().getIdUsuarioCreador(), equalTo(USUARIO_ID));
  }

  @Test
  public void siLaPropuestaEsInvalidaDeberiaMostrarElError() {
    doThrow(new PropuestaNoExiste("Error"))
      .when(servicioVotacionMock)
      .guardarPropuesta(any(Propuesta.class));

    ModelAndView mav = controlador.guardarPropuesta(
      new DatosPropuesta(),
      session,
      redirectAttributes
    );

    assertThat(mav.getModel().get("mensajeError"), equalTo("Error"));
  }

  @Test
  public void deberiaVotarConElUsuarioYElHogarDeLaSesion() {
    controlador.votar(10L, true, "Dale", session, redirectAttributes);

    verify(servicioVotacionMock).votar(USUARIO_ID, HOGAR_ID, 10L, true, "Dale");
  }

  @Test
  public void siElVotoFallaDeberiaPasarElMensajeDeError() {
    doThrow(new UsuarioYaVoto("Ya votaste"))
      .when(servicioVotacionMock)
      .votar(USUARIO_ID, HOGAR_ID, 10L, true, null);

    controlador.votar(10L, true, null, session, redirectAttributes);

    assertThat(redirectAttributes.getFlashAttributes().get("mensajeError"), equalTo("Ya votaste"));
  }
}
