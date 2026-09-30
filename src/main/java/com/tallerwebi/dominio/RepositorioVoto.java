package com.tallerwebi.dominio.repository;

import com.tallerwebi.dominio.Voto;
import java.util.List;

public interface RepositorioVoto {
  Voto guardar(Voto voto);
  Voto obtenerPorId(Long id);
  List<Voto> obtenerVotosPorPropuesta(Long idPropuesta);
  List<Voto> obtenerTodosLosVotos();
  List<Voto> obtenerVotosPorUsuario(Long idUsuario);
  boolean existeVoto(Long idUsuario, Long idPropuesta);
  Long obtenerVotosAfirmativosPorPropuesta(Long idPropuesta);
  Long obtenerVotosNegativosPorPropuesta(Long idPropuesta);
}
