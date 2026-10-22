package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioHogar {
  Hogar obtenerHogarPorNombre(String nombre);

  List<Hogar> obtenerHogaresDeUsuario(String email);

  void guardar(Hogar hogar);
  Hogar buscarPorId(Long id);
  List<Hogar> obtenerTodos();
}
