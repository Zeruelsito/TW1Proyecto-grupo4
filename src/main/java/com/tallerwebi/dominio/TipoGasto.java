package com.tallerwebi.dominio;

public enum TipoGasto {
  RECURRENTE("Recurrente"),
  OCASIONAL("Ocasional"),
  FIJO("Fijo");

  private final String descripcion;

  TipoGasto(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return descripcion;
  }
}
