package com.tallerwebi.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "pagos_pendientes")
public class PagoPendiente {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "emisor_id")
  private Usuario emisor;

  @ManyToOne
  @JoinColumn(name = "receptor_id")
  private Usuario receptor;

  private Double monto;

  @ManyToOne
  @JoinColumn(name = "hogar_id")
  private Hogar hogar;

  public PagoPendiente() {}

  public PagoPendiente(Usuario emisor, Usuario receptor, Double monto, Hogar hogar) {
    this.emisor = emisor;
    this.receptor = receptor;
    this.monto = monto;
    this.hogar = hogar;
  }

  // Getters y Setters actualizados a Usuario
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Usuario getEmisor() {
    return emisor;
  }

  public void setEmisor(Usuario emisor) {
    this.emisor = emisor;
  }

  public Usuario getReceptor() {
    return receptor;
  }

  public void setReceptor(Usuario receptor) {
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
