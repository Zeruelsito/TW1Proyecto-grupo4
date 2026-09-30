package com.tallerwebi.dominio;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public class ReporteMensual {

  private final YearMonth mes;
  private final List<Gasto> gastos;
  private final List<Tarea> tareas;
  private final List<FilaBalance> balances;
  private final List<TransferenciaSugerida> transferencias;
  private final Map<Long, String> nombresPorId;

  public ReporteMensual(
    YearMonth mes,
    List<Gasto> gastos,
    List<Tarea> tareas,
    List<FilaBalance> balances,
    List<TransferenciaSugerida> transferencias,
    Map<Long, String> nombresPorId
  ) {
    this.mes = mes;
    this.gastos = gastos;
    this.tareas = tareas;
    this.balances = balances;
    this.transferencias = transferencias;
    this.nombresPorId = nombresPorId;
  }

  public YearMonth getMes() {
    return mes;
  }

  public List<Gasto> getGastos() {
    return gastos;
  }

  public List<Tarea> getTareas() {
    return tareas;
  }

  public List<FilaBalance> getBalances() {
    return balances;
  }

  public List<TransferenciaSugerida> getTransferencias() {
    return transferencias;
  }

  public double getTotalGastos() {
    double total = 0;
    for (Gasto gasto : gastos) {
      total += gasto.getMonto();
    }
    return total;
  }

  public String nombreDe(Long idUsuario) {
    if (idUsuario == null) {
      return "-";
    }
    String nombre = nombresPorId.get(idUsuario);
    return nombre != null ? nombre : "Usuario " + idUsuario;
  }
}
