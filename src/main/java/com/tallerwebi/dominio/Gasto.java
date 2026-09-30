package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
//import java.math.BigDecimal;

import java.time.LocalDateTime;

@Entity
public class Gasto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String descripcion;
  private Double monto;
  private Long pagadorId;
  private String categoria;
  private TipoGasto tipoGasto;
  private LocalDateTime fecha;

  public Gasto() {}

  public Gasto(
    TipoGasto tipoGasto,
    String descripcion,
    Double monto,
    Long pagadorId,
    String categoria
  ) {
    this.tipoGasto = tipoGasto;
    this.descripcion = descripcion;
    this.monto = monto;
    this.pagadorId = pagadorId;
    this.categoria = categoria;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public Double getMonto() {
    return monto;
  }

  public void setMonto(Double monto) {
    this.monto = monto;
  }

  public Long getPagadorId() {
    return pagadorId;
  }

  public void setPagadorId(Long pagadorId) {
    this.pagadorId = pagadorId;
  }

  public String getCategoria() {
    return categoria;
  }

  public void setCategoria(String categoria) {
    this.categoria = categoria;
  }

  public TipoGasto getTipoGasto() {
    return tipoGasto;
  }

  public void setTipoGasto(TipoGasto tipoGasto) {
    this.tipoGasto = tipoGasto;
  }

  public LocalDateTime getFecha() {
    return fecha;
  }

  public void setFecha(LocalDateTime fecha) {
    this.fecha = fecha;
  }
}
