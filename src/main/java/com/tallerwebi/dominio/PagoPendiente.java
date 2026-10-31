package com.tallerwebi.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "pagos_pendientes")
public class PagoPendiente {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String emisor; // <--- AGREGAR ESTO
  private String receptor;
  private Double monto;

  @ManyToOne
  @JoinColumn(name = "hogar_id")
  private Hogar hogar;

  public PagoPendiente() {}

  public PagoPendiente(String emisor, String receptor, Double monto, Hogar hogar) {
    this.emisor = emisor;
    this.receptor = receptor;
    this.monto = monto;
    this.hogar = hogar;
  }

  // Getters y Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public Hogar getHogar() {
    return hogar;
  }

  public void setHogar(Hogar hogar) {
    this.hogar = hogar;
  }
}
