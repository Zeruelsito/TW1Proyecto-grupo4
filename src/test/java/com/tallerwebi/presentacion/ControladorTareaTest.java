package com.tallerwebi.presentacion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tallerwebi.dominio.ServicioCreditoTarea;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Tarea;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.TareaNoEncontradaException;
import com.tallerwebi.dominio.excepcion.TareaYaCompletadaException;
import com.tallerwebi.dominio.excepcion.ValorCreditoInvalidoException;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

public class ControladorTareaTest {

  private MockMvc mockMvc;
  private ServicioCreditoTarea servicioCreditoTareaMock;
  private ServicioUsuario servicioUsuarioMock;
  private ControladorTarea controladorTarea;

  @BeforeEach
  public void setUp() {
    servicioCreditoTareaMock = mock(ServicioCreditoTarea.class);
    servicioUsuarioMock = mock(ServicioUsuario.class);

    controladorTarea = new ControladorTarea(servicioCreditoTareaMock, servicioUsuarioMock);

    // Configuramos el ViewResolver para evitar el bucle de vista circular en Spring MVC
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
      .perform(get("/tareas").sessionAttr("USUARIO_ID", 1L))
      .andExpect(status().isOk())
      .andExpect(view().name("tareas"))
      .andExpect(model().attributeExists("nombreUsuario"))
      .andExpect(model().attributeExists("tareas"))
      .andExpect(model().attributeExists("creditoAcumulado"))
      .andExpect(model().attributeExists("precioHoraHogar"))
      .andExpect(model().attributeExists("nuevaTarea"));
  }

  @Test
  public void queCreeTareaExitosamenteYRedirija() throws Exception {
    doNothing().when(servicioCreditoTareaMock).registrarTarea(any(Tarea.class));

    mockMvc
      .perform(
        post("/tareas/crear").sessionAttr("USUARIO_ID", 1L).param("nombre", "Limpiar cocina")
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
        post("/tareas/crear").sessionAttr("USUARIO_ID", 1L).param("nombre", "Tarea inválida")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("tareas"))
      .andExpect(model().attributeExists("error"))
      .andExpect(model().attribute("error", "El valor del crédito debe ser mayor a $0."));
  }

  @Test
  public void queCompletarTareaExitosamenteRedirija() throws Exception {
    doNothing().when(servicioCreditoTareaMock).completarTareaYGenerarCredito(10L, 1L);

    mockMvc
      .perform(post("/tareas/completar").sessionAttr("USUARIO_ID", 1L).param("tareaId", "10"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/tareas"));

    verify(servicioCreditoTareaMock, times(1)).completarTareaYGenerarCredito(10L, 1L);
  }

  @Test
  public void queManejeErrorSiIdDeTareaEsNullAlCompletar() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Carlos");

    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(1L)).thenReturn(100.0);
    when(servicioCreditoTareaMock.obtenerTodasLasTareas()).thenReturn(Collections.emptyList());

    mockMvc
      .perform(post("/tareas/completar").sessionAttr("USUARIO_ID", 1L)) // Sin param tareaId
      .andExpect(status().isOk())
      .andExpect(view().name("tareas"))
      .andExpect(model().attribute("error", "Identificador de tarea inválido."));

    verify(servicioCreditoTareaMock, never()).completarTareaYGenerarCredito(anyLong(), anyLong());
  }

  @Test
  public void queManejeExcepcionTareaNoEncontradaAlCompletar() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Carlos");

    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(1L)).thenReturn(100.0);
    when(servicioCreditoTareaMock.obtenerTodasLasTareas()).thenReturn(Collections.emptyList());

    doThrow(new TareaNoEncontradaException())
      .when(servicioCreditoTareaMock)
      .completarTareaYGenerarCredito(99L, 1L);

    mockMvc
      .perform(post("/tareas/completar").sessionAttr("USUARIO_ID", 1L).param("tareaId", "99"))
      .andExpect(status().isOk())
      .andExpect(view().name("tareas"))
      .andExpect(model().attribute("error", "La tarea seleccionada no existe."));
  }

  @Test
  public void queManejeExcepcionTareaYaCompletadaAlCompletar() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setNombre("Carlos");

    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(1L)).thenReturn(200.0);
    when(servicioCreditoTareaMock.obtenerTodasLasTareas()).thenReturn(Collections.emptyList());

    doThrow(new TareaYaCompletadaException())
      .when(servicioCreditoTareaMock)
      .completarTareaYGenerarCredito(5L, 1L);

    mockMvc
      .perform(post("/tareas/completar").sessionAttr("USUARIO_ID", 1L).param("tareaId", "5"))
      .andExpect(status().isOk())
      .andExpect(view().name("tareas"))
      .andExpect(model().attribute("error", "Esta tarea ya fue completada previamente."));
  }
}
