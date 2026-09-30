package com.tallerwebi.dominio.excepcion;

public class VotacionCerradaException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public VotacionCerradaException(String mensaje) {
    super(mensaje);
  }
}
