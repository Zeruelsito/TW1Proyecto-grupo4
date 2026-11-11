package com.tallerwebi.presentacion;

public class DatosPagoBaja {

  private Long usuarioId;
  private String emisor;
  private String receptor;
  private Double monto;

  public DatosPagoBaja() {}

  public DatosPagoBaja(Long usuarioId, String emisor, String receptor, Double monto) {
    this.usuarioId = usuarioId;
    this.emisor = emisor;
    this.receptor = receptor;
    this.monto = monto;
  }

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    this.usuarioId = usuarioId;
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
}
