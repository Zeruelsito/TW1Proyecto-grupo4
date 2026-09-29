package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.TareaNoEncontradaException;
import com.tallerwebi.dominio.excepcion.TareaYaCompletadaException;
import com.tallerwebi.dominio.excepcion.ValorCreditoInvalidoException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ServicioCreditoTareaImpl implements ServicioCreditoTarea {

  private final List<Tarea> repositorioTareasMock = new ArrayList<>();

  @Override
  public void registrarTarea(Tarea tarea) {
    validarHorasDedicadas(tarea.getHorasDedicadas());

    tarea.setEstado(EstadoTarea.PENDIENTE);

    if (tarea.getId() == null) {
      tarea.setId((long) (repositorioTareasMock.size() + 1));
    }

    repositorioTareasMock.add(tarea);
  }

  @Override
  public void completarTareaYGenerarCredito(Long tareaId, Long usuarioId) {
    Tarea tarea = buscarTareaPorId(tareaId);
    validarEstadoPendiente(tarea);

    tarea.setEstado(EstadoTarea.COMPLETADA);
    tarea.setUsuarioRealizadorId(usuarioId);
    tarea.setFechaRegistro(LocalDateTime.now());
  }

  @Override
  public Double obtenerCreditoAcumuladoPorUsuario(Long usuarioId) {
    double creditoTotal = 0.0;
    for (Tarea tarea : repositorioTareasMock) {
      creditoTotal += calcularCreditoDeTarea(tarea, usuarioId);
    }
    return creditoTotal;
  }

  @Override
  public Double calcularMontoNetoAPagar(Double montoBruto, Long usuarioId) {
    Double credito = obtenerCreditoAcumuladoPorUsuario(usuarioId);
    double saldoResultante = montoBruto - credito;
    return Math.max(0.0, saldoResultante);
  }

  @Override
  public List<Tarea> obtenerTodasLasTareas() {
    return new ArrayList<>(repositorioTareasMock);
  }

  /* --- Métodos Auxiliares Privados (PMD Compliant) --- */

  private void validarHorasDedicadas(Double horas) {
    if (horas == null || horas <= 0) {
      throw new ValorCreditoInvalidoException();
    }
  }

  private Tarea buscarTareaPorId(Long tareaId) {
    for (Tarea tarea : repositorioTareasMock) {
      if (tarea.getId() != null && tarea.getId().equals(tareaId)) {
        return tarea;
      }
    }
    throw new TareaNoEncontradaException();
  }

  private void validarEstadoPendiente(Tarea tarea) {
    if (EstadoTarea.COMPLETADA.equals(tarea.getEstado())) {
      throw new TareaYaCompletadaException();
    }
  }

  private double calcularCreditoDeTarea(Tarea tarea, Long usuarioId) {
    if (
      EstadoTarea.COMPLETADA.equals(tarea.getEstado()) &&
      usuarioId.equals(tarea.getUsuarioRealizadorId())
    ) {
      return tarea.getValorCredito() != null ? tarea.getValorCredito() : 0.0;
    }
    return 0.0;
  }
}
