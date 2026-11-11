package com.tallerwebi.dominio;

import java.util.List;
import java.util.Objects;

public class SimulacionBaja {

  private static final double MARGEN_MONTO = 0.01;

  private final Usuario usuarioSaliente;
  private final Double balanceFinal;
  private final List<TransferenciaSugerida> liquidacionFinal;
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
    this.usuarioSaliente = usuarioSaliente;
    this.balanceFinal = balanceFinal;
    this.liquidacionFinal = liquidacionFinal;
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

  public List<TransferenciaSugerida> getLiquidacionFinal() {
    return liquidacionFinal;
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
    return balanceFinal != null && balanceFinal < 0;
  }

  public boolean isDebeCobrar() {
    return balanceFinal != null && balanceFinal > 0;
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
    return balanceFinal != null && Math.abs(balanceFinal) > 0.01;
  }
}
