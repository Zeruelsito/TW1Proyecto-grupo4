package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@SuppressWarnings("PMD.TooManyFields")
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private String email;
  private String password;
  private String rol;
  private Boolean activo = false;
  private Double ingresoMensual;
  private Double capacidadRelativaPagoMensual;
  private Double cuotaMensual;
  private Double horasATrabajar;
  private static Double creditosPorTareas;
  private Double porcentajeSegunCapacidadDePagoMensual;
  private Double horasTareaARealizar;

  public Double getHorasTareaARealizar() {
    return horasTareaARealizar;
  }

  public void setHorasTareaARealizar(Double horasTareaARealizar) {
    this.horasTareaARealizar = horasTareaARealizar;
  }

  public Double getCuotaMensual() {
    return cuotaMensual;
  }

  public void setCuotaMensual(Double cuotaMensual) {
    this.cuotaMensual = cuotaMensual;
  }

  public Double getIngresoMensual() {
    return ingresoMensual;
  }

  public void setIngresoMensual(Double ingresoMensual) {
    this.ingresoMensual = ingresoMensual;
  }

  public Double getCapacidadRelativaPagoMensual() {
    return capacidadRelativaPagoMensual;
  }

  public void setCapacidadRelativaPagoMensual(Double capacidadRelativaPagoMensual) {
    this.capacidadRelativaPagoMensual = capacidadRelativaPagoMensual;
  }

  public Double getHorasATrabajar() {
    return horasATrabajar;
  }

  public void setHorasATrabajar(Double horasATrabajar) {
    this.horasATrabajar = horasATrabajar;
  }

  public static Double getCreditosPorTareas() {
    return creditosPorTareas;
  }

  public static void setCreditosPorTareas(Double creditosPorTareas) {
    Usuario.creditosPorTareas = creditosPorTareas;
  }

  public void setPorcentajeSegunCapacidadDePagoMensual(
    Double porcentajeSegunCapacidadDePagoMensual
  ) {
    this.porcentajeSegunCapacidadDePagoMensual = porcentajeSegunCapacidadDePagoMensual;
  }

  public Double getPorcentajeSegunCapacidadDePagoMensual() {
    if (this.porcentajeSegunCapacidadDePagoMensual == null) {
      return 0.0;
    }
    double porcentaje = this.porcentajeSegunCapacidadDePagoMensual;
    // Redondea para arriba el double y tomma solo los dos decimales despues de la coma
    return BigDecimal.valueOf(porcentaje).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
  }

  public Usuario(String nombre, Double ingresoMensual) {
    this.nombre = nombre;
    this.ingresoMensual = ingresoMensual;
  }

  public Usuario() {}

  private LocalDateTime fechaAlta;
  private LocalDateTime fechaBaja;

  public Usuario(String nombre, String email, String password) {
    this.nombre = nombre;
    this.email = email;
    this.password = password;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getRol() {
    return rol;
  }

  public void setRol(String rol) {
    this.rol = rol;
  }

  public Boolean getActivo() {
    return activo;
  }

  public void setActivo(Boolean activo) {
    this.activo = activo;
  }

  public void activar() {
    activo = true;
  }

  @PrePersist
  void registrarFechaAlta() {
    if (fechaAlta == null) fechaAlta = LocalDateTime.now();
  }

  public boolean estaDeBaja() {
    return fechaBaja != null;
  }

  public boolean participaEn(LocalDateTime fechaGasto) {
    if (fechaGasto == null) {
      return true;
    }
    boolean ingresoAntes = fechaAlta == null || !fechaAlta.isAfter(fechaGasto);
    boolean salioDespues = fechaBaja == null || !fechaBaja.isBefore(fechaGasto);
    return ingresoAntes && salioDespues;
  }

  public LocalDateTime getFechaAlta() {
    return fechaAlta;
  }

  public void setFechaAlta(LocalDateTime fechaAlta) {
    this.fechaAlta = fechaAlta;
  }

  public LocalDateTime getFechaBaja() {
    return fechaBaja;
  }

  public void setFechaBaja(LocalDateTime fechaBaja) {
    this.fechaBaja = fechaBaja;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }
}
