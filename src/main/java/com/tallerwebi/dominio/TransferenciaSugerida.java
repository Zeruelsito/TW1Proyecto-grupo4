package com.tallerwebi.dominio;

import java.util.Objects;

public class TransferenciaSugerida {

  private String emisor; // Quién debe pagar
  private String receptor; // Quién debe recibir
  private Double monto; // Cuánto debe transferir

  public TransferenciaSugerida() {}

  public TransferenciaSugerida(String emisor, String receptor, Double monto) {
    this.emisor = emisor;
    this.receptor = receptor;
    this.monto = monto;
  }

  public String getEmisor() {
    return emisor;
  }

  public void setEmisor(String emisor) {
    this.emisor = emisor;
  }

  public String getReceptor() {
    return receptor;
  }

  public void setReceptor(String receptor) {
    this.receptor = receptor;
  }

  public Double getMonto() {
    return monto;
  }

  public void setMonto(Double monto) {
    this.monto = monto;
  }

  @Override
  public boolean equals(Object objetoComparado) {
    if (this == objetoComparado) return true;
    if (objetoComparado == null || getClass() != objetoComparado.getClass()) return false;
    TransferenciaSugerida que = (TransferenciaSugerida) objetoComparado;
    return (
      Objects.equals(emisor, que.emisor) &&
      Objects.equals(receptor, que.receptor) &&
      Objects.equals(monto, que.monto)
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(emisor, receptor, monto);
  }
}
