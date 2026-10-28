package com.tallerwebi.dominio;

import java.util.List;

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

  public boolean tieneTransferenciaPendiente(String emisor, String receptor, Double monto) {
    for (TransferenciaSugerida transferencia : liquidacionFinal) {
      if (
        transferencia.getEmisor().equals(emisor) &&
        transferencia.getReceptor().equals(receptor) &&
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
