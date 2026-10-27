package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioHogar {
  void guardar(Hogar hogar);
  void actualizar(Hogar hogar);
  Hogar buscarPorId(Long id);
  List<Hogar> obtenerHogaresDeUsuario(Long usuarioId);
  List<Hogar> obtenerTodos();
}
