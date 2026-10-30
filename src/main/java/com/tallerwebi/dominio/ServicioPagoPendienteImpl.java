package com.tallerwebi.dominio;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioPagoPendienteImpl implements ServicioPagoPendiente {

  private final RepositorioPagoPendiente repositorioPagoPendiente;

  @Autowired
  public ServicioPagoPendienteImpl(RepositorioPagoPendiente repositorioPagoPendiente) {
    this.repositorioPagoPendiente = repositorioPagoPendiente;
  }

  @Override
  public boolean existePendiente(String emisor, String receptor, Double monto, Hogar hogar) {
    return repositorioPagoPendiente.buscarPorDatos(emisor, receptor, monto, hogar) != null;
  }

  @Override
  public void eliminarPorId(Long id) {
    repositorioPagoPendiente.eliminarPorId(id);
  }

  @Override
  public PagoPendiente buscarPorId(Long id) {
    return repositorioPagoPendiente.buscarPorId(id);
  }

  @Override
  public void guardarPendiente(PagoPendiente pagoPendiente) {
    repositorioPagoPendiente.guardar(pagoPendiente);
  }

  @Override
  public List<PagoPendiente> obtenerPendientes(Hogar hogar) {
    return repositorioPagoPendiente.obtenerPendientesPorHogar(hogar);
  }

  @Override
  public void eliminarPendiente(String emisor, String receptor, Double monto, Hogar hogar) {
    PagoPendiente pendiente = repositorioPagoPendiente.buscarPorDatos(
      emisor,
      receptor,
      monto,
      hogar
    );
    if (pendiente != null) {
      repositorioPagoPendiente.eliminar(pendiente);
    }
  }
}
