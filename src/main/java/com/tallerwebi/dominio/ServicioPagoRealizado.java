package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioPagoRealizado {
  void registrarPagoExitoso(PagoRealizado pago);
  List<PagoRealizado> obtenerHistorialPagos(Long hogarId);
}
