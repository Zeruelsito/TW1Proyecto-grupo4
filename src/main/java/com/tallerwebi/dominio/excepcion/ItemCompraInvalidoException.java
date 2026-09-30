package com.tallerwebi.dominio.excepcion;

public class ItemCompraInvalidoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ItemCompraInvalidoException(String mensaje) {
        super(mensaje);
    }
    
}
