package com.tallerwebi.dominio;

//import com.tallerwebi.dominio.EstadoPropuesta;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Propuesta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String titulo;

  @Column(length = 1000)
  private String descripcion;

  private Long idUsuarioCreador;
  private Long hogarId;
  private LocalDate fechaLimite;
  private double porcentajeQuorum;

  @Enumerated(EnumType.STRING)
  private EstadoPropuesta estado;

  @Transient
  private List<DetalleVoto> votos = new ArrayList<>();

  @Transient
  private long faltanVotar;

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

  public Boolean fueCreadaPor(Long idUsuario) {
    return idUsuarioCreador != null && idUsuarioCreador.equals(idUsuario);
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

  public Long getHogarId() {
    return hogarId;
  }

  public void setHogarId(Long hogarId) {
    this.hogarId = hogarId;
  }

  public boolean perteneceAlHogar(Long idHogar) {
    return hogarId != null && hogarId.equals(idHogar);
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

  public List<DetalleVoto> getVotos() {
    return votos;
  }

  public void setVotos(List<DetalleVoto> votos) {
    this.votos = votos;
  }

  public long getFaltanVotar() {
    return faltanVotar;
  }

  public void setFaltanVotar(long faltanVotar) {
    this.faltanVotar = faltanVotar;
  }
}
