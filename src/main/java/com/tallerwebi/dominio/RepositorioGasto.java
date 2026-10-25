package com.tallerwebi.dominio;

import java.time.LocalDateTime;
import java.util.List;

public interface RepositorioGasto {
  void guardar(Gasto gasto);
  Gasto buscarPorCodigo(String codigo);
  Gasto buscarPorId(Long id);
  Double obtenerMontoPorCodigo(String codigo);
  String obtenerNombreDeUsuarioPagadorPorCodigoGasto(String codigo);

  //filtrados por hogar
  List<Gasto> obtenerTodosPorHogar(Long hogarId);
  List<Gasto> obtenerPorTipo(TipoGasto tipo, Long hogarId);
  List<Gasto> obtenerGastosPendientesConVencimiento(Long hogarId);
  List<Gasto> obtenerGastosFijosRecurrentes(Long hogarId);

  void eliminar(Long id);
  void modificar(Gasto gasto);

  List<Gasto> obtenerPendientesConVencimientoEntre(
    LocalDateTime desde,
    LocalDateTime hasta,
    Long hogarId
  );
  List<Gasto> obtenerPendientesVencidosAntesDe(LocalDateTime ahora, Long hogarId);
}
