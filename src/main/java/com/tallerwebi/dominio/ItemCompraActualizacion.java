package com.tallerwebi.dominio;

import com.tallerwebi.dominio.DestinoItemCompra;
import java.util.List;

public class ItemCompraActualizacion {

  private Long id;
  private Double precioUnitario;
  private Integer cantidad;
  private DestinoItemCompra destino;
  private Long destinatarioIndividualId;
  private List<Long> destinatariosIds;

  // Getters y Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Double getPrecioUnitario() {
    return precioUnitario;
  }

  public void setPrecioUnitario(Double precioUnitario) {
    this.precioUnitario = precioUnitario;
  }

  public Integer getCantidad() {
    return cantidad;
  }

  public void setCantidad(Integer cantidad) {
    this.cantidad = cantidad;
  }

  public DestinoItemCompra getDestino() {
    return destino;
  }

  public void setDestino(DestinoItemCompra destino) {
    this.destino = destino;
  }

  public Long getDestinatarioIndividualId() {
    return destinatarioIndividualId;
  }

  public void setDestinatarioIndividualId(Long destinatarioIndividualId) {
    this.destinatarioIndividualId = destinatarioIndividualId;
  }

  public List<Long> getDestinatariosIds() {
    return destinatariosIds;
  }

  public void setDestinatariosIds(List<Long> destinatariosIds) {
    this.destinatariosIds = destinatariosIds;
  }
}
