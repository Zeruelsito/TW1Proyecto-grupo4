package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.VencimientoInvalidoException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioVencimientoImpl implements ServicioVencimiento {

  private final RepositorioVencimiento repositorioVencimiento;

  @Autowired
  public ServicioVencimientoImpl(RepositorioVencimiento repositorioVencimiento) {
    this.repositorioVencimiento = repositorioVencimiento;
  }

  @Override
  public void registrar(Vencimiento vencimiento) {
    if (
      vencimiento.getFechaVencimiento() == null ||
      vencimiento.getTitulo() == null ||
      vencimiento.getTitulo().trim().isEmpty()
    ) {
      throw new VencimientoInvalidoException();
    }
    repositorioVencimiento.guardar(vencimiento);
  }

  @Override
  public List<Vencimiento> obtenerOrdenadosPorFecha() {
    return repositorioVencimiento.obtenerTodosOrdenadosPorFecha();
  }

  @Override
  public List<Vencimiento> obtenerProximos(LocalDate hoy) {
    return repositorioVencimiento.obtenerDesde(hoy);
  }
}
