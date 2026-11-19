package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.TareaNoEncontradaException;
import com.tallerwebi.dominio.excepcion.TareaYaCompletadaException;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.excepcion.ValorCreditoInvalidoException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ServicioCreditoTareaImpl implements ServicioCreditoTarea {

  private final RepositorioTarea repositorioTarea;
  private final RepositorioUsuario repositorioUsuario;
  private final RepositorioTareaRepetitiva repositorioTareaRepetitiva;
  private final ServicioCalculosGasto servicioCalculosGasto;
  private final ServicioLiquidacion servicioLiquidacion;
  private final ServicioRegistroGasto servicioRegistroGasto;

  @Autowired
  public ServicioCreditoTareaImpl(
    RepositorioTarea repositorioTarea,
    RepositorioUsuario repositorioUsuario,
    RepositorioTareaRepetitiva repositorioTareaRepetitiva,
    ServicioCalculosGasto servicioCalculosGasto,
    @Lazy ServicioLiquidacion servicioLiquidacion,
    ServicioRegistroGasto servicioRegistroGasto
  ) {
    this.repositorioTarea = repositorioTarea;
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioTareaRepetitiva = repositorioTareaRepetitiva;
    this.servicioCalculosGasto = servicioCalculosGasto;
    this.servicioLiquidacion = servicioLiquidacion;
    this.servicioRegistroGasto = servicioRegistroGasto;
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
    tarea.setHogar(plantilla.getHogar());
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

    double totalGenerado = repositorioTarea
      .obtenerPorUsuario(usuario)
      .stream()
      .filter(t -> EstadoTarea.COMPLETADA.equals(t.getEstado()))
      .mapToDouble(t -> t.getValorCredito() != null ? t.getValorCredito() : 0.0)
      .sum();

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

  @Override
  public List<Tarea> obtenerTareasPorHogar(Long hogarId) {
    return repositorioTarea.obtenerPorHogar(hogarId);
  }

  @Override
  public Map<String, Double> calcularMetricasBrechaUsuario(
    Usuario usuario,
    Hogar hogar,
    List<Gasto> gastos,
    Double precioHora
  ) {
    Map<String, Double> resultado = new HashMap<>();
    double dineroRequeridoBrecha = 0.0;
    double horasRequeridasBrecha = 0.0;

    List<Usuario> integrantes = obtenerIntegrantesHogares(hogar);
    Double tarifaHogar = (hogar != null && hogar.getPrecioHoraTarea() != null)
      ? hogar.getPrecioHoraTarea()
      : precioHora;

    if (
      usuario != null &&
      !integrantes.isEmpty() &&
      servicioCalculosGasto != null &&
      tarifaHogar != null &&
      tarifaHogar > 0
    ) {
      double totalOcasionales = calcularTotalGastosOcasionalesCompartidos(gastos, integrantes);
      double pisoMinimo = obtenerPisoMinimoHogar(hogar);
      Double valorADeber = servicioCalculosGasto.calcularValorDeHorasDeTareaPorUsuario(
        usuario,
        tarifaHogar,
        totalOcasionales,
        pisoMinimo,
        integrantes
      );
      dineroRequeridoBrecha = valorADeber != null ? Math.max(0.0, valorADeber) : 0.0;
      horasRequeridasBrecha = Math.ceil((dineroRequeridoBrecha / tarifaHogar) * 2.0) / 2.0;
    }

    resultado.put("dineroRequeridoBrecha", dineroRequeridoBrecha);
    resultado.put("horasRequeridasBrecha", horasRequeridasBrecha);
    return resultado;
  }

  private List<Usuario> obtenerIntegrantesHogares(Hogar hogar) {
    return (hogar != null && hogar.getIntegrantes() != null)
      ? hogar.getIntegrantes()
      : Collections.emptyList();
  }

  private double calcularTotalGastosOcasionalesCompartidos(
    List<Gasto> gastos,
    List<Usuario> integrantes
  ) {
    if (gastos == null) {
      return 0.0;
    }
    return gastos
      .stream()
      .filter(g -> g != null && g.getMonto() != null)
      .filter(g -> g.getTipoGasto() == TipoGasto.OCASIONAL)
      .filter(g -> esCompartidoPorTodos(g, integrantes))
      .mapToDouble(Gasto::getMonto)
      .sum();
  }

  private boolean esCompartidoPorTodos(Gasto gasto, List<Usuario> integrantes) {
    List<Usuario> participantes = gasto.getParticipantes();
    if (participantes == null || participantes.isEmpty()) {
      return true;
    }
    return participantes.size() >= integrantes.size();
  }

  private double obtenerPisoMinimoHogar(Hogar hogar) {
    if (hogar == null) {
      return 0.0;
    }
    Double pisoConfigurado = hogar.getPisoMinimoHogar();
    if (pisoConfigurado != null && pisoConfigurado > 0) {
      return pisoConfigurado;
    }
    return sumarGastosNoOcasionales(hogar.getId());
  }

  private double sumarGastosNoOcasionales(Long hogarId) {
    if (hogarId == null || servicioRegistroGasto == null) {
      return 0.0;
    }
    List<Gasto> gastosFijos = servicioRegistroGasto.obtenerGastosFijosRecurrentes(hogarId);
    if (gastosFijos == null) {
      return 0.0;
    }
    double total = 0.0;
    for (Gasto gasto : gastosFijos) {
      if (
        gasto != null && gasto.getTipoGasto() != TipoGasto.OCASIONAL && gasto.getMonto() != null
      ) {
        total += gasto.getMonto();
      }
    }
    return total;
  }

  @Override
  public void procesarLiquidacionCreditos(
    Long usuarioId,
    Long hogarId,
    List<Gasto> gastos,
    Hogar hogar
  ) {
    Usuario usuario = repositorioUsuario.buscarPorId(usuarioId);
    if (
      usuario == null ||
      hogar == null ||
      hogar.getPrecioHoraTarea() == null ||
      hogar.getPrecioHoraTarea() <= 0
    ) {
      return;
    }

    double deuda = servicioLiquidacion.obtenerDeudaUsuario(usuario, hogar, gastos);
    Map<String, Double> metricasBrecha = calcularMetricasBrechaUsuario(
      usuario,
      hogar,
      gastos,
      hogar.getPrecioHoraTarea()
    );
    double dineroRequeridoBrecha = metricasBrecha.get("dineroRequeridoBrecha");

    double creditoTotalBruto = obtenerCreditoAcumuladoPorUsuario(usuarioId);
    double creditoLiquidadoMes = obtenerCreditoLiquidadoPorUsuario(usuarioId);

    double creditoLibreExcedente = Math.max(0.0, creditoTotalBruto - dineroRequeridoBrecha);
    double creditoDisponibleParaLiquidar = Math.max(
      0.0,
      creditoLibreExcedente - creditoLiquidadoMes
    );
    double montoALiquidar = Math.min(creditoDisponibleParaLiquidar, deuda);

    if (montoALiquidar > 0) {
      liquidarCreditosParciales(usuarioId, montoALiquidar);
    }
  }

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
