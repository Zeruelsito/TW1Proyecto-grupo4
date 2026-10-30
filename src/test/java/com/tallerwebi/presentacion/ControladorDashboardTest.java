package com.tallerwebi.presentacion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.RepositorioHogar;
import com.tallerwebi.dominio.ServicioCalculosGasto;
import com.tallerwebi.dominio.ServicioHogar;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

public class ControladorDashboardTest {

  private MockMvc mockMvc;
  private ServicioHogar servicioHogarMock;
  private ServicioUsuario servicioUsuarioMock;
  private ServicioCalculosGasto servicioCalculosGastoMock;
  private RepositorioHogar repositorioHogarMock;
  private ControladorDashboard controladorDashboard;

  @BeforeEach
  public void setUp() {
    servicioHogarMock = mock(ServicioHogar.class);
    servicioUsuarioMock = mock(ServicioUsuario.class);
    servicioCalculosGastoMock = mock(ServicioCalculosGasto.class);
    repositorioHogarMock = mock(RepositorioHogar.class);

    controladorDashboard = new ControladorDashboard();
    controladorDashboard.servicioHogar = servicioHogarMock;
    controladorDashboard.servicioUsuario = servicioUsuarioMock;
    controladorDashboard.servicioCalculosGasto = servicioCalculosGastoMock;
    controladorDashboard.repositorioHogar = repositorioHogarMock;

    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setPrefix("/WEB-INF/views/");
    viewResolver.setSuffix(".jsp");

    mockMvc =
      MockMvcBuilders.standaloneSetup(controladorDashboard).setViewResolvers(viewResolver).build();
  }

  @Test
  public void queMuestreDashboardExitosamenteCuandoElUsuarioTieneHogar() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setId(1L);
    usuario.setNombre("Macarena");
    Hogar hogar = new Hogar("Depto Palermo");
    hogar.setId(1L);

    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioHogarMock.obtenerHogares(1L)).thenReturn(Collections.singletonList(hogar));

    mockMvc
      .perform(get("/dashboard").sessionAttr("USUARIO_ID", 1L).sessionAttr("HOGAR_ID", 1L))
      .andExpect(status().isOk())
      .andExpect(view().name("dashboard"))
      .andExpect(model().attributeExists("nombreUsuario"))
      .andExpect(model().attributeExists("hogaresUsuario"));
  }

  @Test
  public void queCreeHogarExitosamenteYRedirija() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setId(1L);
    usuario.setNombre("Macarena");

    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    doNothing().when(servicioHogarMock).crearHogar(any(Hogar.class));

    mockMvc
      .perform(
        post("/crear-hogar")
          .sessionAttr("USUARIO_ID", 1L)
          .param("nombreHogar", "Nuevo Hogar")
          .param("pin", "pin123")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/dashboard"));

    verify(servicioHogarMock, times(1)).crearHogar(any(Hogar.class));
  }

  @Test
  public void quePrevisualiceHogarExitosamenteConPinValido() throws Exception {
    Hogar hogar = new Hogar("Depto Palermo");
    hogar.setId(1L);
    hogar.setPin("kumo2026");

    when(servicioHogarMock.buscarPorPin("kumo2026")).thenReturn(hogar);
    when(servicioHogarMock.obtenerHogares(1L)).thenReturn(Collections.emptyList());

    mockMvc
      .perform(post("/previsualizar-unirse").sessionAttr("USUARIO_ID", 1L).param("pin", "kumo2026"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/dashboard"))
      .andExpect(flash().attributeExists("hogarPrevisualizado"));
  }

  @Test
  public void queMuestreErrorAlPrevisualizarSiPinNoExiste() throws Exception {
    when(servicioHogarMock.buscarPorPin("inexistente")).thenReturn(null);

    mockMvc
      .perform(
        post("/previsualizar-unirse").sessionAttr("USUARIO_ID", 1L).param("pin", "inexistente")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/dashboard"))
      .andExpect(flash().attributeExists("errorPin"));
  }

  @Test
  public void queMuestreErrorAlPrevisualizarSiYaEsParteDelHogar() throws Exception {
    Hogar hogar = new Hogar("Depto Palermo");
    hogar.setId(1L);
    hogar.setPin("kumo2026");

    when(servicioHogarMock.buscarPorPin("kumo2026")).thenReturn(hogar);
    when(servicioHogarMock.obtenerHogares(1L)).thenReturn(Collections.singletonList(hogar));

    mockMvc
      .perform(post("/previsualizar-unirse").sessionAttr("USUARIO_ID", 1L).param("pin", "kumo2026"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/dashboard"))
      .andExpect(flash().attributeExists("errorPin"));
  }

  @Test
  public void queConfirmeUnirseAHogarExitosamente() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setId(1L);
    usuario.setNombre("Macarena");
    Hogar hogar = new Hogar("Depto Palermo");
    hogar.setId(1L);
    hogar.setPin("kumo2026");

    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioHogarMock.buscarPorId(1L)).thenReturn(hogar);
    when(servicioHogarMock.unirseAHogar(anyString(), anyString(), any(Usuario.class)))
      .thenReturn(true);

    mockMvc
      .perform(post("/confirmar-unirse-hogar").sessionAttr("USUARIO_ID", 1L).param("hogarId", "1"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/dashboard"));

    verify(servicioHogarMock, times(1))
      .unirseAHogar(eq(hogar.getNombreHogar()), eq(hogar.getPin()), eq(usuario));
  }
}
