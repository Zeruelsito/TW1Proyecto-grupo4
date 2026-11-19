package com.tallerwebi.dominio;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SimulacionBaja {

  private static final double MARGEN_MONTO = 0.01;

  private final Usuario usuarioSaliente;
  private final Double balanceFinal;
  private final Double deudaHorasTarea;
  private final Double horasTareaAdeudadas;
  private final List<TransferenciaSugerida> liquidacionFinal;
  private final Set<String> clavesPagosPendientes;
  private final List<FilaCuota> cuotasRestantes;
  private final Double totalGastosFijosMes;
  private final boolean cuotasCalculadas;

  public SimulacionBaja(
    Usuario usuarioSaliente,
    Double balanceFinal,
    List<TransferenciaSugerida> liquidacionFinal,
    List<FilaCuota> cuotasRestantes,
    Double totalGastosFijosMes,
    boolean cuotasCalculadas
  ) {
    this(
      usuarioSaliente,
      balanceFinal,
      0.0,
      0.0,
      liquidacionFinal,
      Collections.emptySet(),
      cuotasRestantes,
      totalGastosFijosMes,
      cuotasCalculadas
    );
  }

  public SimulacionBaja(
    Usuario usuarioSaliente,
    Double balanceFinal,
    Double deudaHorasTarea,
    Double horasTareaAdeudadas,
    List<TransferenciaSugerida> liquidacionFinal,
    Set<String> clavesPagosPendientes,
    List<FilaCuota> cuotasRestantes,
    Double totalGastosFijosMes,
    boolean cuotasCalculadas
  ) {
    this.usuarioSaliente = usuarioSaliente;
    this.balanceFinal = balanceFinal;
    this.deudaHorasTarea = deudaHorasTarea != null ? deudaHorasTarea : 0.0;
    this.horasTareaAdeudadas = horasTareaAdeudadas != null ? horasTareaAdeudadas : 0.0;
    this.liquidacionFinal = liquidacionFinal;
    this.clavesPagosPendientes =
      clavesPagosPendientes != null ? clavesPagosPendientes : Collections.emptySet();
    this.cuotasRestantes = cuotasRestantes;
    this.totalGastosFijosMes = totalGastosFijosMes;
    this.cuotasCalculadas = cuotasCalculadas;
  }

  public Usuario getUsuarioSaliente() {
    return usuarioSaliente;
  }

  public Double getBalanceFinal() {
    return balanceFinal;
  }

  public Double getDeudaHorasTarea() {
    return deudaHorasTarea;
  }

  public Double getHorasTareaAdeudadas() {
    return horasTareaAdeudadas;
  }

  public List<TransferenciaSugerida> getLiquidacionFinal() {
    return liquidacionFinal;
  }

  public Set<String> getClavesPagosPendientes() {
    return clavesPagosPendientes;
  }

  public List<FilaCuota> getCuotasRestantes() {
    return cuotasRestantes;
  }

  public Double getTotalGastosFijosMes() {
    return totalGastosFijosMes;
  }

  /** false cuando el hogar no tiene piso mínimo configurado y no se pueden calcular cuotas. */
  public boolean isCuotasCalculadas() {
    return cuotasCalculadas;
  }

  public boolean isDebePagar() {
    return balanceFinal != null && balanceFinal < -MARGEN_MONTO;
  }

  public boolean isDebeCobrar() {
    return balanceFinal != null && balanceFinal > MARGEN_MONTO;
  }

  public boolean isTieneDeudaHorasTarea() {
    return deudaHorasTarea != null && deudaHorasTarea > MARGEN_MONTO;
  }

  public boolean esTransferenciaPendienteConfirmacion(TransferenciaSugerida transferencia) {
    if (transferencia == null) {
      return false;
    }
    String clave =
      transferencia.getEmisor() +
      "_" +
      transferencia.getReceptor() +
      "_" +
      transferencia.getMonto();
    return clavesPagosPendientes.contains(clave);
  }

  public boolean tieneTransferenciaPendiente(Usuario emisor, Usuario receptor, Double monto) {
    String nombreEmisor = emisor != null ? emisor.getNombre() : null;
    String nombreReceptor = receptor != null ? receptor.getNombre() : null;

    for (TransferenciaSugerida transferencia : liquidacionFinal) {
      if (
        Objects.equals(transferencia.getEmisor(), nombreEmisor) &&
        Objects.equals(transferencia.getReceptor(), nombreReceptor) &&
        Math.abs(transferencia.getMonto() - monto) < MARGEN_MONTO
      ) {
        return true;
      }
    }
    return false;
  }

  public boolean isSaldoPendiente() {
    return balanceFinal != null && balanceFinal < -MARGEN_MONTO;
  }
}
