package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioHogar {
  void actualizarParametros(Double precioHoraTarea, Double pisoMinimoHogar);

  Hogar obtenerHogar();
  void registrarHogar(Hogar hogar);
  void seleccionarHogar(Long hogarId);

  List<CambioParametro> obtenerHistorial();

  List<Hogar> obtenerHogares();
}
