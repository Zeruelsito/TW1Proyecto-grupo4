package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
  private Long usuarioRealizadorId;
  private LocalDateTime fechaRegistro;

  public Tarea() {
    this.estado = EstadoTarea.PENDIENTE;
  }

  public Tarea(Long id, String titulo, Double horasDedicadas, Double tarifaPorHora) {
    this.id = id;
    this.titulo = titulo;
    this.horasDedicadas = horasDedicadas;
    this.valorCredito = horasDedicadas * tarifaPorHora;
    this.estado = EstadoTarea.PENDIENTE;
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

  public void setHorasDedicadas(Double horasDedicadas) {
    this.horasDedicadas = horasDedicadas;
    // Si la tarifa de prueba es $1000/h, calculamos automáticamente el valorCredito al recibir las horas:
    if (horasDedicadas != null) {
      Double tarifaPrueba = 1000.0;
      this.valorCredito = horasDedicadas * tarifaPrueba;
    }
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

  public Long getUsuarioRealizadorId() {
    return usuarioRealizadorId;
  }

  public void setUsuarioRealizadorId(Long usuarioRealizadorId) {
    this.usuarioRealizadorId = usuarioRealizadorId;
  }

  public LocalDateTime getFechaRegistro() {
    return fechaRegistro;
  }

  public void setFechaRegistro(LocalDateTime fechaRegistro) {
    this.fechaRegistro = fechaRegistro;
  }
}
