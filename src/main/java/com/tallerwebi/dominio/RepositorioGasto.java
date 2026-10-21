package com.tallerwebi.dominio;

import java.time.LocalDateTime;
import java.util.List;

public interface RepositorioGasto {
  void guardar(Gasto gasto);
  Gasto buscarPorCodigo(String codigo);
  Gasto buscarPorId(Long id);
  List<Gasto> obtenerTodos();
  List<Gasto> obtenerPorTipo(TipoGasto tipo);
  Double obtenerMontoPorCodigo(String codigo);
  String obtenerNombreDeUsuarioPagadorPorCodigoGasto(String codigo);
  List<Gasto> obtenerGastosPendientesConVencimiento();
  void modificar(Gasto gasto);
  List<Gasto> obtenerGastosFijosRecurrentes();
  void eliminar(Long id);

List<Gasto> obtenerPendientesConVencimientoEntre(LocalDateTime desde, LocalDateTime hasta);
List<Gasto> obtenerPendientesVencidosAntesDe(LocalDateTime ahora);
}
