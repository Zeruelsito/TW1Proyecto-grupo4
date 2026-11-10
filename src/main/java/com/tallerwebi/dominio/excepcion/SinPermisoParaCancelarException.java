package com.tallerwebi.dominio.excepcion;

public class SinPermisoParaCancelarException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public SinPermisoParaCancelarException(String mensaje) {
    super(mensaje);
  }
}
