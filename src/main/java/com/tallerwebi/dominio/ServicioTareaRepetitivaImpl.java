package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.HogarSinIntegrantesException;
import com.tallerwebi.dominio.excepcion.TareaNoEncontradaException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioTareaRepetitivaImpl implements ServicioTareaRepetitiva {

  private final RepositorioTareaRepetitiva repositorioTareaRepetitiva;
  private final RepositorioHogar repositorioHogar;

  @Autowired
  public ServicioTareaRepetitivaImpl(
    RepositorioTareaRepetitiva repositorioTareaRepetitiva,
    RepositorioHogar repositorioHogar
  ) {
    this.repositorioTareaRepetitiva = repositorioTareaRepetitiva;
    this.repositorioHogar = repositorioHogar;
  }

  @Override
  public void registrar(TareaRepetitiva tarea, Long hogarId) {
    tarea.setHogar(repositorioHogar.buscarPorId(hogarId));
    repositorioTareaRepetitiva.guardar(tarea);
  }

  @Override
  public List<TareaRepetitiva> obtenerTodas(Long hogarId) {
    return repositorioTareaRepetitiva.obtenerPorHogar(hogarId);
  }

  @Override
  public void asignarEquitativamente(Long tareaId, List<Usuario> integrantes, Long hogarId) {
    if (integrantes.isEmpty()) {
      throw new HogarSinIntegrantesException();
    }
    List<TareaRepetitiva> tareasDelHogar = repositorioTareaRepetitiva.obtenerPorHogar(hogarId);
    TareaRepetitiva tarea = buscarPorId(tareaId, tareasDelHogar);
    tarea.setUsuarioAsignado(elegirConMenosTareas(tarea, integrantes, tareasDelHogar));
  }

  private TareaRepetitiva buscarPorId(Long tareaId, List<TareaRepetitiva> tareas) {
    for (TareaRepetitiva tarea : tareas) {
      if (tarea.getId().equals(tareaId)) {
        return tarea;
      }
    }

    throw new TareaNoEncontradaException();
  }

  private Usuario elegirConMenosTareas(
    TareaRepetitiva tarea,
    List<Usuario> integrantes,
    List<TareaRepetitiva> tareas
  ) {
    Usuario us = null;
    int cantidadMinimaTareas = 1000;

    for (Usuario integrante : integrantes) {
      boolean yaEsResponsable = integrante.equals(tarea.getUsuarioAsignado());
      boolean puedeTomarla = !yaEsResponsable || integrantes.size() == 1;
      int cantidadTareas = contarTareasAsignadas(integrante, tareas);

      if (puedeTomarla && cantidadTareas < cantidadMinimaTareas) {
        cantidadMinimaTareas = cantidadTareas;
        us = integrante;
      }
    }
    return us;
  }

  private int contarTareasAsignadas(Usuario usuario, List<TareaRepetitiva> tareas) {
    int cantidad = 0;
    for (TareaRepetitiva tarea : tareas) {
      if (usuario.equals(tarea.getUsuarioAsignado())) {
        cantidad++;
      }
    }
    return cantidad;
  }
}
