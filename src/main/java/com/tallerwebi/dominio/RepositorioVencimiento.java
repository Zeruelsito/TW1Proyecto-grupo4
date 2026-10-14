package com.tallerwebi.dominio;

import java.time.LocalDate;
import java.util.List;

public interface RepositorioVencimiento {
  void guardar(Vencimiento vencimiento);

  List<Vencimiento> obtenerTodosOrdenadosPorFecha();

  List<Vencimiento> obtenerDesde(LocalDate fecha);
}
