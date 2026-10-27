package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioTareaRepetitiva {
  void guardar(TareaRepetitiva tareaRepetitiva);
  List<TareaRepetitiva> obtenerTodos();
  TareaRepetitiva obtenerPorId(Long id);
  int contarAsignadasAUsuario(Usuario usuario); // borrar

  List<TareaRepetitiva> obtenerPorHogar(Long hogarId);
}
