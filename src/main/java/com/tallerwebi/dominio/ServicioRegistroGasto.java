package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioRegistroGasto {
  Gasto registrarGasto(Gasto gasto);
  List<Gasto> obtenerTodosLosGastos();
  List<Gasto> obtenerGastosPorTipo(TipoGasto tipo);
  List<Usuario> obtenerIntegrantes();
  String obtenerSugerenciaProximoPagador();
}
