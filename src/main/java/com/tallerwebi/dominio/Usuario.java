package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.Getter;
import lombok.Setter;

@Entity
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String email;
  private String password;
  private String rol;
  private Boolean activo = false;
  private String nombre;
  private Double ingresoMensual;
  private Double capacidadRelativaPagoMensual;
  private Double cuotaMensual;
  private Double horasATrabajar;
  private static Double creditosPorTareas;
  private Double porcentajeSegunCapacidadDePagoMensual;

  public Double getCuotaMensual() {
    return cuotaMensual;
  }

  public void setCuotaMensual(Double cuotaMensual) {
    this.cuotaMensual = cuotaMensual;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
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
    double porcentaje = this.porcentajeSegunCapacidadDePagoMensual;

    // Redondea para arriba el double y tomma solo los dos decimales despues de la coma
    return BigDecimal.valueOf(porcentaje).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
  }

  public Usuario(String eze, Double ingresoMensual, Double horasATrabajar) {
    this.nombre = eze;
    this.ingresoMensual = ingresoMensual;
    this.horasATrabajar = horasATrabajar;
  }

  public Usuario() {}

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
}
