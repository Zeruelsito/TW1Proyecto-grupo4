package com.tallerwebi.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
//import java.math.BigDecimal;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@SuppressWarnings("PMD.TooManyFields")
public class Gasto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String codigo;

  @ManyToOne
  @JoinColumn(name = "hogar_id")
  private Hogar hogar;

  private String descripcion;
  private Double monto;
  private String categoria;
  private Boolean pagado;

  @ManyToOne
  @JoinColumn(name = "usuario_pagador_id")
  private Usuario usuarioPagador;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
    name = "gasto_participante",
    joinColumns = @JoinColumn(name = "gasto_id"),
    inverseJoinColumns = @JoinColumn(name = "usuario_id")
  )
  private List<Usuario> participantes = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  private TipoGasto tipoGasto;

  private LocalDateTime fecha;
  private LocalDateTime fechaVencimiento;

  public Gasto() {}

  public Gasto(
    String codigo,
    TipoGasto tipoGasto,
    String descripcion,
    Double monto,
    Usuario usuarioPagador,
    String categoria
  ) {
    this.codigo = codigo;
    this.tipoGasto = tipoGasto;
    this.descripcion = descripcion;
    this.monto = monto;
    this.usuarioPagador = usuarioPagador;
    this.categoria = categoria;
    this.pagado = false;
    this.fecha = LocalDateTime.now();
  }

  public Gasto(
    String codigo,
    TipoGasto tipoGasto,
    String descripcion,
    Double monto,
    String categoria
  ) {
    this.codigo = codigo;
    this.tipoGasto = tipoGasto;
    this.descripcion = descripcion;
    this.monto = monto;
    this.categoria = categoria;
    this.pagado = false;
    this.fecha = LocalDateTime.now();
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

  public Usuario getUsuarioPagador() {
    return usuarioPagador;
  }

  public void setUsuarioPagador(Usuario usuarioPagador) {
    this.usuarioPagador = usuarioPagador;
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

  public String getCodigo() {
    return codigo;
  }

  public Boolean getPagado() {
    return pagado;
  }

  public void setPagado(Boolean pagado) {
    this.pagado = pagado;
  }

  public LocalDateTime getFechaVencimiento() {
    return fechaVencimiento;
  }

  public void setFechaVencimiento(LocalDateTime fechaVencimiento) {
    this.fechaVencimiento = fechaVencimiento;
  }

  public Hogar getHogar() {
    return hogar;
  }

  public void setHogar(Hogar hogar) {
    this.hogar = hogar;
  }

  public List<Usuario> getParticipantes() {
    return participantes;
  }

  public void setParticipantes(List<Usuario> participantes) {
    this.participantes = participantes;
  }
}
