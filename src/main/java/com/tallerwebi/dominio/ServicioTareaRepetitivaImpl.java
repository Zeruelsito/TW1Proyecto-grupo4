package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.HogarSinIntegrantesException;
import com.tallerwebi.dominio.excepcion.TareaNoEncontradaException;
import java.util.ArrayList;
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
    List<TareaRepetitiva> delHogar = new ArrayList<>();
    for (TareaRepetitiva tarea : repositorioTareaRepetitiva.obtenerPorHogar(hogarId)) {
      if (tarea.getAlcance() == AlcanceTarea.HOGAR && tarea.isActiva()) {
        delHogar.add(tarea);
      }
    }
    return delHogar;
  }

  @Override
  public void asignarEquitativamente(Long tareaId, List<Usuario> integrantes, Long hogarId) {
    if (integrantes.isEmpty()) {
      throw new HogarSinIntegrantesException();
    }
    List<TareaRepetitiva> tareasDelHogar = obtenerTodas(hogarId);
    TareaRepetitiva tarea = buscarPorId(tareaId, tareasDelHogar);
    tarea.setUsuarioAsignado(elegirConMenosTareas(tarea, integrantes, tareasDelHogar));
  }

  @Override
  public void guardarComoPlantilla(String titulo, Long hogarId) {
    crearPlantilla(titulo, hogarId, AlcanceTarea.HOGAR, null);
  }

  @Override
  public void guardarComoPlantillaPersonal(String titulo, Long hogarId, Usuario propietario) {
    crearPlantilla(titulo, hogarId, AlcanceTarea.PERSONAL, propietario);
  }

  @Override
  public List<TareaRepetitiva> obtenerCatalogo(Long hogarId, Long usuarioId) {
    return repositorioTareaRepetitiva.obtenerPorHogarYUsuario(hogarId, usuarioId);
  }

  @Override
  public void quitarPlantilla(Long plantillaId, Long hogarId, Long usuarioId) {
    TareaRepetitiva plantilla = repositorioTareaRepetitiva.obtenerPorId(plantillaId);
    if (
      plantilla == null ||
      plantilla.getHogar() == null ||
      !plantilla.getHogar().getId().equals(hogarId) ||
      !plantilla.esVisiblePara(usuarioId)
    ) {
      throw new TareaNoEncontradaException();
    }
    plantilla.setActiva(false);
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
      boolean yaEsResponsable = esMismoUsuario(integrante, tarea.getUsuarioAsignado());

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
      if (esMismoUsuario(usuario, tarea.getUsuarioAsignado())) {
        cantidad++;
      }
    }
    return cantidad;
  }

  private void crearPlantilla( // crearPlantillaSiNoExiste
    String titulo,
    Long hogarId,
    AlcanceTarea alcance,
    Usuario propietario
  ) {
    if (titulo == null || titulo.isBlank()) {
      return;
    }
    Long propietarioId = null;
    if (propietario != null) {
      propietarioId = propietario.getId();
    }

    if (
      repositorioTareaRepetitiva.obtenerPorHogarUsuarioYTitulo(hogarId, propietarioId, titulo) !=
      null
    ) {
      return;
    }
    TareaRepetitiva plantilla = new TareaRepetitiva();
    plantilla.setTitulo(titulo);
    plantilla.setAlcance(alcance);
    plantilla.setPropietario(propietario);
    plantilla.setActiva(true);
    registrar(plantilla, hogarId);
  }

  private boolean esMismoUsuario(Usuario unUsuario, Usuario otroUsuario) {
    return (
      unUsuario != null &&
      otroUsuario != null &&
      unUsuario.getId() != null &&
      unUsuario.getId().equals(otroUsuario.getId())
    );
  }
}
