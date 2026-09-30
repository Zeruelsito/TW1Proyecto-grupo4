package com.tallerwebi.dominio.repository;

import com.tallerwebi.dominio.Propuesta;
import java.util.List;

public interface RepositorioPropuesta {
  Propuesta guardar(Propuesta propuesta);
  Propuesta obtenerPorId(Long id);
  List<Propuesta> obtenerTodasLasPropuestas();
  List<Propuesta> obtenerPropuestasPorUsuario(Long idUsuario);
  long obtenerTotalIntegrantes();
}
