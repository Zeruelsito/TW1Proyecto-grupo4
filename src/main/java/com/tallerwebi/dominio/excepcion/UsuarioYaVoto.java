package com.tallerwebi.dominio.excepcion;

public class UsuarioYaVoto extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public UsuarioYaVoto(String mensaje) {
    super(mensaje);
  }
}
