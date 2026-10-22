package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioHogar {
  Hogar obtenerHogarPorNombre(String nombre);

  List<Hogar> obtenerHogaresDeUsuario(String email);

  List<Hogar> obtenerHogares();

  Hogar obtenerHogarPorId(Long hogarId);

  void guardar(Hogar hogar);
}
