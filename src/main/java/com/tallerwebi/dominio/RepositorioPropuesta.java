package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioPropuesta {
  Propuesta guardar(Propuesta propuesta);
  Propuesta obtenerPorId(Long id);
  List<Propuesta> obtenerTodasLasPropuestas();
  List<Propuesta> obtenerPropuestasPorHogar(Long hogarId);
  List<Propuesta> obtenerPropuestasPorUsuario(Long idUsuario);
  long obtenerTotalIntegrantes(Long hogarId);
}
