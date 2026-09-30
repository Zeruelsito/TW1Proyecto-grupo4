package com.tallerwebi.dominio;

import java.time.LocalDate;
import java.util.List;

public interface ServicioVencimiento {
  void registrar(Vencimiento vencimiento);

  List<Vencimiento> obtenerOrdenadosPorFecha();
  List<Vencimiento> obtenerProximos(LocalDate hoy);
}
