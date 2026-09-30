package com.tallerwebi.dominio;

//import com.tallerwebi.dominio.EstadoItemCompra;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ItemCompra {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private Long idUsuarioCreador;

  @Enumerated(EnumType.STRING)
  private EstadoItemCompra estado;

  public ItemCompra() {}

  public ItemCompra(String nombre, Long idUsuarioCreador, EstadoItemCompra estado) {
    this.nombre = nombre;
    this.idUsuarioCreador = idUsuarioCreador;
    this.estado = estado;
  }

  public boolean estaEnCarrito() {
    return EstadoItemCompra.EN_CARRITO.equals(estado);
  }

  public boolean estaArchivado() {
    return EstadoItemCompra.ARCHIVADO.equals(estado);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public Long getIdUsuarioCreador() {
    return idUsuarioCreador;
  }

  public void setIdUsuarioCreador(Long idUsuarioCreador) {
    this.idUsuarioCreador = idUsuarioCreador;
  }

  public EstadoItemCompra getEstado() {
    return estado;
  }

  public void setEstado(EstadoItemCompra estado) {
    this.estado = estado;
  }
}
