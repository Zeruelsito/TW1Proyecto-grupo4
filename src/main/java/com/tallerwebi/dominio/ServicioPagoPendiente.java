package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioPagoPendiente {
  void guardarPendiente(PagoPendiente pagoPendiente);
  List<PagoPendiente> obtenerPendientes(Hogar hogar);
  void eliminarPendiente(String emisor, String receptor, Double monto, Hogar hogar);
  void eliminarPorId(Long id);
  PagoPendiente buscarPorId(Long id);
  boolean existePendiente(String emisor, String receptor, Double monto, Hogar hogar);
}
