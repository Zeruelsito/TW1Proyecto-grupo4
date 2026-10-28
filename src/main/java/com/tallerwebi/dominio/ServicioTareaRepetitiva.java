package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioTareaRepetitiva {
  void registrar(TareaRepetitiva tarea, Long hogarId);

  void asignarEquitativamente(Long tareaId, List<Usuario> integrantes, Long hogarId);

  List<TareaRepetitiva> obtenerTodas(Long hogarId);
}
