package com.tallerwebi.dominio;

public class FilaCuota {

  private final String integrante;
  private final Double cuotaAntes;
  private final Double cuotaDespues;
  private final Double horasAntes;
  private final Double horasDespues;
  private final Double parteFijaAntes;
  private final Double parteFijaDespues;

  public FilaCuota(
    String integrante,
    Double cuotaAntes,
    Double cuotaDespues,
    Double horasAntes,
    Double horasDespues,
    Double parteFijaAntes,
    Double parteFijaDespues
  ) {
    this.integrante = integrante;
    this.cuotaAntes = cuotaAntes;
    this.cuotaDespues = cuotaDespues;
    this.horasAntes = horasAntes;
    this.horasDespues = horasDespues;
    this.parteFijaAntes = parteFijaAntes;
    this.parteFijaDespues = parteFijaDespues;
  }

  public String getIntegrante() {
    return integrante;
  }

  public Double getCuotaAntes() {
    return cuotaAntes;
  }

  public Double getCuotaDespues() {
    return cuotaDespues;
  }

  public Double getHorasAntes() {
    return horasAntes;
  }

  public Double getHorasDespues() {
    return horasDespues;
  }

  public Double getParteFijaAntes() {
    return parteFijaAntes;
  }

  public Double getParteFijaDespues() {
    return parteFijaDespues;
  }

  public Double getDiferenciaCuota() {
    if (cuotaAntes == null || cuotaDespues == null) {
      return null;
    }
    return cuotaDespues - cuotaAntes;
  }

  public Double getDiferenciaParteFija() {
    return parteFijaDespues - parteFijaAntes;
  }
}
