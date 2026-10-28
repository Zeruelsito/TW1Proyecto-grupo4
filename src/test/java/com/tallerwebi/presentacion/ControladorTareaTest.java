package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.ServicioCreditoTarea;
import com.tallerwebi.dominio.ServicioTareaRepetitiva;
import com.tallerwebi.dominio.Tarea;
import com.tallerwebi.dominio.excepcion.TareaNoEncontradaException;
import com.tallerwebi.dominio.excepcion.TareaYaCompletadaException;
import com.tallerwebi.dominio.excepcion.ValorCreditoInvalidoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

public class ControladorTareaTest {

  private ControladorTarea controladorTarea;
  private ServicioCreditoTarea servicioCreditoTareaMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;
  private ServicioTareaRepetitiva servicioTareaRepetitivaMock;

  @BeforeEach
  public void init() {
    servicioCreditoTareaMock = mock(ServicioCreditoTarea.class);
    servicioTareaRepetitivaMock = mock(ServicioTareaRepetitiva.class);
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);

    when(requestMock.getSession()).thenReturn(sessionMock);
    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);

    controladorTarea = new ControladorTarea(servicioCreditoTareaMock, servicioTareaRepetitivaMock);
  }

  @Test
  public void irATareasDebeDevolverLaVistaTareasYElModeloConCreditoAcumulado() {
    // preparación[cite: 12]
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(1L)).thenReturn(1500.0);
    when(servicioCreditoTareaMock.obtenerTodasLasTareas()).thenReturn(new ArrayList<>());

    // ejecución[cite: 12]
    ModelAndView modelAndView = controladorTarea.irATareas(requestMock);

    // verificación[cite: 12]
    assertThat(modelAndView.getViewName(), equalTo("tareas"));
    ModelMap modelMap = modelAndView.getModelMap();
    assertThat(modelMap.get("creditoAcumulado"), equalTo(1500.0));
    assertThat(modelMap.get("tareas"), notNullValue());
    assertThat(modelMap.get("nuevaTarea"), notNullValue());
  }

  @Test
  public void crearTareaExitosaDebeRedirigirATareas() {
    // preparación - Usamos el nuevo constructor sin ID y lo seteamos aparte[cite: 9, 12]
    Tarea tarea = new Tarea("Limpiar la cocina", 2.0, 1000.0, null);
    tarea.setId(1L);
    doNothing().when(servicioCreditoTareaMock).registrarTarea(any(Tarea.class));

    // ejecución[cite: 12]
    ModelAndView modelAndView = controladorTarea.crearTarea(tarea, requestMock);

    // verificación[cite: 12]
    assertThat(modelAndView.getViewName(), equalTo("redirect:/tareas"));
    verify(servicioCreditoTareaMock, times(1)).registrarTarea(tarea);
  }

  @Test
  public void crearTareaConCreditoInvalidoDebeMostrarMensajeDeError() {
    // preparación - Usamos el nuevo constructor sin ID[cite: 9, 12]
    Tarea tareaInvalida = new Tarea("Pintar reja", -1.0, 1000.0, null);
    tareaInvalida.setId(1L);
    doThrow(new ValorCreditoInvalidoException())
      .when(servicioCreditoTareaMock)
      .registrarTarea(any(Tarea.class));
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(1L)).thenReturn(0.0);

    // ejecución[cite: 12]
    ModelAndView modelAndView = controladorTarea.crearTarea(tareaInvalida, requestMock);

    // verificación[cite: 12]
    assertThat(modelAndView.getViewName(), equalTo("tareas"));
    assertThat(
      modelAndView.getModelMap().get("error"),
      equalTo("El valor del crédito debe ser mayor a $0.")
    );
  }

  @Test
  public void completarTareaExitosaDebeRedirigirATareas() {
    // preparación[cite: 12]
    Long tareaId = 5L;
    Long usuarioId = 1L;
    doNothing().when(servicioCreditoTareaMock).completarTareaYGenerarCredito(tareaId, usuarioId);

    // ejecución[cite: 12]
    ModelAndView modelAndView = controladorTarea.completarTarea(tareaId, requestMock);

    // verificación[cite: 12]
    assertThat(modelAndView.getViewName(), equalTo("redirect:/tareas"));
    verify(servicioCreditoTareaMock, times(1))
      .completarTareaYGenerarCredito(eq(tareaId), eq(usuarioId));
  }

  @Test
  public void completarTareaYaCompletadaDebePermanecerEnVistaYMostrarError() {
    // preparación[cite: 12]
    Long tareaId = 5L;
    Long usuarioId = 1L;
    doThrow(new TareaYaCompletadaException())
      .when(servicioCreditoTareaMock)
      .completarTareaYGenerarCredito(tareaId, usuarioId);

    // ejecución[cite: 12]
    ModelAndView modelAndView = controladorTarea.completarTarea(tareaId, requestMock);

    // verificación[cite: 12]
    assertThat(modelAndView.getViewName(), equalTo("tareas"));
    assertThat(
      modelAndView.getModelMap().get("error"),
      equalTo("Esta tarea ya fue completada previamente.")
    );
  }

  @Test
  public void crearTareaDesdeCatalogoDebeRegistrarConLaPlantillaYRedirigir() {
    when(requestMock.getParameter("tareaRepetitivaId")).thenReturn("7");
    Tarea tarea = new Tarea();
    tarea.setHorasDedicadas(2.0);

    ModelAndView modelAndView = controladorTarea.crearTarea(tarea, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/tareas"));
    verify(servicioCreditoTareaMock).registrarTareaDesdeCatalogo(7L, 2.0, 1L);
  }

  @Test
  public void crearTareaDesdeCatalogoInexistenteDebeMostrarMensajeDeError() {
    when(requestMock.getParameter("tareaRepetitivaId")).thenReturn("99");
    when(sessionMock.getAttribute("HOGAR_ID")).thenReturn(91L);
    Tarea tarea = new Tarea();
    tarea.setHorasDedicadas(1.0);

    doThrow(new TareaNoEncontradaException())
      .when(servicioCreditoTareaMock)
      .registrarTareaDesdeCatalogo(99L, 1.0, 91L);

    ModelAndView modelAndView = controladorTarea.crearTarea(tarea, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("tareas"));
    assertThat(
      modelAndView.getModelMap().get("error"),
      equalTo("La tarea seleccionada no existe en tu hogar.")
    );
  }
}
