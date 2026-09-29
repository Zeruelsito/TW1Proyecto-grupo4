package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tallerwebi.dominio.excepcion.TareaYaCompletadaException;
import com.tallerwebi.dominio.excepcion.ValorCreditoInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioCreditoTareaTest {

  private ServicioCreditoTarea servicioCreditoTarea;

  @BeforeEach
  public void init() {
    servicioCreditoTarea = new ServicioCreditoTareaImpl();
  }

  @Test
  public void debeGenerarCreditoMonetarioAlCompletarUnaTarea() {
    //given (2.5 hs a $1000/h = $2500)
    servicioCreditoTarea.registrarTarea(givenUnaTareaValida());
    Long usuarioId = 10L;
    //when
    servicioCreditoTarea.completarTareaYGenerarCredito(1L, usuarioId);
    //then
    thenObtengoElCreditoEsperado(usuarioId);
  }

  @Test
  public void debeDescontarCreditoAcumuladoDelMontoBrutoACompartir() {
    // 1 hs = $1000 y 0.5 hs = $500 (Total $1500)
    Tarea tarea1 = new Tarea(1L, "Lavar los platos", 1.0, 1000.0);
    Tarea tarea2 = new Tarea(2L, "Sacar la basura", 0.5, 1000.0);
    servicioCreditoTarea.registrarTarea(tarea1);
    servicioCreditoTarea.registrarTarea(tarea2);

    Long usuarioId = 10L;
    servicioCreditoTarea.completarTareaYGenerarCredito(1L, usuarioId);
    servicioCreditoTarea.completarTareaYGenerarCredito(2L, usuarioId);

    Double montoBrutoGasto = 5000.0;
    Double montoNeto = servicioCreditoTarea.calcularMontoNetoAPagar(montoBrutoGasto, usuarioId);

    assertThat(montoNeto, equalTo(3500.0));
  }

  @Test
  public void debeLanzarExcepcionSiSeIntentaCompletarUnaTareaYaCompletada() {
    //given
    Tarea tarea = new Tarea(1L, "Limpiar baño", 3.0, 1000.0);
    servicioCreditoTarea.registrarTarea(tarea);
    Long usuario1 = 10L;
    Long usuario2 = 20L;
    //when
    servicioCreditoTarea.completarTareaYGenerarCredito(1L, usuario1);
    //then
    thenDebeLanzarExceptionAlCompletarUnaTareaDosVeces(usuario2);
  }

  @Test
  public void debeLanzarExcepcionSiLasHorasDedicadasSonInvalidas() {
    //given + when + then
    thenDebeLanzarExceptionAlRegistrarUnaTareaInvalida(givenUnaTareaInvalida());
  }

  private Tarea givenUnaTareaInvalida() {
    Tarea tareaInvalida = new Tarea(1L, "Pintar pared", -1.0, 1000.0);
    return tareaInvalida;
  }

  private void thenDebeLanzarExceptionAlRegistrarUnaTareaInvalida(Tarea tarea) {
    assertThrows(
      ValorCreditoInvalidoException.class,
      () -> {
        servicioCreditoTarea.registrarTarea(tarea);
      }
    );
  }

  private void thenObtengoElCreditoEsperado(Long usuarioId) {
    Double creditoObtenido = servicioCreditoTarea.obtenerCreditoAcumuladoPorUsuario(usuarioId);
    assertThat(creditoObtenido, equalTo(2500.0));
  }

  private Tarea givenUnaTareaValida() {
    Tarea tarea = new Tarea(1L, "Limpieza de cocina", 2.5, 1000.0);
    return tarea;
  }

  private void thenDebeLanzarExceptionAlCompletarUnaTareaDosVeces(Long usuario2) {
    assertThrows(
      TareaYaCompletadaException.class,
      () -> {
        servicioCreditoTarea.completarTareaYGenerarCredito(1L, usuario2);
      }
    );
  }
}
