package com.tallerwebi.dominio;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioPagoRealizado {
  void guardar(PagoRealizado pago);
  List<PagoRealizado> obtenerPorHogarId(Long hogarId);
}
