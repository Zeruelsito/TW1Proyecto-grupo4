package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.HogarNoEncontradoException;
import java.util.List;

public interface ServicioHogar {
  void actualizarParametros(Double precioHoraTarea, Double pisoMinimoHogar);

  Hogar obtenerHogar();
  void registrarHogar(Hogar hogar);
  Hogar seleccionarHogarPorId(Long hogarId) throws HogarNoEncontradoException;

  List<CambioParametro> obtenerHistorial();

  List<Hogar> obtenerHogares(Long usuarioId);
  void crearHogar(Hogar hogar); // <-- Nuevo
  boolean unirseAHogar(String nombreHogar, String pin, Usuario usuario);
}
