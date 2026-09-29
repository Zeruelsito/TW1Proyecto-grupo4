package com.tallerwebi.dominio;

public enum EstadoTarea {
  PENDIENTE("Pendiente"),
  COMPLETADA("Completada");

  private final String descripcion;

  EstadoTarea(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return descripcion;
  }
}
