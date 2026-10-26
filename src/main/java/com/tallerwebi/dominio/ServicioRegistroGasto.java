package com.tallerwebi.dominio;

import java.time.LocalDateTime;
import java.util.List;

public interface ServicioRegistroGasto {
  Gasto registrarGasto(Gasto gasto, Long hogarId);
  List<Gasto> obtenerTodosLosGastos(Long hogarId);
  List<Gasto> obtenerGastosPorTipo(TipoGasto tipo, Long hogarId);
  List<Usuario> obtenerIntegrantes(Long hogarId);
  String obtenerSugerenciaProximoPagador(Long hogarId);
  void marcarComoPagado(Long gastoId, Long usuarioId);
  List<Gasto> obtenerGastosFijosRecurrentes(Long hogarId);
  void actualizarGastoFijo(Long id, Double nuevoMonto, LocalDateTime nuevaFechaVencimiento);
  void eliminarGasto(Long id);

  List<Gasto> obtenerVencimientosPendientes(Long hogarId);
  List<Gasto> obtenerVencimientosProximos(LocalDateTime hoy, Long hogarId);
  List<Gasto> obtenerVencimientosVencidos(LocalDateTime hoy, Long hogarId);
}
