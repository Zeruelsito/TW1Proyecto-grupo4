package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DescripcionGastoVaciaException;
import com.tallerwebi.dominio.excepcion.MontoGastoInvalidoException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ServicioRegistroGastoImpl implements ServicioRegistroGasto {

  private List<Gasto> gastosRegistrados = new ArrayList<>();

  @Override
  public Gasto registrarGasto(Gasto gasto) {
    if (gasto.getDescripcion() == null || gasto.getDescripcion().trim().isEmpty()) {
      throw new DescripcionGastoVaciaException();
    }

    if (gasto.getMonto() == null || gasto.getMonto() <= 0) {
      throw new MontoGastoInvalidoException();
    }

    this.gastosRegistrados.add(gasto);
    return gasto;
  }

  @Override
  public List<Gasto> obtenerTodosLosGastos() {
    return this.gastosRegistrados;
  }

  @Override
  public List<Usuario> obtenerIntegrantes() {
    List<Usuario> usuarios = new ArrayList<>();

    Usuario u1 = new Usuario();
    u1.setId(1L);
    u1.setEmail("macarena@kumo.com");

    Usuario u2 = new Usuario();
    u2.setId(2L);
    u2.setEmail("ezequiel@kumo.com");

    Usuario u3 = new Usuario();
    u3.setId(3L);
    u3.setEmail("oriana@kumo.com");

    Usuario u4 = new Usuario();
    u4.setId(4L);
    u4.setEmail("dylan@kumo.com");

    usuarios.add(u1);
    usuarios.add(u2);
    usuarios.add(u3);
    usuarios.add(u4);

    return usuarios;
  }

  @Override
  public String obtenerSugerenciaProximoPagador() {
    List<Usuario> integrantes = obtenerIntegrantes();
    List<Gasto> gastos = obtenerTodosLosGastos();

    if (gastos.isEmpty()) {
      return "@Cualquiera";
    }

    Map<Long, Double> totalPorUsuario = calcularTotalesPorUsuario(integrantes, gastos);
    Long idProximoPagador = obtenerIdConMenorMonto(integrantes, totalPorUsuario);

    return buscarNombrePorId(integrantes, idProximoPagador);
  }

  private Map<Long, Double> calcularTotalesPorUsuario(
    List<Usuario> integrantes,
    List<Gasto> gastos
  ) {
    Map<Long, Double> totales = new HashMap<>();
    for (Usuario u : integrantes) {
      totales.put(u.getId(), 0.0);
    }
    for (Gasto g : gastos) {
      acumularMontoEnMap(totales, g);
    }
    return totales;
  }

  private void acumularMontoEnMap(Map<Long, Double> totales, Gasto gasto) {
    if (gasto.getPagadorId() != null && totales.containsKey(gasto.getPagadorId())) {
      totales.put(gasto.getPagadorId(), totales.get(gasto.getPagadorId()) + gasto.getMonto());
    }
  }

  private Long obtenerIdConMenorMonto(List<Usuario> integrantes, Map<Long, Double> totales) {
    Long idMenor = integrantes.get(0).getId();
    double menorMonto = Double.MAX_VALUE;

    for (Map.Entry<Long, Double> entry : totales.entrySet()) {
      if (entry.getValue() < menorMonto) {
        menorMonto = entry.getValue();
        idMenor = entry.getKey();
      }
    }
    return idMenor;
  }

  private String buscarNombrePorId(List<Usuario> integrantes, Long idBuscado) {
    for (Usuario u : integrantes) {
      if (u.getId().equals(idBuscado)) {
        return obtenerNombreFormateado(u.getEmail());
      }
    }
    return obtenerNombreFormateado(integrantes.get(0).getEmail());
  }

  private String obtenerNombreFormateado(String email) {
    if (email != null && email.contains("@")) {
      return email.split("@")[0];
    }
    return email;
  }

  @Override
  public List<Gasto> obtenerGastosPorTipo(TipoGasto tipo) {
    List<Gasto> gastosFiltrados = new ArrayList<>();
    for (Gasto gasto : this.gastosRegistrados) {
      if (gasto.getTipoGasto().equals(tipo)) {
        gastosFiltrados.add(gasto);
      }
    }
    return gastosFiltrados;
  }
}
