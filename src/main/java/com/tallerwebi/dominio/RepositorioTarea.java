package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioTarea {
  void guardar(Tarea tarea);
  Tarea buscarPorId(Long id);
  List<Tarea> obtenerTodas();
  List<Tarea> obtenerPorUsuario(Usuario usuarioRealizador);
}
