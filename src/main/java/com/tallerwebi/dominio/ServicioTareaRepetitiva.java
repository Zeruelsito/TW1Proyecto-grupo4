package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioTareaRepetitiva {
  void registrar(TareaRepetitiva tarea, Long hogarId);

  void asignarEquitativamente(Long tareaId, List<Usuario> integrantes, Long hogarId);

  List<TareaRepetitiva> obtenerTodas(Long hogarId);

  void guardarComoPlantilla(String titulo, Long hogarId);
  void guardarComoPlantillaPersonal(String titulo, Long hogarId, Usuario propietario);
  void quitarPlantilla(Long plantillaId, Long hogarId, Long usuarioId);

  List<TareaRepetitiva> obtenerCatalogo(Long hogarId, Long usuarioId);
}
