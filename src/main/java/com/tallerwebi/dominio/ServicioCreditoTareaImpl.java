package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.TareaNoEncontradaException;
import com.tallerwebi.dominio.excepcion.TareaYaCompletadaException;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.excepcion.ValorCreditoInvalidoException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ServicioCreditoTareaImpl implements ServicioCreditoTarea {

  private final RepositorioTarea repositorioTarea;
  private final RepositorioUsuario repositorioUsuario;

  @Autowired
  public ServicioCreditoTareaImpl(
    RepositorioTarea repositorioTarea,
    RepositorioUsuario repositorioUsuario
  ) {
    this.repositorioTarea = repositorioTarea;
    this.repositorioUsuario = repositorioUsuario;
  }

  @Override
  public void registrarTarea(Tarea tarea) {
    validarHorasDedicadas(tarea.getHorasDedicadas());

    tarea.setEstado(EstadoTarea.PENDIENTE);
    if (tarea.getFechaRegistro() == null) {
      tarea.setFechaRegistro(LocalDateTime.now());
    }

    repositorioTarea.guardar(tarea);
  }

  @Override
  public void completarTareaYGenerarCredito(Long tareaId, Long usuarioId) {
    Tarea tarea = repositorioTarea.buscarPorId(tareaId);
    if (tarea == null) {
      throw new TareaNoEncontradaException();
    }

    validarEstadoPendiente(tarea);

    Usuario usuario = repositorioUsuario.buscarPorId(usuarioId);
    if (usuario == null) {
      throw new UsuarioNoEncontrado();
    }

    tarea.setEstado(EstadoTarea.COMPLETADA);
    tarea.setUsuarioRealizador(usuario); // Seteamos el objeto Usuario directamente
    tarea.setFechaRegistro(LocalDateTime.now());

    repositorioTarea.guardar(tarea);
  }

  @Override
  public Double obtenerCreditoAcumuladoPorUsuario(Long usuarioId) {
    Usuario usuario = repositorioUsuario.buscarPorId(usuarioId);
    if (usuario == null) {
      return 0.0;
    }

    List<Tarea> tareasDelUsuario = repositorioTarea.obtenerPorUsuario(usuario);
    double creditoTotal = 0.0;

    for (Tarea tarea : tareasDelUsuario) {
      if (EstadoTarea.COMPLETADA.equals(tarea.getEstado())) {
        creditoTotal += tarea.getValorCredito() != null ? tarea.getValorCredito() : 0.0;
      }
    }
    return creditoTotal;
  }

  @Override
  public Double calcularMontoNetoAPagar(Double montoBruto, Long usuarioId) {
    Double credito = obtenerCreditoAcumuladoPorUsuario(usuarioId);
    double saldoResultante = montoBruto - credito;
    return Math.max(0.0, saldoResultante);
  }

  @Override
  public List<Tarea> obtenerTodasLasTareas() {
    return repositorioTarea.obtenerTodas();
  }

  /* --- Métodos Auxiliares Privados --- */

  private void validarHorasDedicadas(Double horas) {
    if (horas == null || horas <= 0) {
      throw new ValorCreditoInvalidoException();
    }
  }

  private void validarEstadoPendiente(Tarea tarea) {
    if (EstadoTarea.COMPLETADA.equals(tarea.getEstado())) {
      throw new TareaYaCompletadaException();
    }
  }
}
