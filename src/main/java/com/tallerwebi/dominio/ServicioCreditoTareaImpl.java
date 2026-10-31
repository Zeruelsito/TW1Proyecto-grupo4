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
  private final RepositorioTareaRepetitiva repositorioTareaRepetitiva;

  @Autowired
  public ServicioCreditoTareaImpl(
    RepositorioTarea repositorioTarea,
    RepositorioUsuario repositorioUsuario,
    RepositorioTareaRepetitiva repositorioTareaRepetitiva
  ) {
    this.repositorioTarea = repositorioTarea;
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioTareaRepetitiva = repositorioTareaRepetitiva;
  }

  @Override
  public void registrarTarea(Tarea tarea) {
    tarea.setEstado(EstadoTarea.PENDIENTE);
    if (tarea.getFechaRegistro() == null) {
      tarea.setFechaRegistro(LocalDateTime.now());
    }
    repositorioTarea.guardar(tarea);
  }

  @Override
  public void registrarTareaDesdeCatalogo(
    Long tareaRepetitivaId,
    Double horasDedicadasIgnoradas,
    Long hogarId
  ) {
    TareaRepetitiva plantilla = buscarPlantillaDelHogar(tareaRepetitivaId, hogarId);

    Tarea tarea = new Tarea(plantilla.getTitulo());
    tarea.setTareaRepetitiva(plantilla);
    registrarTarea(tarea);
  }

  @Override
  public void completarTareaYGenerarCredito(
    Long tareaId,
    Long usuarioId,
    Double horasDedicadas,
    Double tarifaPorHora
  ) {
    Tarea tarea = obtenerTareaValidadaParaCompletar(tareaId, horasDedicadas);
    Usuario usuario = obtenerUsuarioValidado(usuarioId);

    double tarifaFinal = determinarTarifaFinal(tarea, tarifaPorHora);

    tarea.setHorasDedicadas(horasDedicadas, tarifaFinal);
    tarea.setEstado(EstadoTarea.COMPLETADA);
    tarea.setUsuarioRealizador(usuario);

    if (tarea.getTareaRepetitiva() != null) {
      tarea.getTareaRepetitiva().setUsuarioAsignado(usuario);
    }

    tarea.setFechaRegistro(LocalDateTime.now());
    repositorioTarea.guardar(tarea);
  }

  @Override
  public Double obtenerCreditoAcumuladoPorUsuario(Long usuarioId) {
    Usuario usuario = repositorioUsuario.buscarPorId(usuarioId);
    if (usuario == null) {
      return 0.0;
    }

    // Suma el valor en pesos de todas las tareas completadas
    double totalGenerado = repositorioTarea
      .obtenerPorUsuario(usuario)
      .stream()
      .filter(t -> EstadoTarea.COMPLETADA.equals(t.getEstado()))
      .mapToDouble(t -> t.getValorCredito() != null ? t.getValorCredito() : 0.0)
      .sum();

    // Resta el dinero que el usuario ya decidió descontar en la liquidación este mes
    double totalLiquidado = usuario.getCreditoLiquidado() != null
      ? usuario.getCreditoLiquidado()
      : 0.0;
    return Math.max(0.0, totalGenerado - totalLiquidado);
  }

  @Override
  public Double obtenerCreditoLiquidadoPorUsuario(Long usuarioId) {
    Usuario usuario = repositorioUsuario.buscarPorId(usuarioId);
    if (usuario == null) {
      return 0.0;
    }
    // Devuelve el total de plata que ya se reclamó/liquidó este mes para la tira informativa
    return usuario.getCreditoLiquidado() != null ? usuario.getCreditoLiquidado() : 0.0;
  }

  @Override
  public void liquidarCreditosParciales(Long usuarioId, double montoMaximo) {
    if (montoMaximo <= 0) {
      return;
    }
    Usuario usuario = repositorioUsuario.buscarPorId(usuarioId);
    if (usuario == null) {
      return;
    }

    // Incrementa el acumulado de plata liquidada del usuario sin tocar las tareas del historial
    double actualLiquidado = usuario.getCreditoLiquidado() != null
      ? usuario.getCreditoLiquidado()
      : 0.0;
    usuario.setCreditoLiquidado(actualLiquidado + montoMaximo);
    repositorioUsuario.guardar(usuario);
  }

  @Override
  public void liquidarCreditosDeUsuario(Long usuarioId) {
    double creditoAcumulado = obtenerCreditoAcumuladoPorUsuario(usuarioId);
    if (creditoAcumulado > 0) {
      liquidarCreditosParciales(usuarioId, creditoAcumulado);
    }
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

  private void validarEstadoPendiente(Tarea tarea) {
    if (EstadoTarea.COMPLETADA.equals(tarea.getEstado())) {
      throw new TareaYaCompletadaException();
    }
  }

  private TareaRepetitiva buscarPlantillaDelHogar(Long tareaRepetitivaId, Long hogarId) {
    TareaRepetitiva plantilla = null;
    if (tareaRepetitivaId != null) {
      plantilla = repositorioTareaRepetitiva.obtenerPorId(tareaRepetitivaId);
    }
    boolean esDelHogar =
      plantilla != null &&
      plantilla.getHogar() != null &&
      plantilla.getHogar().getId().equals(hogarId);
    if (!esDelHogar) {
      throw new TareaNoEncontradaException();
    }
    return plantilla;
  }

  private Usuario obtenerUsuarioValidado(Long usuarioId) {
    Usuario usuario = repositorioUsuario.buscarPorId(usuarioId);
    if (usuario == null) {
      throw new UsuarioNoEncontrado();
    }
    return usuario;
  }

  private double determinarTarifaFinal(Tarea tarea, Double tarifaPorHora) {
    if (
      tarea.getTareaRepetitiva() != null && tarea.getTareaRepetitiva().obtenerValorPorHora() != null
    ) {
      return tarea.getTareaRepetitiva().obtenerValorPorHora();
    }
    return (tarifaPorHora != null && tarifaPorHora > 0) ? tarifaPorHora : 1000.0;
  }

  private Tarea obtenerTareaValidadaParaCompletar(Long tareaId, Double horasDedicadas) {
    if (tareaId == null) {
      throw new TareaNoEncontradaException();
    }
    Tarea tarea = repositorioTarea.buscarPorId(tareaId);
    if (tarea == null) {
      throw new TareaNoEncontradaException();
    }
    validarEstadoPendiente(tarea);
    if (horasDedicadas == null || horasDedicadas <= 0) {
      throw new ValorCreditoInvalidoException();
    }
    return tarea;
  }
}
