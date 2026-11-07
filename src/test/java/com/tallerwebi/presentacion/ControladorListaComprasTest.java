package com.tallerwebi.presentacion;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tallerwebi.dominio.ServicioHogar;
import com.tallerwebi.dominio.ServicioListaCompras;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.ItemCompraInvalidoException;
import com.tallerwebi.dominio.excepcion.SinItemsMarcadosException;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

public class ControladorListaComprasTest {

  private MockMvc mockMvc;
  private ServicioListaCompras servicioListaComprasMock;
  private ServicioHogar servicioHogarMock;
  private ServicioUsuario servicioUsuarioMock;
  private HttpSession sessionMock;
  private ControladorListaCompras controladorListaCompras;

  @BeforeEach
  public void setUp() {
    servicioListaComprasMock = mock(ServicioListaCompras.class);
    servicioHogarMock = mock(ServicioHogar.class);
    servicioUsuarioMock = mock(ServicioUsuario.class);
    sessionMock = mock(HttpSession.class);

    controladorListaCompras =
      new ControladorListaCompras(servicioListaComprasMock, servicioHogarMock, sessionMock);

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
  public void queRedirijaAlDashboardSiNoHayHogarOUsuarioLogueado() throws Exception {
    mockMvc
      .perform(get("/lista-compras"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/dashboard"));
  }

  @Test
  public void queMuestreLaVistaDeListaDeComprasSiElUsuarioEstaLogueado() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("María");

    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
    when(sessionMock.getAttribute("HOGAR_ID")).thenReturn(1L);
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioListaComprasMock.obtenerItemsActivos()).thenReturn(Collections.emptyList());

    mockMvc
      .perform(get("/lista-compras").sessionAttr("USUARIO_ID", 1L).sessionAttr("HOGAR_ID", 1L))
      .andExpect(status().isOk())
      .andExpect(view().name("lista-compras"))
      .andExpect(model().attributeExists("items"))
      .andExpect(model().attributeExists("cantidadEnCarrito"));
  }

  @Test
  public void queConviertaEnGastoMasivoExitosamenteYRedirija() throws Exception {
    doNothing()
      .when(servicioListaComprasMock)
      .actualizarDetallesYConvertirEnGasto(anyList(), anyLong(), anyLong());

    mockMvc
      .perform(
        post("/lista-compras/convertir-en-gasto-masivo")
          .sessionAttr("USUARIO_ID", 1L)
          .sessionAttr("HOGAR_ID", 1L)
          .contentType(MediaType.APPLICATION_JSON)
          .content("[{\"id\":1,\"precioUnitario\":100.0,\"cantidad\":1,\"destino\":\"HOGAR\"}]")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/lista-compras"))
      .andExpect(flash().attribute("mensajeExito", "¡Compra convertida en gastos con éxito!"));
  }

  @Test
  public void queManejeExcepcionAlConvertirEnGastoMasivo() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Ana");

    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
    when(sessionMock.getAttribute("HOGAR_ID")).thenReturn(1L);
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioListaComprasMock.obtenerItemsActivos()).thenReturn(Collections.emptyList());

    doThrow(new SinItemsMarcadosException("No hay items marcados"))
      .when(servicioListaComprasMock)
      .actualizarDetallesYConvertirEnGasto(anyList(), anyLong(), anyLong());

    mockMvc
      .perform(
        post("/lista-compras/convertir-en-gasto-masivo")
          .sessionAttr("USUARIO_ID", 1L)
          .sessionAttr("HOGAR_ID", 1L)
          .contentType(MediaType.APPLICATION_JSON)
          .content("[]")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("lista-compras"))
      .andExpect(model().attribute("mensajeError", "No hay items marcados"));
  }

  @Test
  public void queAlternarItemFuncioneCorrectamente() throws Exception {
    doNothing().when(servicioListaComprasMock).alternarItem(5L);

    mockMvc
      .perform(
        post("/lista-compras/5/alternar").sessionAttr("USUARIO_ID", 1L).sessionAttr("HOGAR_ID", 1L)
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/lista-compras"));

    verify(servicioListaComprasMock, times(1)).alternarItem(5L);
  }

  @Test
  public void queEliminarItemFuncioneCorrectamente() throws Exception {
    doNothing().when(servicioListaComprasMock).eliminarItem(3L);

    mockMvc
      .perform(
        post("/lista-compras/3/eliminar").sessionAttr("USUARIO_ID", 1L).sessionAttr("HOGAR_ID", 1L)
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/lista-compras"));

    verify(servicioListaComprasMock, times(1)).eliminarItem(3L);
  }

  @Test
  public void queConviertaEnGastoExitosamenteYRedirijaConExito() throws Exception {
    when(servicioListaComprasMock.convertirEnGasto(anyLong(), anyLong()))
      .thenReturn("Compra convertida en gastos con éxito.");

    mockMvc
      .perform(
        post("/lista-compras/convertir-en-gasto")
          .sessionAttr("USUARIO_ID", 1L)
          .sessionAttr("HOGAR_ID", 1L)
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/lista-compras"))
      .andExpect(flash().attribute("mensajeExito", "¡Compra convertida en gastos con éxito!"));
  }

  @Test
  public void queManejeExcepcionSinItemsMarcadosAlConvertirEnGasto() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Ana");

    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
    when(sessionMock.getAttribute("HOGAR_ID")).thenReturn(1L); // <--- ¡Agregá esta línea acá también!
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioListaComprasMock.obtenerItemsActivos()).thenReturn(Collections.emptyList());

    doThrow(new SinItemsMarcadosException("No hay items marcados"))
      .when(servicioListaComprasMock)
      .convertirEnGasto(anyLong(), anyLong());

    mockMvc
      .perform(
        post("/lista-compras/convertir-en-gasto")
          .sessionAttr("USUARIO_ID", 1L)
          .sessionAttr("HOGAR_ID", 1L)
      )
      .andExpect(status().isOk())
      .andExpect(view().name("lista-compras"))
      .andExpect(model().attribute("mensajeError", "No hay items marcados"));
  }
}
