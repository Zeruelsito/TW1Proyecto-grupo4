package com.tallerwebi.dominio;

import jakarta.persistence.PrePersist;         
import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private String email;
  private String password;
  private String rol;
  private Boolean activo = false;
  private LocalDateTime fechaAlta;
  private LocalDateTime fechaBaja;

  public Usuario() {}

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

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }
  
  @PrePersist 
  void registrarFechaAlta() {
    if (fechaAlta == null)
    fechaAlta = LocalDateTime.now();
  }

  public boolean estaDeBaja() {
    return fechaBaja != null;
  }

  public boolean participaEn(LocalDateTime fechaGasto){
    if(fechaGasto == null){
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
