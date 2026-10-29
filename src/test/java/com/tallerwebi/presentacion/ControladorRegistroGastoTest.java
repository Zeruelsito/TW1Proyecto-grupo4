package com.tallerwebi.presentacion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tallerwebi.dominio.Gasto;
import com.tallerwebi.dominio.ServicioRegistroGasto;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.DescripcionGastoVaciaException;
import com.tallerwebi.dominio.excepcion.MontoGastoInvalidoException;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

public class ControladorRegistroGastoTest {

  private MockMvc mockMvc;
  private ServicioRegistroGasto servicioRegistroGastoMock;
  private ServicioUsuario servicioUsuarioMock;
  private HttpSession sessionMock;
  private ControladorRegistroGasto controladorRegistroGasto;

  @BeforeEach
  public void setUp() {
    servicioRegistroGastoMock = mock(ServicioRegistroGasto.class);
    servicioUsuarioMock = mock(ServicioUsuario.class);
    sessionMock = mock(HttpSession.class);

    controladorRegistroGasto =
      new ControladorRegistroGasto(servicioRegistroGastoMock, servicioUsuarioMock);

    // Configuramos el ViewResolver para evitar el bucle de vista circular en Spring MVC
    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setPrefix("/WEB-INF/views/");
    viewResolver.setSuffix(".jsp");

    mockMvc =
      MockMvcBuilders
        .standaloneSetup(controladorRegistroGasto)
        .setViewResolvers(viewResolver)
        .build();
  }

  @Test
  public void queMuestreLaVistaDeGastosCorrectamente() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Lucía");

    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioRegistroGastoMock.obtenerTodosLosGastos(any()))
      .thenReturn(Collections.emptyList());
    when(servicioRegistroGastoMock.obtenerGastosFijosRecurrentes(any()))
      .thenReturn(Collections.emptyList());
    when(servicioRegistroGastoMock.obtenerIntegrantes(any())).thenReturn(Collections.emptyList());
    when(servicioRegistroGastoMock.obtenerSugerenciaProximoPagador(any())).thenReturn(null);

    mockMvc
      .perform(get("/gastos").sessionAttr("USUARIO_ID", 1L))
      .andExpect(status().isOk())
      .andExpect(view().name("gastos"))
      .andExpect(model().attributeExists("nombreUsuario"))
      .andExpect(model().attributeExists("listaGastos"))
      .andExpect(model().attributeExists("gastosFijosRecurrentes"))
      .andExpect(model().attributeExists("integrantes"))
      .andExpect(model().attributeExists("gasto"));
  }

  @Test
  public void queRegistreGastoExitososamenteYRedirija() throws Exception {
    mockMvc
      .perform(
        post("/gastos/guardar").param("descripcion", "Compra supermercado").param("monto", "1500.0")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/gastos"));

    // Actualizado al nuevo método con participantes
    verify(servicioRegistroGastoMock, times(1))
      .registrarGastoConParticipantes(any(Gasto.class), any(), any());
  }

  @Test
  public void queManejeExcepcionDescripcionGastoVaciaAlGuardar() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Lucía");

    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioRegistroGastoMock.obtenerTodosLosGastos(any()))
      .thenReturn(Collections.emptyList());

    // Actualizado al nuevo método con participantes
    doThrow(new DescripcionGastoVaciaException())
      .when(servicioRegistroGastoMock)
      .registrarGastoConParticipantes(any(Gasto.class), any(), any());

    mockMvc
      .perform(post("/gastos/guardar").param("descripcion", "").param("monto", "500.0"))
      .andExpect(status().isOk())
      .andExpect(view().name("gastos"))
      .andExpect(
        model().attribute("mensajeError", "La descripción del gasto no puede estar vacía.")
      );
  }

  @Test
  public void queManejeExcepcionMontoGastoInvalidoAlGuardar() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Lucía");

    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioRegistroGastoMock.obtenerTodosLosGastos(any()))
      .thenReturn(Collections.emptyList());

    // Actualizado al nuevo método con participantes
    doThrow(new MontoGastoInvalidoException())
      .when(servicioRegistroGastoMock)
      .registrarGastoConParticipantes(any(Gasto.class), any(), any());

    mockMvc
      .perform(post("/gastos/guardar").param("descripcion", "Luz").param("monto", "-100.0"))
      .andExpect(status().isOk())
      .andExpect(view().name("gastos"))
      .andExpect(model().attribute("mensajeError", "El monto del gasto debe ser mayor a cero."));
  }

  @Test
  public void quePagueGastoExitososamenteYRedirija() throws Exception {
    doNothing().when(servicioRegistroGastoMock).marcarComoPagado(10L, 2L);

    mockMvc
      .perform(post("/gastos/pagar").param("gastoId", "10").param("pagadorId", "2"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/gastos"));

    verify(servicioRegistroGastoMock, times(1)).marcarComoPagado(10L, 2L);
  }

  @Test
  public void queElimineGastoExitososamenteYRedirija() throws Exception {
    doNothing().when(servicioRegistroGastoMock).eliminarGasto(5L);

    mockMvc
      .perform(post("/gastos/eliminar").param("id", "5"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/gastos"));

    verify(servicioRegistroGastoMock, times(1)).eliminarGasto(5L);
  }

  @Test
  public void queMuestreVencimientosConFiltroTodosPorDefecto() throws Exception {
    when(servicioRegistroGastoMock.obtenerVencimientosPendientes(any()))
      .thenReturn(Collections.emptyList());

    mockMvc
      .perform(get("/vencimientos"))
      .andExpect(status().isOk())
      .andExpect(view().name("vencimientos"))
      .andExpect(model().attribute("filtro", "todos"))
      .andExpect(model().attributeExists("vencimientos"));
  }

  @Test
  public void queMuestreVencimientosConFiltroProximos() throws Exception {
    when(servicioRegistroGastoMock.obtenerVencimientosProximos(any(), any()))
      .thenReturn(Collections.emptyList());

    mockMvc
      .perform(get("/vencimientos").param("filtro", "proximos"))
      .andExpect(status().isOk())
      .andExpect(view().name("vencimientos"))
      .andExpect(model().attribute("filtro", "proximos"));
  }
}
