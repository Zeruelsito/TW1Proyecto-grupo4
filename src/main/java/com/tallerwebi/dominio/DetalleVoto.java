package com.tallerwebi.dominio;

public class DetalleVoto {

  private final String nombreUsuario;
  private final boolean esAfirmativo;
  private final String comentario;

  public DetalleVoto(String nombreUsuario, boolean esAfirmativo, String comentario) {
    this.nombreUsuario = nombreUsuario;
    this.esAfirmativo = esAfirmativo;
    this.comentario = comentario;
  }

  public String getNombreUsuario() {
    return nombreUsuario;
  }

  public boolean isEsAfirmativo() {
    return esAfirmativo;
  }

  public String getComentario() {
    return comentario;
  }

  public boolean tieneComentario() {
    return comentario != null && !comentario.trim().isEmpty();
  }
}
