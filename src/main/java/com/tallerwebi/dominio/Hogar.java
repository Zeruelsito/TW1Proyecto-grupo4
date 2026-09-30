package com.tallerwebi.dominio;

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

  @OneToMany
  private List<CambioParametro> historial;

  public Hogar(String nombreHogar) {
    this.nombreHogar = nombreHogar;
    this.integrantes = new ArrayList<>();
    this.precioHoraTarea = 0.0;
    this.pisoMinimoHogar = 0.0;
    this.historial = new ArrayList<>();
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

  public Object getId() {
    return id;
  }

  public Object getNombreHogar() {
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
}
