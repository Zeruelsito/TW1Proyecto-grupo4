package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.ServicioCalculosGasto;
import com.tallerwebi.dominio.ServicioCreditoTarea;
import com.tallerwebi.dominio.ServicioHogar;
import com.tallerwebi.dominio.ServicioLiquidacion;
import com.tallerwebi.dominio.ServicioRegistroGasto;
import com.tallerwebi.dominio.ServicioTareaRepetitiva;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Tarea;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.TareaNoEncontradaException;
import com.tallerwebi.dominio.excepcion.TareaYaCompletadaException;
import com.tallerwebi.dominio.excepcion.ValorCreditoInvalidoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

public class ControladorTareaTest {

  private MockMvc mockMvc;
  private ServicioCreditoTarea servicioCreditoTareaMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;
  private ServicioTareaRepetitiva servicioTareaRepetitivaMock;
  private ServicioUsuario servicioUsuarioMock;
  private ServicioHogar servicioHogarMock;
  private ServicioLiquidacion servicioLiquidacionMock;
  private ServicioRegistroGasto servicioRegistroGastoMock;
  private ServicioCalculosGasto servicioCalculosGastoMock;
  private ControladorTarea controladorTarea;

  @BeforeEach
  public void setUp() {
    servicioCreditoTareaMock = mock(ServicioCreditoTarea.class);
    servicioTareaRepetitivaMock = mock(ServicioTareaRepetitiva.class);
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    servicioUsuarioMock = mock(ServicioUsuario.class);
    servicioHogarMock = mock(ServicioHogar.class);
    servicioLiquidacionMock = mock(ServicioLiquidacion.class);
    servicioRegistroGastoMock = mock(ServicioRegistroGasto.class);
    servicioCalculosGastoMock = mock(ServicioCalculosGasto.class);

    controladorTarea =
      new ControladorTarea(
        servicioCreditoTareaMock,
        servicioUsuarioMock,
        servicioTareaRepetitivaMock,
        servicioHogarMock,
        servicioLiquidacionMock,
        servicioRegistroGastoMock,
        servicioCalculosGastoMock
      );

    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setPrefix("/WEB-INF/views/");
    viewResolver.setSuffix(".jsp");

    mockMvc =
      MockMvcBuilders.standaloneSetup(controladorTarea).setViewResolvers(viewResolver).build();
  }

