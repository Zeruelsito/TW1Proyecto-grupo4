package com.tallerwebi.presentacion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tallerwebi.dominio.Propuesta;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.ServicioVotacion;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.PropuestaNoExiste;
import com.tallerwebi.dominio.excepcion.UsuarioYaVoto;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

public class ControladorVotacionTest {

  private MockMvc mockMvc;
  private ServicioVotacion servicioVotacionMock;
  private ServicioUsuario servicioUsuarioMock;
  private HttpSession sessionMock;
  private ControladorVotacion controladorVotacion;

  @BeforeEach
  public void setUp() {
    servicioVotacionMock = mock(ServicioVotacion.class);
    servicioUsuarioMock = mock(ServicioUsuario.class);
    sessionMock = mock(HttpSession.class);

    controladorVotacion =
      new ControladorVotacion(servicioVotacionMock, servicioUsuarioMock, sessionMock);

    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setPrefix("/WEB-INF/views/");
    viewResolver.setSuffix(".jsp");

    mockMvc =
      MockMvcBuilders.standaloneSetup(controladorVotacion).setViewResolvers(viewResolver).build();
  }

  @Test
  public void queRedirijaAlDashboardSiNoHayUsuarioLogueadoEnIrAPropuestas() throws Exception {
    mockMvc
      .perform(get("/propuestas"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/dashboard"));
  }

  @Test
  public void queMuestreLaVistaDePropuestasSiElUsuarioEstaLogueado() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Juan");

    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
    when(sessionMock.getAttribute("HOGAR_ID")).thenReturn(1L);
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioVotacionMock.obtenerTodasLasPropuestas()).thenReturn(Collections.emptyList());

    mockMvc
      .perform(get("/propuestas").sessionAttr("USUARIO_ID", 1L).sessionAttr("HOGAR_ID", 1L))
      .andExpect(status().isOk())
      .andExpect(view().name("propuestas"))
      .andExpect(model().attributeExists("nombreUsuario"))
      .andExpect(model().attributeExists("listaPropuestas"));
  }

  @Test
  public void queGuardePropuestaExitosamenteYRedirija() throws Exception {
    doNothing().when(servicioVotacionMock).guardarPropuesta(any(Propuesta.class));

    mockMvc
      .perform(
        post("/propuestas/guardar")
          .sessionAttr("USUARIO_ID", 1L)
          .sessionAttr("HOGAR_ID", 1L)
          .param("titulo", "Nueva Propuesta")
          .param("descripcion", "Descripcion de prueba")
          .param("porcentajeQuorum", "50.0")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/propuestas"));

    verify(servicioVotacionMock, times(1)).guardarPropuesta(any(Propuesta.class));
  }

  @Test
  public void queManejeExcepcionPropuestaNoExisteAlGuardar() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Admin");

    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioVotacionMock.obtenerTodasLasPropuestas()).thenReturn(Collections.emptyList());

    doThrow(new PropuestaNoExiste("La propuesta no existe"))
      .when(servicioVotacionMock)
      .guardarPropuesta(any(Propuesta.class));

    mockMvc
      .perform(
        post("/propuestas/guardar")
          .sessionAttr("USUARIO_ID", 1L)
          .sessionAttr("HOGAR_ID", 1L)
          .param("titulo", "Invalida")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("propuestas"))
      .andExpect(model().attributeExists("mensajeError"));
  }

  @Test
  public void quePermitaVotarExitosamente() throws Exception {
    doNothing().when(servicioVotacionMock).votar(1L, 10L, true, "Excelente");

    mockMvc
      .perform(
        post("/propuestas/10/votar")
          .sessionAttr("USUARIO_ID", 1L)
          .sessionAttr("HOGAR_ID", 1L)
          .param("esAfirmativo", "true")
          .param("comentario", "Excelente")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/propuestas"));

    verify(servicioVotacionMock).votar(1L, 10L, true, "Excelente");
  }

  @Test
  public void queRedirijaAlLoginSiIntentaVotarSinSesion() throws Exception {
    mockMvc
      .perform(post("/propuestas/10/votar").param("esAfirmativo", "true"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/login"));
  }

  @Test
  public void queManejeExcepcionUsuarioYaVotoAlIntentarVotar() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Pedro");

    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioVotacionMock.obtenerTodasLasPropuestas()).thenReturn(Collections.emptyList());

    doThrow(new UsuarioYaVoto("El usuario ya ha votado"))
      .when(servicioVotacionMock)
      .votar(eq(1L), eq(10L), eq(true), any());

    mockMvc
      .perform(
        post("/propuestas/10/votar")
          .sessionAttr("USUARIO_ID", 1L)
          .sessionAttr("HOGAR_ID", 1L)
          .param("esAfirmativo", "true")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("propuestas"))
      .andExpect(model().attribute("mensajeError", "El usuario ya ha votado"));
  }
}
