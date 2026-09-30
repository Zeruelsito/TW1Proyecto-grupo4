package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioTareaRepetitiva {
  void registrar(TareaRepetitiva tarea);

  void asignarEquitativamente(Long tareaId, List<Usuario> integrantes);
  //la modifica, le pone su usuario asignado asi queda en el objeto
}
