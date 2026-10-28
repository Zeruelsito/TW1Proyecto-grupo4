package com.tallerwebi.presentacion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.ServicioHogar;
import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

public class ControladorLoginTest {

  private MockMvc mockMvc;
  private ServicioLogin servicioLoginMock;
  private ServicioHogar servicioHogarMock;
  private ControladorLogin controladorLogin;

  @BeforeEach
  public void setUp() {
    servicioLoginMock = mock(ServicioLogin.class);
    servicioHogarMock = mock(ServicioHogar.class);

    controladorLogin = new ControladorLogin(servicioLoginMock);
    // Inyectamos manualmente el servicio de hogar ya que usa @Autowired por campo
    org.springframework.test.util.ReflectionTestUtils.setField(
      controladorLogin,
      "servicioHogar",
      servicioHogarMock
    );

    // Configuramos el ViewResolver para evitar el bucle de vista circular en Spring MVC
    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setPrefix("/WEB-INF/views/");
    viewResolver.setSuffix(".jsp");

    mockMvc =
      MockMvcBuilders.standaloneSetup(controladorLogin).setViewResolvers(viewResolver).build();
  }

  @Test
  public void queMuestreLaVistaDeLoginAlAccederAURLRaiz() throws Exception {
    mockMvc
      .perform(get("/"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/login"));
  }

  @Test
  public void queMuestreLaVistaDeLoginCorrectamente() throws Exception {
    mockMvc
      .perform(get("/login"))
      .andExpect(status().isOk())
      .andExpect(view().name("login"))
      .andExpect(model().attributeExists("datosLogin"));
  }

  @Test
  public void queValideLoginExitosamenteYRedirijaADashboard() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setId(1L);
    usuario.setEmail("test@test.com");
    usuario.setRol("USER");

    when(servicioLoginMock.consultarUsuario("test@test.com", "1234")).thenReturn(usuario);
    when(servicioHogarMock.obtenerHogares(1L))
      .thenReturn(Collections.singletonList(new Hogar("Hogar 1")));

    mockMvc
      .perform(post("/validar-login").param("email", "test@test.com").param("password", "1234"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/dashboard"));

    verify(servicioLoginMock, times(1)).consultarUsuario("test@test.com", "1234");
  }

  @Test
  public void queMuestreErrorEnLoginSiLasCredencialesSonIncorrectas() throws Exception {
    when(servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(null);

    mockMvc
      .perform(post("/validar-login").param("email", "error@test.com").param("password", "wrong"))
      .andExpect(status().isOk())
      .andExpect(view().name("login"))
      .andExpect(model().attributeExists("error"));
  }

  @Test
  public void queMuestreVistaDeNuevoUsuarioEnGet() throws Exception {
    mockMvc
      .perform(get("/nuevo-usuario"))
      .andExpect(status().isOk())
      .andExpect(view().name("nuevo-usuario"))
      .andExpect(model().attributeExists("usuario"));
  }

  @Test
  public void queRegistreUsuarioExitosamenteYRedirijaALogin() throws Exception {
    doNothing().when(servicioLoginMock).registrar(any(Usuario.class));

    mockMvc
      .perform(post("/registrarme").param("email", "nuevo@test.com").param("password", "1234"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/login"));

    verify(servicioLoginMock, times(1)).registrar(any(Usuario.class));
  }

  @Test
  public void queManejeExcepcionUsuarioExistenteAlRegistrar() throws Exception {
    doThrow(new UsuarioExistente()).when(servicioLoginMock).registrar(any(Usuario.class));

    mockMvc
      .perform(post("/registrarme").param("email", "existente@test.com").param("password", "1234"))
      .andExpect(status().isOk())
      .andExpect(view().name("nuevo-usuario"))
      .andExpect(model().attribute("error", "El usuario ya existe"));
  }
}
