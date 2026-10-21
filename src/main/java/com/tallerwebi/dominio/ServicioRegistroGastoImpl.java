package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DescripcionGastoVaciaException;
import com.tallerwebi.dominio.excepcion.MontoGastoInvalidoException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ServicioRegistroGastoImpl implements ServicioRegistroGasto {

  private final RepositorioGasto repositorioGasto;
  private final RepositorioUsuario repositorioUsuario;

  @Autowired
  public ServicioRegistroGastoImpl(
    RepositorioGasto repositorioGasto,
    RepositorioUsuario repositorioUsuario
  ) {
    this.repositorioGasto = repositorioGasto;
    this.repositorioUsuario = repositorioUsuario;
  }

  @Override
  public Gasto registrarGasto(Gasto gasto) {
    if (gasto.getDescripcion() == null || gasto.getDescripcion().trim().isEmpty()) {
      throw new DescripcionGastoVaciaException();
    }

    if (gasto.getMonto() == null || gasto.getMonto() <= 0) {
      throw new MontoGastoInvalidoException();
    }

    if (gasto.getFecha() == null) {
      gasto.setFecha(LocalDateTime.now());
    }

    repositorioGasto.guardar(gasto);
    return gasto;
  }

  @Override
  public List<Gasto> obtenerTodosLosGastos() {
    return repositorioGasto.obtenerTodos();
  }

  @Override
  public List<Usuario> obtenerIntegrantes() {
    return repositorioUsuario.obtenerIntegrantesDelHogar();
  }

  @Override
  public String obtenerSugerenciaProximoPagador() {
    List<Usuario> integrantes = obtenerIntegrantes();
    List<Gasto> gastos = obtenerTodosLosGastos();

    if (integrantes.isEmpty()) {
      return "@Cualquiera";
    }

    if (gastos.isEmpty()) {
      return obtenerNombreFormateado(integrantes.get(0).getEmail());
    }

    Map<Long, Double> totalPorUsuario = calcularTotalesPorUsuario(integrantes, gastos);
    Long idProximoPagador = obtenerIdConMenorMonto(integrantes, totalPorUsuario);

    return buscarNombrePorId(integrantes, idProximoPagador);
  }

  @Override
  public void marcarComoPagado(Long gastoId, Long usuarioId) {
    Gasto gasto = repositorioGasto.buscarPorId(gastoId);
    if (gasto != null) {
      gasto.setPagado(true);
      Usuario pagador = repositorioUsuario.buscarPorId(usuarioId);
      gasto.setUsuarioPagador(pagador);
      repositorioGasto.modificar(gasto);
    }
  }

  @Override
  public List<Gasto> obtenerGastosFijosRecurrentes() {
    return repositorioGasto.obtenerGastosFijosRecurrentes();
  }

  @Override
  public void actualizarGastoFijo(Long id, Double nuevoMonto, LocalDateTime nuevaFechaVencimiento) {
    Gasto gasto = repositorioGasto.buscarPorId(id);
    if (gasto != null) {
      if (nuevoMonto != null && nuevoMonto > 0) {
        gasto.setMonto(nuevoMonto);
      }
      if (nuevaFechaVencimiento != null) {
        gasto.setFechaVencimiento(nuevaFechaVencimiento);
        // Si actualizamos la fecha al nuevo mes, podemos volver a ponerlo como pendiente de pago
        gasto.setPagado(false);
        gasto.setUsuarioPagador(null);
      }
      repositorioGasto.modificar(gasto);
    }
  }

  @Override
  public void eliminarGasto(Long id) {
    repositorioGasto.eliminar(id);
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
    if (gasto.getUsuarioPagador() != null) {
      Long usuarioId = gasto.getUsuarioPagador().getId();
      if (totales.containsKey(usuarioId)) {
        totales.put(usuarioId, totales.get(usuarioId) + gasto.getMonto());
      }
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
    return repositorioGasto.obtenerPorTipo(tipo);
  }
}
