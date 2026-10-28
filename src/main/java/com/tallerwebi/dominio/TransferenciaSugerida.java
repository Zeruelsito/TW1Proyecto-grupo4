package com.tallerwebi.dominio;

import java.util.Objects;

public class TransferenciaSugerida {

  private String emisor; // Nombre del emisor (para mostrar en la tabla)
  private String emailEmisor; // Email (para comparar en el th:if)
  private String receptor; // Nombre del receptor
  private Double monto; // Cuánto debe transferir

  public TransferenciaSugerida() {}

  public TransferenciaSugerida(String emisor, String emailEmisor, String receptor, Double monto) {
    this.emisor = emisor;
    this.emailEmisor = emailEmisor;
    this.receptor = receptor;
    this.monto = monto;
  }

  public String getEmisor() {
    return emisor;
  }

  public void setEmisor(String emisor) {
    this.emisor = emisor;
  }

  public String getEmailEmisor() {
    return emailEmisor;
  }

  public void setEmailEmisor(String emailEmisor) {
    this.emailEmisor = emailEmisor;
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
      Objects.equals(emailEmisor, que.emailEmisor) &&
      Objects.equals(receptor, que.receptor) &&
      Objects.equals(monto, que.monto)
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(emisor, emailEmisor, receptor, monto);
  }
}
