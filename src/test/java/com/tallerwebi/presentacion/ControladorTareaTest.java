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
import com.tallerwebi.dominio.Tarea;
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

  @BeforeEach
  public void init() {
    servicioCreditoTareaMock = mock(ServicioCreditoTarea.class);
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);

    when(requestMock.getSession()).thenReturn(sessionMock);
    when(sessionMock.getAttribute("USUARIO_ID")).thenReturn(1L);

    controladorTarea = new ControladorTarea(servicioCreditoTareaMock);
  }

  @Test
  public void irATareasDebeDevolverLaVistaTareasYElModeloConCreditoAcumulado() {
    // preparación
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(1L)).thenReturn(1500.0);
    when(servicioCreditoTareaMock.obtenerTodasLasTareas()).thenReturn(new ArrayList<>());

    // ejecución
    ModelAndView modelAndView = controladorTarea.irATareas(requestMock);

    // verificación
    assertThat(modelAndView.getViewName(), equalTo("tareas"));
    ModelMap modelMap = modelAndView.getModelMap();
    assertThat(modelMap.get("creditoAcumulado"), equalTo(1500.0));
    assertThat(modelMap.get("tareas"), notNullValue());
    assertThat(modelMap.get("nuevaTarea"), notNullValue());
  }

  @Test
  public void crearTareaExitosaDebeRedirigirATareas() {
    // preparación
    Tarea tarea = new Tarea(1L, "Limpiar la cocina", 2.0, 1000.0);
    doNothing().when(servicioCreditoTareaMock).registrarTarea(any(Tarea.class));

    // ejecución
    ModelAndView modelAndView = controladorTarea.crearTarea(tarea, requestMock);

    // verificación
    assertThat(modelAndView.getViewName(), equalTo("redirect:/tareas"));
    verify(servicioCreditoTareaMock, times(1)).registrarTarea(tarea);
  }

  @Test
  public void crearTareaConCreditoInvalidoDebeMostrarMensajeDeError() {
    // preparación
    Tarea tareaInvalida = new Tarea(1L, "Pintar reja", -1.0, 1000.0);
    doThrow(new ValorCreditoInvalidoException())
      .when(servicioCreditoTareaMock)
      .registrarTarea(any(Tarea.class));
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(1L)).thenReturn(0.0);

    // ejecución
    ModelAndView modelAndView = controladorTarea.crearTarea(tareaInvalida, requestMock);

    // verificación
    assertThat(modelAndView.getViewName(), equalTo("tareas"));
    assertThat(
      modelAndView.getModelMap().get("error"),
      equalTo("El valor del crédito debe ser mayor a $0.")
    );
  }

  @Test
  public void completarTareaExitosaDebeRedirigirATareas() {
    // preparación
    Long tareaId = 5L;
    Long usuarioId = 1L;
    doNothing().when(servicioCreditoTareaMock).completarTareaYGenerarCredito(tareaId, usuarioId);

    // ejecución
    ModelAndView modelAndView = controladorTarea.completarTarea(tareaId, requestMock);

    // verificación
    assertThat(modelAndView.getViewName(), equalTo("redirect:/tareas"));
    verify(servicioCreditoTareaMock, times(1))
      .completarTareaYGenerarCredito(eq(tareaId), eq(usuarioId));
  }

  @Test
  public void completarTareaYaCompletadaDebePermanecerEnVistaYMostrarError() {
    // preparación
    Long tareaId = 5L;
    Long usuarioId = 1L;
    doThrow(new TareaYaCompletadaException())
      .when(servicioCreditoTareaMock)
      .completarTareaYGenerarCredito(tareaId, usuarioId);

    // ejecución
    ModelAndView modelAndView = controladorTarea.completarTarea(tareaId, requestMock);

    // verificación
    assertThat(modelAndView.getViewName(), equalTo("tareas"));
    assertThat(
      modelAndView.getModelMap().get("error"),
      equalTo("Esta tarea ya fue completada previamente.")
    );
  }
}
