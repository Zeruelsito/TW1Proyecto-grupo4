package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
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

  @ManyToOne
  private Usuario usuarioAsignado;

  public TareaRepetitiva(Long id, String titulo) {
    this.id = id;
    this.titulo = titulo;
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
}
