package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class TareaRepetitiva {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String titulo;

  private Double valorPorHora;

  // peso de tarea tipo 0.5 lavar platos y 1 para lavar baño (a revisar) / null = usa el precio por hora del hogar

  @Enumerated(EnumType.STRING)
  private FrecuenciaTarea frecuencia;

  //private Enum<FrecuenciaTarea> frecuencia;

  @ManyToOne
  private Hogar hogar;

  @ManyToOne
  private Usuario usuarioAsignado;

  public TareaRepetitiva() {}

  public TareaRepetitiva(Long id, String titulo) {
    this.id = id;
    this.titulo = titulo;
  }

  public Double obtenerValorPorHora() {
    if (valorPorHora != null) {
      return valorPorHora;
    }
    if (hogar != null) {
      return hogar.getPrecioHoraTarea();
    }
    return null;
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

  public Usuario getUsuarioAsignado() {
    return usuarioAsignado;
  }

  public void setUsuarioAsignado(Usuario usuarioAsignado) {
    this.usuarioAsignado = usuarioAsignado;
  }

  public Hogar getHogar() {
    return hogar;
  }

  public void setHogar(Hogar hogar) {
    this.hogar = hogar;
  }

  public FrecuenciaTarea getFrecuencia() {
    return frecuencia;
  }

  public void setFrecuencia(FrecuenciaTarea frecuencia) {
    this.frecuencia = frecuencia;
  }

  public Double getValorPorHora() {
    return valorPorHora;
  }

  public void setValorPorHora(double valorPorHora) {
    this.valorPorHora = valorPorHora;
  }
}
