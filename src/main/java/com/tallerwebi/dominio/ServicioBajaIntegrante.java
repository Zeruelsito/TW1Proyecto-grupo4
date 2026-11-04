package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioBajaIntegrante {
  List<Usuario> obtenerIntegrantesActivos();

  SimulacionBaja simularBaja(Long usuarioId);

  void confirmarBaja(Long usuarioId);

  void registrarPago(Long usuarioId, Usuario emisor, Usuario receptor, Double monto);
}
