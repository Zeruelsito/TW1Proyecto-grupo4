package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.HogarSinIntegrantesException;
import com.tallerwebi.dominio.excepcion.TareaNoEncontradaException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ServicioTareaRepetitivaImpl implements ServicioTareaRepetitiva {

  private final List<TareaRepetitiva> tareas = new ArrayList<>();

  @Override
  public void registrar(TareaRepetitiva tarea) {
    tareas.add(tarea);
  }

  @Override
  public void asignarEquitativamente(Long tareaId, List<Usuario> integrantes) {
    if (integrantes.isEmpty()) {
      throw new HogarSinIntegrantesException();
    }
    TareaRepetitiva tarea = buscarporId(tareaId);
    tarea.setUsuarioAsignado(elegirConMenosTareas(tarea, integrantes));
  }

  private TareaRepetitiva buscarporId(Long tareaId) {
    for (TareaRepetitiva tarea : tareas) {
      if (tarea.getId().equals(tareaId)) {
        return tarea;
      }
    }

    throw new TareaNoEncontradaException();
  }

  private Usuario elegirConMenosTareas(TareaRepetitiva tarea, List<Usuario> integrantes) {
    Usuario us = null;
    int cantidadMinimaTareas = 100;

    for (Usuario integrante : integrantes) {
      boolean yaEsResponsable = integrante.equals(tarea.getUsuarioAsignado());
      boolean puedeTomarla = !yaEsResponsable || integrantes.size() == 1;
      int cantidadTareas = contarTareasAsignadas(integrante);

      if (puedeTomarla && cantidadTareas < cantidadMinimaTareas) {
        cantidadMinimaTareas = cantidadTareas;
        us = integrante;
      }
    }
    return us;
  }

  private int contarTareasAsignadas(Usuario usuario) {
    int cantidad = 0;
    for (TareaRepetitiva tarea : tareas) {
      if (usuario.equals(tarea.getUsuarioAsignado())) {
        cantidad++;
      }
    }
    return cantidad;
  }
}
