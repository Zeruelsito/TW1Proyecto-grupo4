package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.VencimientoInvalidoException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service
public class ServicioVencimientoImpl implements ServicioVencimiento, InitializingBean {

  private final List<Vencimiento> vencimientos = new ArrayList<>();

  @Override
  public void afterPropertiesSet() {
    LocalDate hoy = LocalDate.now();
    registrar(new Vencimiento(1L, "Luz", hoy.plusDays(5)));
    registrar(new Vencimiento(2L, "Internet", hoy.plusDays(9)));
    registrar(new Vencimiento(3L, "Alquiler", hoy.plusDays(12)));
    registrar(new Vencimiento(4L, "Gas", hoy.plusDays(20)));
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
    vencimientos.add(vencimiento);
  }

  @Override
  public List<Vencimiento> obtenerOrdenadosPorFecha() {
    List<Vencimiento> copiaVencimientos = new ArrayList<>(vencimientos);
    copiaVencimientos.sort(Comparator.comparing(Vencimiento::getFechaVencimiento));
    return copiaVencimientos;
  }

  @Override
  public List<Vencimiento> obtenerProximos(LocalDate hoy) {
    List<Vencimiento> proximos = new ArrayList<>();

    for (Vencimiento vencimiento : vencimientos) {
      if (!vencimiento.getFechaVencimiento().isBefore(hoy)) {
        proximos.add(vencimiento);
      }
    }

    proximos.sort(Comparator.comparing(Vencimiento::getFechaVencimiento));
    return proximos;
  }
}
