package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioPagoPendiente {
  void guardarPendiente(PagoPendiente pagoPendiente);
  List<PagoPendiente> obtenerPendientes(Hogar hogar);
  void eliminarPendiente(Usuario emisor, Usuario receptor, Double monto, Hogar hogar);
  void eliminarPorId(Long id);
  PagoPendiente buscarPorId(Long id);
  boolean existePendiente(Usuario emisor, Usuario receptor, Double monto, Hogar hogar);
}
