package com.tallerwebi.presentacion;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tallerwebi.dominio.ItemCompra;
import com.tallerwebi.dominio.ServicioListaCompras;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.ItemCompraInvalidoException;
import com.tallerwebi.dominio.excepcion.ItemCompraNoExiste;
import com.tallerwebi.dominio.excepcion.SinItemsMarcadosException;
import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

public class ControladorListaComprasTest {

  private MockMvc mockMvc;
  private ServicioListaCompras servicioListaComprasMock;
  private ServicioUsuario servicioUsuarioMock;
  private HttpSession sessionMock;
  private ControladorListaCompras controladorListaCompras;

  @BeforeEach
  public void setUp() {
    servicioListaComprasMock = mock(ServicioListaCompras.class);
    servicioUsuarioMock = mock(ServicioUsuario.class);
    sessionMock = mock(HttpSession.class);

    controladorListaCompras =
      new ControladorListaCompras(servicioListaComprasMock, servicioUsuarioMock, sessionMock);

    // Configuramos el ViewResolver para evitar el bucle de vista circular en Spring MVC
    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setPrefix("/WEB-INF/views/");
    viewResolver.setSuffix(".jsp");

    mockMvc =
      MockMvcBuilders
        .standaloneSetup(controladorListaCompras)
        .setViewResolvers(viewResolver)
        .build();
  }

  @Test
  public void queRedirijaAlLoginSiNoHayUsuarioLogueadoEnIrAListaDeCompras() throws Exception {
    mockMvc
      .perform(get("/lista-compras"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/login"));
  }

  @Test
  public void queMuestreLaVistaDeListaDeComprasSiElUsuarioEstaLogueado() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("María");

    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioListaComprasMock.obtenerItemsActivos()).thenReturn(Collections.emptyList());

    mockMvc
      .perform(get("/lista-compras").sessionAttr("USUARIO_ID", 1L))
      .andExpect(status().isOk())
      .andExpect(view().name("lista-compras"))
      .andExpect(model().attributeExists("nombreUsuario"))
      .andExpect(model().attributeExists("items"))
      .andExpect(model().attributeExists("cantidadEnCarrito"));
  }

  @Test
  public void queAgregueItemExitosamenteYRedirija() throws Exception {
    doNothing().when(servicioListaComprasMock).agregarItem(eq("Manzanas"), eq(1L));

    mockMvc
      .perform(
        post("/lista-compras/agregar").sessionAttr("USUARIO_ID", 1L).param("nombre", "Manzanas")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/lista-compras"));

    verify(servicioListaComprasMock, times(1)).agregarItem("Manzanas", 1L);
  }

  @Test
  public void queManejeExcepcionItemCompraInvalidoAlAgregar() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Carlos");

    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioListaComprasMock.obtenerItemsActivos()).thenReturn(Collections.emptyList());

    doThrow(new ItemCompraInvalidoException("Item inválido"))
      .when(servicioListaComprasMock)
      .agregarItem(eq(""), eq(1L));

    mockMvc
      .perform(post("/lista-compras/agregar").sessionAttr("USUARIO_ID", 1L).param("nombre", ""))
      .andExpect(status().isOk())
      .andExpect(view().name("lista-compras"))
      .andExpect(model().attribute("mensajeError", "Item inválido"));
  }

  @Test
  public void queAlternarItemFuncioneCorrectamente() throws Exception {
    doNothing().when(servicioListaComprasMock).alternarItem(5L);

    mockMvc
      .perform(post("/lista-compras/5/alternar").sessionAttr("USUARIO_ID", 1L))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/lista-compras"));

    verify(servicioListaComprasMock, times(1)).alternarItem(5L);
  }

  @Test
  public void queEliminarItemFuncioneCorrectamente() throws Exception {
    doNothing().when(servicioListaComprasMock).eliminarItem(3L);

    mockMvc
      .perform(post("/lista-compras/3/eliminar").sessionAttr("USUARIO_ID", 1L))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/lista-compras"));

    verify(servicioListaComprasMock, times(1)).eliminarItem(3L);
  }

  @Test
  public void queConviertaEnGastoExitosamenteYRedirijaConDescripcionCodificada() throws Exception {
    when(servicioListaComprasMock.convertirEnGasto()).thenReturn("Supermercado mes");

    mockMvc
      .perform(post("/lista-compras/convertir-en-gasto").sessionAttr("USUARIO_ID", 1L))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/gastos?descripcion=Supermercado+mes"));
  }

  @Test
  public void queManejeExcepcionSinItemsMarcadosAlConvertirEnGasto() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Ana");

    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioListaComprasMock.obtenerItemsActivos()).thenReturn(Collections.emptyList());

    doThrow(new SinItemsMarcadosException("No hay items marcados"))
      .when(servicioListaComprasMock)
      .convertirEnGasto();

    mockMvc
      .perform(post("/lista-compras/convertir-en-gasto").sessionAttr("USUARIO_ID", 1L))
      .andExpect(status().isOk())
      .andExpect(view().name("lista-compras"))
      .andExpect(model().attribute("mensajeError", "No hay items marcados"));
  }
}
