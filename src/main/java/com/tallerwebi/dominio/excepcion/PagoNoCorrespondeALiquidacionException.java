package com.tallerwebi.dominio.excepcion;

/** Se lanza cuando se intenta registrar un pago que no figura en la liquidación final del integrante. */
public class PagoNoCorrespondeALiquidacionException extends RuntimeException {

  private static final long serialVersionUID = 1L;
}
