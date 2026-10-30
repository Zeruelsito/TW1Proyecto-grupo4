package com.tallerwebi.dominio;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioPagoRealizadoImpl implements ServicioPagoRealizado {

  private final RepositorioPagoRealizado repositorioPagoRealizado;

  @Autowired
  public ServicioPagoRealizadoImpl(RepositorioPagoRealizado repositorioPagoRealizado) {
    this.repositorioPagoRealizado = repositorioPagoRealizado;
  }

  @Override
  public void registrarPagoExitoso(PagoRealizado pago) {
    this.repositorioPagoRealizado.guardar(pago);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PagoRealizado> obtenerHistorialPagos(Long hogarId) {
    if (hogarId == null) {
      return Collections.emptyList();
    }
    return repositorioPagoRealizado.obtenerPorHogarId(hogarId);
  }
}
