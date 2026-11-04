package com.tallerwebi.dominio;

import java.util.Objects;

public class TransferenciaSugerida {

  private Long emisorId; // ID del emisor (para solucionar el error de t.emisorId)
  private String emisor; // Nombre del emisor (para mostrar en la tabla)
  private String emailEmisor; // Email (para comparar en el th:if)
  private Long receptorId; // ID del receptor
  private String receptor; // Nombre del receptor
  private Double monto; // Cuánto debe transferir

  public TransferenciaSugerida() {}

  public TransferenciaSugerida(String emisor, String emailEmisor, String receptor, Double monto) {
    this.emisor = emisor;
    this.emailEmisor = emailEmisor;
    this.receptor = receptor;
    this.monto = monto;
  }

  // Constructor ampliado por si necesitas pasar los IDs directamente
  public TransferenciaSugerida(
    Long emisorId,
    String emisor,
    String emailEmisor,
    Long receptorId,
    String receptor,
    Double monto
  ) {
    this.emisorId = emisorId;
    this.emisor = emisor;
    this.emailEmisor = emailEmisor;
    this.receptorId = receptorId;
    this.receptor = receptor;
    this.monto = monto;
  }

  public Long getEmisorId() {
    return emisorId;
  }

  public void setEmisorId(Long emisorId) {
    this.emisorId = emisorId;
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

  public Long getReceptorId() {
    return receptorId;
  }

  public void setReceptorId(Long receptorId) {
    this.receptorId = receptorId;
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
    TransferenciaSugerida that = (TransferenciaSugerida) objetoComparado;
    return (
      Objects.equals(emisorId, that.emisorId) &&
      Objects.equals(emisor, that.emisor) &&
      Objects.equals(emailEmisor, that.emailEmisor) &&
      Objects.equals(receptorId, that.receptorId) &&
      Objects.equals(receptor, that.receptor) &&
      Objects.equals(monto, that.monto)
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(emisorId, emisor, emailEmisor, receptorId, receptor, monto);
  }
}
