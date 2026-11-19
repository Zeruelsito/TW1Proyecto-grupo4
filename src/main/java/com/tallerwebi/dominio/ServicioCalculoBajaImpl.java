package com.tallerwebi.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioCalculoBajaImpl implements ServicioCalculoBaja {

  private static final double MARGEN_SALDO = 0.01;
  private static final double TARIFA_HORA_DEFAULT = 1000.0;

  private final RepositorioUsuario repositorioUsuario;
  private final ServicioLiquidacion servicioLiquidacion;
  private final ServicioCreditoTarea servicioCreditoTarea;
  private final ServicioPagoRealizado servicioPagoRealizado;
  private final CalculadorCuotasBaja calculadorCuotasBaja;

  @Autowired
  public ServicioCalculoBajaImpl(
    RepositorioUsuario repositorioUsuario,
    ServicioLiquidacion servicioLiquidacion,
    ServicioCreditoTarea servicioCreditoTarea,
    ServicioPagoRealizado servicioPagoRealizado,
    CalculadorCuotasBaja calculadorCuotasBaja
  ) {
    this.repositorioUsuario = repositorioUsuario;
    this.servicioLiquidacion = servicioLiquidacion;
    this.servicioCreditoTarea = servicioCreditoTarea;
    this.servicioPagoRealizado = servicioPagoRealizado;
    this.calculadorCuotasBaja = calculadorCuotasBaja;
  }

  @Override
  public double[] calcularDeudaHorasTarea(Usuario saliente, Hogar hogar, List<Gasto> gastos) {
    if (hogar == null || saliente == null || servicioCreditoTarea == null) {
      return new double[] { 0.0, 0.0 };
    }
    double precioHora = obtenerPrecioHora(hogar);
    asegurarIntegrantesEnHogar(hogar);
    Map<String, Double> metricas = servicioCreditoTarea.calcularMetricasBrechaUsuario(
      saliente,
      hogar,
      gastos,
      precioHora
    );
    double dineroRequerido = metricas == null
      ? 0.0
      : metricas.getOrDefault("dineroRequeridoBrecha", 0.0);
    double deudaPesos = Math.max(0.0, dineroRequerido - obtenerCreditoAcumulado(saliente));
    double horasFaltantes = precioHora > 0 ? Math.ceil((deudaPesos / precioHora) * 2.0) / 2.0 : 0.0;
    return new double[] { deudaPesos, horasFaltantes };
  }

  @Override
  public Map<String, Double> calcularBalancesConBaja(
    List<Usuario> integrantes,
    List<Usuario> restantes,
    Usuario saliente,
    List<Gasto> gastos,
    double deudaHorasPesos,
    Long hogarId
  ) {
    Map<String, Double> balances = calcularBalancesBase(integrantes, gastos);
    distribuirDeudaHoras(balances, saliente, restantes, deudaHorasPesos);
    aplicarPagosRealizados(balances, hogarId);
    return balances;
  }

  @Override
  public List<TransferenciaSugerida> liquidacionDelSaliente(
    String nombreSaliente,
    Map<String, Double> balances
  ) {
    List<TransferenciaSugerida> todas = servicioLiquidacion.calcularLiquidacionOptima(balances);
    Map<String, Long> idsPorNombre = mapearIdsPorNombre();
    List<TransferenciaSugerida> resultado = new ArrayList<>();
    for (TransferenciaSugerida transferencia : todas) {
      if (involucraSaliente(transferencia, nombreSaliente)) {
        resultado.add(completarIdsTransferencia(transferencia, idsPorNombre));
      }
    }
    return resultado;
  }

  @Override
  public double totalGastosFijosDelMes(List<Gasto> gastos) {
    return calculadorCuotasBaja.totalGastosFijosDelMes(gastos);
  }

  @Override
  public boolean parametrosValidos(Hogar hogar) {
    return calculadorCuotasBaja.parametrosValidos(hogar);
  }

  @Override
  public List<FilaCuota> armarFilasCuota(
    List<Usuario> integrantes,
    List<Usuario> restantes,
    double totalFijos,
    Hogar hogar,
    boolean cuotasCalculables
  ) {
    return calculadorCuotasBaja.armarFilasCuota(
      integrantes,
      restantes,
      totalFijos,
      hogar,
      cuotasCalculables
    );
  }

  @Override
  public void recalcularYGuardarCuotasRestantes(List<Usuario> restantes, Hogar hogar) {
    calculadorCuotasBaja.recalcularYGuardarCuotasRestantes(restantes, hogar);
  }

  private boolean involucraSaliente(TransferenciaSugerida transferencia, String nombreSaliente) {
    return (
      nombreSaliente.equals(transferencia.getEmisor()) ||
      nombreSaliente.equals(transferencia.getReceptor())
    );
  }

  private double obtenerPrecioHora(Hogar hogar) {
    Double precio = hogar.getPrecioHoraTarea();
    return (precio != null && precio > 0) ? precio : TARIFA_HORA_DEFAULT;
  }

  private void asegurarIntegrantesEnHogar(Hogar hogar) {
    if (
      hogar.getId() != null && (hogar.getIntegrantes() == null || hogar.getIntegrantes().isEmpty())
    ) {
      hogar.setIntegrantes(repositorioUsuario.obtenerIntegrantesDelHogar(hogar.getId()));
    }
  }

  private double obtenerCreditoAcumulado(Usuario usuario) {
    if (usuario.getId() == null || servicioCreditoTarea == null) {
      return 0.0;
    }
    Double credito = servicioCreditoTarea.obtenerCreditoAcumuladoPorUsuario(usuario.getId());
    return credito == null ? 0.0 : credito;
  }

  private double obtenerCreditoLiquidado(Usuario usuario) {
    if (usuario.getId() == null || servicioCreditoTarea == null) {
      return 0.0;
    }
    Double liquidado = servicioCreditoTarea.obtenerCreditoLiquidadoPorUsuario(usuario.getId());
    return liquidado == null ? 0.0 : liquidado;
  }

  private Map<String, Double> calcularBalancesBase(List<Usuario> integrantes, List<Gasto> gastos) {
    Map<Long, Double> consumo = new HashMap<>();
    Map<Long, Double> pagado = new HashMap<>();
    for (Usuario usuario : integrantes) {
      consumo.put(usuario.getId(), 0.0);
      pagado.put(usuario.getId(), 0.0);
    }
    for (Gasto gasto : gastos) {
      repartirGasto(gasto, integrantes, consumo, pagado);
    }
    Map<String, Double> balances = new LinkedHashMap<>();
    for (Usuario usuario : integrantes) {
      Long id = usuario.getId();
      double balanceNeto = (pagado.get(id) + obtenerCreditoLiquidado(usuario)) - consumo.get(id);
      balances.put(nombreDe(usuario), balanceNeto);
    }
    return balances;
  }

  private void repartirGasto(
    Gasto gasto,
    List<Usuario> integrantes,
    Map<Long, Double> consumo,
    Map<Long, Double> pagado
  ) {
    if (gasto.getMonto() == null) {
      return;
    }
    if (Boolean.TRUE.equals(gasto.getPagado())) {
      List<Usuario> participantes = participantesDe(gasto, integrantes);
      double parte = gasto.getMonto() / participantes.size();
      for (Usuario usuario : participantes) {
        consumo.merge(usuario.getId(), parte, Double::sum);
      }
    }
    Usuario pagador = gasto.getUsuarioPagador();
    if (pagador != null && pagado.containsKey(pagador.getId())) {
      pagado.merge(pagador.getId(), gasto.getMonto(), Double::sum);
    }
  }

  private List<Usuario> participantesDe(Gasto gasto, List<Usuario> integrantes) {
    List<Usuario> elegidos = gasto.getParticipantes();
    return (elegidos != null && !elegidos.isEmpty()) ? elegidos : integrantes;
  }

  private void distribuirDeudaHoras(
    Map<String, Double> balances,
    Usuario saliente,
    List<Usuario> restantes,
    double deudaHorasPesos
  ) {
    if (deudaHorasPesos <= MARGEN_SALDO || restantes.isEmpty()) {
      return;
    }
    balances.computeIfPresent(nombreDe(saliente), (k, v) -> v - deudaHorasPesos);
    double partePorRestante = deudaHorasPesos / restantes.size();
    for (Usuario r : restantes) {
      balances.computeIfPresent(nombreDe(r), (k, v) -> v + partePorRestante);
    }
  }

  private void aplicarPagosRealizados(Map<String, Double> balances, Long hogarId) {
    for (PagoRealizado pago : servicioPagoRealizado.obtenerHistorialPagos(hogarId)) {
      if (pago.getMonto() != null) {
        balances.computeIfPresent(
          pago.getEmisor().getNombre(),
          (nombre, saldo) -> saldo + pago.getMonto()
        );
        balances.computeIfPresent(
          pago.getReceptor().getNombre(),
          (nombre, saldo) -> saldo - pago.getMonto()
        );
      }
    }
  }

  private Map<String, Long> mapearIdsPorNombre() {
    Map<String, Long> mapa = new HashMap<>();
    for (Usuario u : repositorioUsuario.obtenerTodos()) {
      if (u.getNombre() != null) {
        mapa.put(u.getNombre(), u.getId());
      }
    }
    return mapa;
  }

  private TransferenciaSugerida completarIdsTransferencia(
    TransferenciaSugerida transferencia,
    Map<String, Long> idsPorNombre
  ) {
    Long emisorId = resolverId(
      transferencia.getEmisorId(),
      idsPorNombre.get(transferencia.getEmisor())
    );
    Long receptorId = resolverId(
      transferencia.getReceptorId(),
      idsPorNombre.get(transferencia.getReceptor())
    );
    return new TransferenciaSugerida(
      emisorId,
      transferencia.getEmisor(),
      transferencia.getEmailEmisor(),
      receptorId,
      transferencia.getReceptor(),
      redondear(transferencia.getMonto())
    );
  }

  private Long resolverId(Long actual, Long desdeMapa) {
    return actual == null ? desdeMapa : actual;
  }

  private String nombreDe(Usuario usuario) {
    return usuario.getNombre() != null ? usuario.getNombre() : "Usuario " + usuario.getId();
  }

  private double redondear(double valor) {
    return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }
}
