package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioPagoPendiente {
  void guardar(PagoPendiente pagoPendiente);
  List<PagoPendiente> obtenerPendientesPorHogar(Hogar hogar);
  void eliminar(PagoPendiente pagoPendiente);
  PagoPendiente buscarPorDatos(String emisor, String receptor, Double monto, Hogar hogar);
  PagoPendiente buscarPorId(Long id);
  void eliminarPorId(Long id);
}
