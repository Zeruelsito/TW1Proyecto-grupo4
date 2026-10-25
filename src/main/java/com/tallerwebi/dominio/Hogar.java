package com.tallerwebi.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Hogar {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombreHogar;

  @ManyToMany
  private List<Usuario> integrantes;

  private Double precioHoraTarea;
  private Double pisoMinimoHogar;
  private String pin;

  @OneToMany(cascade = CascadeType.ALL)
  private List<CambioParametro> historial;

  public Hogar() {
    this(null);
  }

  public Hogar(String nombreHogar) {
    this.nombreHogar = nombreHogar;
    this.integrantes = new ArrayList<>();
    this.precioHoraTarea = 0.0;
    this.pisoMinimoHogar = 0.0;
    this.historial = new ArrayList<>();
  }

  public Hogar(String nombreHogar, String pin) {
    this.nombreHogar = nombreHogar;
    this.integrantes = new ArrayList<>();
    this.precioHoraTarea = 5000.0;
    this.pisoMinimoHogar = 50000.0;
    this.historial = new ArrayList<>();
    this.pin = pin;
  }

  public List<Usuario> getIntegrantes() {
    return integrantes;
  }

  public void setIntegrantes(List<Usuario> integrantes) {
    this.integrantes = integrantes;
  }

  public Double getPrecioHoraTarea() {
    return precioHoraTarea;
  }

  public void setPrecioHoraTarea(Double precioHoraTarea) {
    this.precioHoraTarea = precioHoraTarea;
  }

  public Double getPisoMinimoHogar() {
    return pisoMinimoHogar;
  }

  public void setPisoMinimoHogar(Double pisoMinimoHogar) {
    this.pisoMinimoHogar = pisoMinimoHogar;
  }

  public Long getId() {
    return id;
  }

  public String getNombreHogar() {
    return nombreHogar;
  }

  public void setId(long id1) {
    id = id1;
  }

  public List<CambioParametro> getHistorial() {
    return historial;
  }

  public void setHistorial(List<CambioParametro> historial) {
    this.historial = historial;
  }

  public String getPin() {
    return pin;
  }

  public void setPin(String pin) {
    this.pin = pin;
  }
}
