package com.tallerwebi.dominio;

import java.util.List;
import java.util.Map;

public interface ServicioCreditoTarea {
  void registrarTarea(Tarea tarea);
  void completarTareaYGenerarCredito(
    Long tareaId,
    Long usuarioId,
    Double horasDedicadas,
    Double tarifaPorHora
  );
  Double obtenerCreditoAcumuladoPorUsuario(Long usuarioId);
  Double calcularMontoNetoAPagar(Double montoBruto, Long usuarioId);
  List<Tarea> obtenerTodasLasTareas();

  void registrarTareaDesdeCatalogo(Long tareaRepetitivaId, Double horasDedicadas, Long hogarId);
  void liquidarCreditosDeUsuario(Long usuarioId);
  Double obtenerCreditoLiquidadoPorUsuario(Long usuarioId);
  void liquidarCreditosParciales(Long usuarioId, double montoMaximo);

  Map<String, Double> calcularMetricasBrechaUsuario(
    Usuario usuario,
    Hogar hogar,
    List<Gasto> gastos,
    Double precioHora
  );
  void procesarLiquidacionCreditos(Long usuarioId, Long hogarId, List<Gasto> gastos, Hogar hogar);
}
