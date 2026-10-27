package com.tallerwebi.dominio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos_realizados")
public class PagoRealizado {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String emisor;
  private String receptor;
  private Double monto;
  private LocalDateTime fecha;

  @ManyToOne
  @JoinColumn(name = "hogar_id")
  private Hogar hogar;

  public PagoRealizado() {}

  public PagoRealizado(
    String emisor,
    String receptor,
    Double monto,
    LocalDateTime fecha,
    Hogar hogar
  ) {
    this.emisor = emisor;
    this.receptor = receptor;
    this.monto = monto;
    this.fecha = fecha;
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

  public LocalDateTime getFecha() {
    return fecha;
  }

  public void setFecha(LocalDateTime fecha) {
    this.fecha = fecha;
  }

  public Hogar getHogar() {
    return hogar;
  }

  public void setHogar(Hogar hogar) {
    this.hogar = hogar;
  }
}
