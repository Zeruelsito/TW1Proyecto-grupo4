package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Propuesta;
import com.tallerwebi.dominio.ServicioVotacion;
import com.tallerwebi.dominio.excepcion.PropuestaNoExiste;
import com.tallerwebi.dominio.excepcion.UsuarioYaVoto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorVotacionTest {

  private ControladorVotacion controladorVotacion;
  private ServicioVotacion servicioVotacionMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;

  @BeforeEach
  public void init() {
    servicioVotacionMock = mock(ServicioVotacion.class);
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    when(requestMock.getSession()).thenReturn(sessionMock);
    when(servicioVotacionMock.obtenerTodasLasPropuestas()).thenReturn(List.of());
    controladorVotacion = new ControladorVotacion(servicioVotacionMock);
  }

  @Test
  public void irAPropuestasSinSesionDeberiaRedirigirALogin() {
    // ejecucion
    ModelAndView modelAndView = controladorVotacion.irAPropuestas(requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void irAPropuestasConSesionDeberiaMostrarLasPropuestas() {
    // preparacion
    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);

    // ejecucion
    ModelAndView modelAndView = controladorVotacion.irAPropuestas(requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("propuestas"));
  }

  @Test
  public void guardarPropuestaValidaDeberiaVolverAPropuestas() {
    // preparacion
    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);

    // ejecucion
    ModelAndView modelAndView = controladorVotacion.guardarPropuesta(
      new DatosPropuesta(),
      requestMock
    );

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/propuestas"));
    verify(servicioVotacionMock, times(1)).guardarPropuesta(any(Propuesta.class));
  }

  @Test
  public void guardarPropuestaInvalidaDeberiaMostrarError() {
    // preparacion
    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
    doThrow(new PropuestaNoExiste("El campo titulo no puede estar vacío"))
      .when(servicioVotacionMock)
      .guardarPropuesta(any(Propuesta.class));

    // ejecucion
    ModelAndView modelAndView = controladorVotacion.guardarPropuesta(
      new DatosPropuesta(),
      requestMock
    );

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("propuestas"));
    assertThat(
      modelAndView.getModel().get("mensajeError").toString(),
      equalToIgnoringCase("El campo titulo no puede estar vacío")
    );
  }

  @Test
  public void votarDeberiaRegistrarElVotoYVolverAPropuestas() {
    // preparacion
    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);

    // ejecucion
    ModelAndView modelAndView = controladorVotacion.votar(5L, true, "Dale", requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/propuestas"));
    verify(servicioVotacionMock, times(1)).votar(1L, 5L, true, "Dale");
  }

  @Test
  public void votarDosVecesDeberiaMostrarError() {
    // preparacion
    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
    doThrow(new UsuarioYaVoto("El usuario ya ha votado en esta propuesta"))
      .when(servicioVotacionMock)
      .votar(anyLong(), anyLong(), anyBoolean(), any());

    // ejecucion
    ModelAndView modelAndView = controladorVotacion.votar(5L, true, null, requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("propuestas"));
    assertThat(
      modelAndView.getModel().get("mensajeError").toString(),
      equalToIgnoringCase("El usuario ya ha votado en esta propuesta")
    );
  }
}