  @Test
  public void queMuestreLaVistaDeTareasConDatosDelDashboard() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Carlos");

    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(1L)).thenReturn(500.0);
    when(servicioCreditoTareaMock.obtenerTodasLasTareas()).thenReturn(Collections.emptyList());

    mockMvc
      .perform(get("/tareas").sessionAttr("USUARIO_ID", 1L).sessionAttr("HOGAR_ID", 1L))
      .andExpect(status().isOk())
      .andExpect(view().name("tareas"))
      .andExpect(model().attributeExists("nombreUsuario"))
      .andExpect(model().attributeExists("tareas"))
      .andExpect(model().attributeExists("creditoAcumulado"))
      .andExpect(model().attributeExists("precioHoraHogar"))
      .andExpect(model().attributeExists("nuevaTarea"));
  }

  @Test
  public void queRedirijaSiNoHayHogarIdEnSesionAlIrATareas() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Carlos");
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);

    mockMvc
      .perform(get("/tareas").sessionAttr("USUARIO_ID", 1L))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/tareas"))
      .andExpect(flash().attributeExists("alertaHogar"));
  }

  @Test
  public void queCreeTareaExitosamenteYRedirija() throws Exception {
    doNothing().when(servicioCreditoTareaMock).registrarTarea(any(Tarea.class));

    mockMvc
      .perform(
        post("/tareas/crear")
          .sessionAttr("USUARIO_ID", 1L)
          .sessionAttr("HOGAR_ID", 1L)
          .param("titulo", "Limpiar cocina")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/tareas"));

    verify(servicioCreditoTareaMock, times(1)).registrarTarea(any(Tarea.class));
  }

  @Test
  public void queManejeExcepcionValorCreditoInvalidoAlCrearTarea() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Carlos");
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(1L)).thenReturn(0.0);
    when(servicioCreditoTareaMock.obtenerTodasLasTareas()).thenReturn(Collections.emptyList());

    doThrow(new ValorCreditoInvalidoException())
      .when(servicioCreditoTareaMock)
      .registrarTarea(any(Tarea.class));

    mockMvc
      .perform(
        post("/tareas/crear")
          .sessionAttr("USUARIO_ID", 1L)
          .sessionAttr("HOGAR_ID", 1L)
          .param("titulo", "")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("tareas"))
      .andExpect(model().attribute("error", "El título de la tarea no puede estar vacío."));
  }

  @Test
  public void queCompletarTareaExitosamenteRedirija() throws Exception {
    Hogar hogar = new Hogar("Hogar 1");
    hogar.setPrecioHoraTarea(1000.0);
    when(servicioHogarMock.buscarPorId(1L)).thenReturn(hogar);

    doNothing()
      .when(servicioCreditoTareaMock)
      .completarTareaYGenerarCredito(eq(10L), eq(1L), eq(2.0), anyDouble());

    mockMvc
      .perform(
        post("/tareas/completar")
          .sessionAttr("USUARIO_ID", 1L)
          .sessionAttr("HOGAR_ID", 1L)
          .param("tareaId", "10")
          .param("horasDedicadas", "2.0")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/tareas"));

    verify(servicioCreditoTareaMock, times(1))
      .completarTareaYGenerarCredito(eq(10L), eq(1L), eq(2.0), anyDouble());
  }

  @Test
  public void queManejeErrorSiIdDeTareaEsNullAlCompletar() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Carlos");

    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(1L)).thenReturn(100.0);
    when(servicioCreditoTareaMock.obtenerTodasLasTareas()).thenReturn(Collections.emptyList());

    mockMvc
      .perform(
        post("/tareas/completar")
          .sessionAttr("USUARIO_ID", 1L)
          .sessionAttr("HOGAR_ID", 1L)
          .param("horasDedicadas", "2.0")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("tareas"))
      .andExpect(model().attribute("error", "Identificador de tarea inválido."));

    verify(servicioCreditoTareaMock, never())
      .completarTareaYGenerarCredito(anyLong(), anyLong(), anyDouble(), anyDouble());
  }

  @Test
  public void queManejeExcepcionTareaNoEncontradaAlCompletar() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Carlos");

    Hogar hogar = new Hogar("Hogar 1");
    when(servicioHogarMock.buscarPorId(1L)).thenReturn(hogar);
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(1L)).thenReturn(100.0);
    when(servicioCreditoTareaMock.obtenerTodasLasTareas()).thenReturn(Collections.emptyList());

    doThrow(new TareaNoEncontradaException())
      .when(servicioCreditoTareaMock)
      .completarTareaYGenerarCredito(eq(99L), eq(1L), eq(2.0), anyDouble());

    mockMvc
      .perform(
        post("/tareas/completar")
          .sessionAttr("USUARIO_ID", 1L)
          .sessionAttr("HOGAR_ID", 1L)
          .param("tareaId", "99")
          .param("horasDedicadas", "2.0")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("tareas"))
      .andExpect(model().attribute("error", "La tarea seleccionada no existe."));
  }

  @Test
  public void queManejeExcepcionTareaYaCompletadaAlCompletar() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Carlos");

    Hogar hogar = new Hogar("Hogar 1");
    when(servicioHogarMock.buscarPorId(1L)).thenReturn(hogar);
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(1L)).thenReturn(200.0);
    when(servicioCreditoTareaMock.obtenerTodasLasTareas()).thenReturn(Collections.emptyList());

    doThrow(new TareaYaCompletadaException())
      .when(servicioCreditoTareaMock)
      .completarTareaYGenerarCredito(eq(5L), eq(1L), eq(2.0), anyDouble());

    mockMvc
      .perform(
        post("/tareas/completar")
          .sessionAttr("USUARIO_ID", 1L)
          .sessionAttr("HOGAR_ID", 1L)
          .param("tareaId", "5")
          .param("horasDedicadas", "2.0")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("tareas"))
      .andExpect(model().attribute("error", "Esta tarea ya fue completada previamente."));
  }

  @Test
  public void queManejeExcepcionValorCreditoInvalidoAlCompletar() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Carlos");

    Hogar hogar = new Hogar("Hogar 1");
    when(servicioHogarMock.buscarPorId(1L)).thenReturn(hogar);
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(1L)).thenReturn(200.0);
    when(servicioCreditoTareaMock.obtenerTodasLasTareas()).thenReturn(Collections.emptyList());

    doThrow(new ValorCreditoInvalidoException())
      .when(servicioCreditoTareaMock)
      .completarTareaYGenerarCredito(eq(5L), eq(1L), eq(0.0), anyDouble());

    mockMvc
      .perform(
        post("/tareas/completar")
          .sessionAttr("USUARIO_ID", 1L)
          .sessionAttr("HOGAR_ID", 1L)
          .param("tareaId", "5")
          .param("horasDedicadas", "0.0")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("tareas"))
      .andExpect(
        model().attribute("error", "La cantidad de horas ingresadas debe ser mayor a cero.")
      );
  }

  @Test
  public void queLiquideCreditosParcialesExitosamente() throws Exception {
    Hogar hogar = new Hogar("Hogar 1");
    Usuario usuario = new Usuario();
    usuario.setId(1L);

    when(servicioHogarMock.buscarPorId(1L)).thenReturn(hogar);
    when(servicioRegistroGastoMock.obtenerTodosLosGastos(1L)).thenReturn(Collections.emptyList());
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioLiquidacionMock.obtenerDeudaUsuario(usuario, hogar, Collections.emptyList()))
      .thenReturn(5000.0);
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(1L)).thenReturn(10000.0);

    mockMvc
      .perform(
        post("/tareas/liquidar-creditos").sessionAttr("USUARIO_ID", 1L).sessionAttr("HOGAR_ID", 1L)
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/tareas"));

    verify(servicioCreditoTareaMock, times(1)).liquidarCreditosParciales(eq(1L), eq(5000.0));
  }

  @Test
  public void crearTareaDesdeCatalogoDebeRegistrarConLaPlantillaYRedirigir() {
    when(requestMock.getSession()).thenReturn(sessionMock);
    when(sessionMock.getAttribute("HOGAR_ID")).thenReturn(1L);
    when(requestMock.getParameter("tareaRepetitivaId")).thenReturn("7");

    Tarea tarea = new Tarea();

    ModelAndView modelAndView = controladorTarea.crearTarea(tarea, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/tareas"));
    verify(servicioCreditoTareaMock).registrarTareaDesdeCatalogo(7L, null, 1L);
  }

  @Test
  public void crearTareaDesdeCatalogoInexistenteDebeMostrarMensajeDeError() {
    when(requestMock.getSession()).thenReturn(sessionMock);
    when(sessionMock.getAttribute("HOGAR_ID")).thenReturn(91L);
    when(requestMock.getParameter("tareaRepetitivaId")).thenReturn("99");

    Tarea tarea = new Tarea();

    doThrow(new TareaNoEncontradaException())
      .when(servicioCreditoTareaMock)
      .registrarTareaDesdeCatalogo(99L, null, 91L);

    ModelAndView modelAndView = controladorTarea.crearTarea(tarea, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("tareas"));
    assertThat(
      modelAndView.getModelMap().get("error"),
      equalTo("La tarea seleccionada no existe en tu hogar.")
    );
  }
}
