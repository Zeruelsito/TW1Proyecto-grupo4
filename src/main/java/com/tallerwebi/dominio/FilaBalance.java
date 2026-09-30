package com.tallerwebi.dominio;

public class FilaBalance {

  private final String integrante;
  private final double totalPagado;
  private final double creditoTareas;
  private final double cuotaEquitativa;

  public FilaBalance(
    String integrante,
    double totalPagado,
    double creditoTareas,
    double cuotaEquitativa
  ) {
    this.integrante = integrante;
    this.totalPagado = totalPagado;
    this.creditoTareas = creditoTareas;
    this.cuotaEquitativa = cuotaEquitativa;
  }

  public String getIntegrante() {
    return integrante;
  }

  public double getTotalPagado() {
    return totalPagado;
  }

  public double getCreditoTareas() {
    return creditoTareas;
  }

  public double getCuotaEquitativa() {
    return cuotaEquitativa;
  }

  public double getBalanceNeto() {
    return totalPagado + creditoTareas - cuotaEquitativa;
  }
}
