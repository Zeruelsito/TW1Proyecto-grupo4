package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioHogar {
  void guardar(Hogar hogar);
  Hogar buscarPorId(Long id);
  List<Hogar> obtenerTodos();
}
