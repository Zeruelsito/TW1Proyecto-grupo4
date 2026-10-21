package com.tallerwebi.dominio;

import java.time.LocalDateTime;
import java.util.List;

public interface ServicioRegistroGasto {
  Gasto registrarGasto(Gasto gasto);
  List<Gasto> obtenerTodosLosGastos();
  List<Gasto> obtenerGastosPorTipo(TipoGasto tipo);
  List<Usuario> obtenerIntegrantes();
  String obtenerSugerenciaProximoPagador();
  void marcarComoPagado(Long gastoId, Long usuarioId);
  List<Gasto> obtenerGastosFijosRecurrentes();
  void actualizarGastoFijo(Long id, Double nuevoMonto, LocalDateTime nuevaFechaVencimiento);
  void eliminarGasto(Long id);
}
