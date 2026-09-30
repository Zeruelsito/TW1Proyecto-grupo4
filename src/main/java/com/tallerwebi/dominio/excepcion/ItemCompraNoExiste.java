package com.tallerwebi.dominio.excepcion;

public class ItemCompraNoExiste extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ItemCompraNoExiste(String mensaje) {
    super(mensaje);
  }
}
