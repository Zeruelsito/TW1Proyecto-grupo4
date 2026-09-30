package com.tallerwebi.dominio.excepcion;

public class SinItemsMarcadosException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public SinItemsMarcadosException(String mensaje) {
    super(mensaje);
  }
}
