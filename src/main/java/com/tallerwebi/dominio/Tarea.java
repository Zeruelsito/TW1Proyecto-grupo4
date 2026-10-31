package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class Tarea {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String titulo;
  private String descripcion;
  private Double horasDedicadas;
  private Double valorCredito;
  private EstadoTarea estado;
  private Boolean descontableDeLiquidacion;

  @ManyToOne
  @JoinColumn(name = "usuario_realizador_id")
  private Usuario usuarioRealizador;

  @ManyToOne
  @JoinColumn(name = "tarea_repetitiva_id")
  private TareaRepetitiva tareaRepetitiva;

  private LocalDateTime fechaRegistro;

  public Tarea() {
    this.estado = EstadoTarea.PENDIENTE;
    this.valorCredito = 0.0;
    this.fechaRegistro = LocalDateTime.now();
  }

  // Constructor simplificado para tareas manuales
  public Tarea(String titulo) {
    this.titulo = titulo;
    this.estado = EstadoTarea.PENDIENTE;
    this.valorCredito = 0.0;
    this.fechaRegistro = LocalDateTime.now();
  }

  public Tarea(
    String titulo,
    Double horasDedicadas,
    Double tarifaPorHora,
    Usuario usuarioRealizador
  ) {
    this.titulo = titulo;
    this.horasDedicadas = horasDedicadas;
    this.valorCredito = horasDedicadas != null ? horasDedicadas * tarifaPorHora : 0.0;
    this.estado = EstadoTarea.PENDIENTE;
    this.usuarioRealizador = usuarioRealizador;
    this.fechaRegistro = LocalDateTime.now();
  }

  // Getters y Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public Double getHorasDedicadas() {
    return horasDedicadas;
  }

  public void setHorasDedicadas(Double horasDedicadas, Double tarifaPorHora) {
    this.horasDedicadas = horasDedicadas;
    if (horasDedicadas != null && tarifaPorHora != null) {
      this.valorCredito = horasDedicadas * tarifaPorHora;
    }
  }

  public void setHorasDedicadas(Double horasDedicadas) {
    this.horasDedicadas = horasDedicadas;
  }

  public Double getValorCredito() {
    return valorCredito;
  }

  public void setValorCredito(Double valorCredito) {
    this.valorCredito = valorCredito;
  }

  public EstadoTarea getEstado() {
    return estado;
  }

  public void setEstado(EstadoTarea estado) {
    this.estado = estado;
  }

  public Usuario getUsuarioRealizador() {
    return usuarioRealizador;
  }

  public void setUsuarioRealizador(Usuario usuarioRealizador) {
    this.usuarioRealizador = usuarioRealizador;
  }

  public Long getUsuarioRealizadorId() {
    return usuarioRealizador != null ? usuarioRealizador.getId() : null;
  }

  public LocalDateTime getFechaRegistro() {
    return fechaRegistro;
  }

  public void setFechaRegistro(LocalDateTime fechaRegistro) {
    this.fechaRegistro = fechaRegistro;
  }

  public TareaRepetitiva getTareaRepetitiva() {
    return tareaRepetitiva;
  }

  public void setTareaRepetitiva(TareaRepetitiva tareaRepetitiva) {
    this.tareaRepetitiva = tareaRepetitiva;
  }

  public Boolean getDescontableDeLiquidacion() {
    return descontableDeLiquidacion;
  }

  public void setDescontableDeLiquidacion(Boolean descontableDeLiquidacion) {
    this.descontableDeLiquidacion = descontableDeLiquidacion;
  }
}
