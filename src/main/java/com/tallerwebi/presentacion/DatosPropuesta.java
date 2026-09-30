package com.tallerwebi.presentacion;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class DatosPropuesta {

  private String titulo;
  private String descripcion;
  private Double porcentajeQuorum = 50.0;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaLimite;

  public DatosPropuesta() {}

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public Double getPorcentajeQuorum() {
    return porcentajeQuorum;
  }

  public void setPorcentajeQuorum(Double porcentajeQuorum) {
    this.porcentajeQuorum = porcentajeQuorum;
  }

  public LocalDate getFechaLimite() {
    return fechaLimite;
  }

  public void setFechaLimite(LocalDate fechaLimite) {
    this.fechaLimite = fechaLimite;
  }
}