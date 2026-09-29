package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.EstadoPropuesta;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Propuesta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String titulo;

  @Column(length = 1000)
  private String descripcion;

  private Long idUsuarioCreador;
  private LocalDate fechaLimite;
  private double porcentajeQuorum;

  @Enumerated(EnumType.STRING)
  private EstadoPropuesta estado;

  public Propuesta() {}

  public Propuesta(
    String titulo,
    String descripcion,
    Long idUsuarioCreador,
    LocalDate fechaLimite,
    double porcentajeQuorum,
    EstadoPropuesta estado
  ) {
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.idUsuarioCreador = idUsuarioCreador;
    this.fechaLimite = fechaLimite;
    this.porcentajeQuorum = porcentajeQuorum;
    this.estado = estado;
  }

  public boolean estaPendiente() {
    return EstadoPropuesta.PENDIENTE.equals(estado);
  }

  public boolean estaVencida(LocalDate hoy) {
    return fechaLimite != null && hoy.isAfter(fechaLimite);
  }

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

  public Long getIdUsuarioCreador() {
    return idUsuarioCreador;
  }

  public void setIdUsuarioCreador(Long idUsuarioCreador) {
    this.idUsuarioCreador = idUsuarioCreador;
  }

  public LocalDate getFechaLimite() {
    return fechaLimite;
  }

  public void setFechaLimite(LocalDate fechaLimite) {
    this.fechaLimite = fechaLimite;
  }

  public double getPorcentajeQuorum() {
    return porcentajeQuorum;
  }

  public void setPorcentajeQuorum(double porcentajeQuorum) {
    this.porcentajeQuorum = porcentajeQuorum;
  }

  public EstadoPropuesta getEstado() {
    return estado;
  }

  public void setEstado(EstadoPropuesta estado) {
    this.estado = estado;
  }
}
