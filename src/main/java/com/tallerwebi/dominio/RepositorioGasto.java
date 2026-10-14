package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioGasto {
  void guardar(Gasto gasto);
  Gasto buscarPorCodigo(String codigo);
  List<Gasto> obtenerTodos();
  List<Gasto> obtenerPorTipo(TipoGasto tipo);
  Double obtenerMontoPorCodigo(String codigo);
  String obtenerNombreDeUsuarioPagadorPorCodigoGasto(String codigo);
}
