package com.tallerwebi.dominio;

/**
 * FrecuenciaTarea
 */
public enum FrecuenciaTarea {
  DIARIA("diaria"),
  SEMANAL("semanal"),
  MENSUAL("mensual"),
  ANUAL("anual");

  private String frecuencia;

  FrecuenciaTarea(String frecuencia) {
    this.frecuencia = frecuencia;
  }

  public String getDescripcion() {
    return frecuencia;
  }
}
