package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class CambioParametro {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String parametro;
  private Double valorAnterior;
  private Double valorNuevo;
  private LocalDateTime fecha;

  public CambioParametro() {}

  public CambioParametro(String parametro, Double valorAnterior, Double valorNuevo) {
    this.parametro = parametro;
    this.valorAnterior = valorAnterior;
    this.valorNuevo = valorNuevo;
    this.fecha = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public String getParametro() {
    return parametro;
  }

  public Double getValorAnterior() {
    return valorAnterior;
  }

  public Double getValorNuevo() {
    return valorNuevo;
  }

  public LocalDateTime getFecha() {
    return fecha;
  }
}
