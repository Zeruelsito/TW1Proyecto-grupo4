package com.tallerwebi.dominio;

import jakarta.persistence.*;

@Entity
public class Voto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public Long idUsuario;
  public Long idPropuesta;
  public boolean esAfirmativo;

  @Column(length = 1000)
  public String comentario;

  public Voto() {}

  public Voto(Long idUsuario, Long idPropuesta, boolean esAfirmativo, String comentario) {
    this.idUsuario = idUsuario;
    this.idPropuesta = idPropuesta;
    this.esAfirmativo = esAfirmativo;
    this.comentario = comentario;
  }

  public Long getIdVoto() {
    return id;
  }

  public void setIdVoto(Long idVoto) {
    this.id = idVoto;
  }

  public Long getIdUsuario() {
    return idUsuario;
  }

  public void setIdUsuario(Long idUsuario) {
    this.idUsuario = idUsuario;
  }

  public Long getIdPropuesta() {
    return idPropuesta;
  }

  public void setIdPropuesta(Long idPropuesta) {
    this.idPropuesta = idPropuesta;
  }

  public boolean isEsAfirmativo() {
    return esAfirmativo;
  }

  public void setEsAfirmativo(boolean esAfirmativo) {
    this.esAfirmativo = esAfirmativo;
  }

  public String getComentario() {
    return comentario;
  }

  public void setComentario(String comentario) {
    this.comentario = comentario;
  }
}
