package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class Vencimiento {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String titulo;

  private LocalDate fechaVencimiento;

  public Vencimiento() {}

  public Vencimiento(Long id, String titulo, LocalDate fechaVencimiento) {
    this.id = id;
    this.titulo = titulo;
    this.fechaVencimiento = fechaVencimiento;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public LocalDate getFechaVencimiento() {
    return fechaVencimiento;
  }

  public void setFechaVencimiento(LocalDate fechaVencimiento) {
    this.fechaVencimiento = fechaVencimiento;
  }
}
