package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioRegistroGasto {
  Gasto registrarGasto(Gasto gasto);
  List<Gasto> obtenerTodosLosGastos();
  List<Usuario> obtenerIntegrantes();
  String obtenerSugerenciaProximoPagador();
}
