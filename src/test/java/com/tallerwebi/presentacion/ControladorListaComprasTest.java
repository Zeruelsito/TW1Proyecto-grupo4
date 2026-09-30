package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;
 
import com.tallerwebi.dominio.excepcion.ItemCompraInvalidoException;
import com.tallerwebi.dominio.excepcion.SinItemsMarcadosException;
import com.tallerwebi.dominio.service.ServicioListaCompras;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
 
public class ControladorListaComprasTest {
 
  private ControladorListaCompras controladorListaCompras;
  private ServicioListaCompras servicioListaComprasMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;
 
  @BeforeEach
  public void init() {
    servicioListaComprasMock = mock(ServicioListaCompras.class);
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    when(requestMock.getSession()).thenReturn(sessionMock);
    when(servicioListaComprasMock.obtenerItemsActivos()).thenReturn(List.of());
    controladorListaCompras = new ControladorListaCompras(servicioListaComprasMock);
  }
 
  @Test
  public void irAListaDeComprasSinSesionDeberiaRedirigirALogin() {
    // ejecucion
    ModelAndView modelAndView = controladorListaCompras.irAListaDeCompras(requestMock);
 
    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
  }
 
  @Test
  public void irAListaDeComprasConSesionDeberiaMostrarLaLista() {
    // preparacion
    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
 
    // ejecucion
    ModelAndView modelAndView = controladorListaCompras.irAListaDeCompras(requestMock);
 
    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("lista-compras"));
  }
 
  @Test
  public void agregarItemValidoDeberiaVolverALaLista() {
    // preparacion
    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
 
    // ejecucion
    ModelAndView modelAndView = controladorListaCompras.agregarItem("Leche", requestMock);
 
    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/lista-compras"));
    verify(servicioListaComprasMock, times(1)).agregarItem("Leche", 1L);
  }
 
  @Test
  public void agregarItemInvalidoDeberiaMostrarError() {
    // preparacion
    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
    doThrow(new ItemCompraInvalidoException("El nombre del ítem no puede estar vacío."))
      .when(servicioListaComprasMock)
      .agregarItem("", 1L);
 
    // ejecucion
    ModelAndView modelAndView = controladorListaCompras.agregarItem("", requestMock);
 
    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("lista-compras"));
    assertThat(
      modelAndView.getModel().get("mensajeError").toString(),
      equalToIgnoringCase("El nombre del ítem no puede estar vacío.")
    );
  }
 
  @Test
  public void convertirEnGastoDeberiaRedirigirAGastosConLaDescripcion() {
    // preparacion
    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
    when(servicioListaComprasMock.convertirEnGasto()).thenReturn("Compras: Leche");
 
    // ejecucion
    ModelAndView modelAndView = controladorListaCompras.convertirEnGasto(requestMock);
 
    // validacion
    assertThat(
      modelAndView.getViewName(),
      equalToIgnoringCase("redirect:/gastos?descripcion=Compras%3A+Leche")
    );
  }
 
  @Test
  public void convertirEnGastoSinItemsDeberiaMostrarError() {
    // preparacion
    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);
    when(servicioListaComprasMock.convertirEnGasto())
      .thenThrow(new SinItemsMarcadosException("Marcá al menos un ítem como comprado."));
 
    // ejecucion
    ModelAndView modelAndView = controladorListaCompras.convertirEnGasto(requestMock);
 
    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("lista-compras"));
    assertThat(
      modelAndView.getModel().get("mensajeError").toString(),
      equalToIgnoringCase("Marcá al menos un ítem como comprado.")
    );
  }
}