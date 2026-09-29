package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioCreditoTarea {
  void registrarTarea(Tarea tarea);
  void completarTareaYGenerarCredito(Long tareaId, Long usuarioId);
  Double obtenerCreditoAcumuladoPorUsuario(Long usuarioId);
  Double calcularMontoNetoAPagar(Double montoBruto, Long usuarioId);
  List<Tarea> obtenerTodasLasTareas();
}
