package com.tallerwebi.dominio;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "items_compra")
public class ItemCompra {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;

  private Long usuarioId; // Quién lo agregó a la lista

  @Enumerated(EnumType.STRING)
  private EstadoItemCompra estado = EstadoItemCompra.PENDIENTE;

  // --- NUEVOS CAMPOS PARA LA COMPRA Y GASTO ---
  private Double precioUnitario;

  private Integer cantidad;

  @Enumerated(EnumType.STRING)
  private DestinoItemCompra destino; // HOGAR, PERSONAL, GRUPAL

  @ManyToMany
  @JoinTable(
    name = "item_compra_destinatarios",
    joinColumns = @JoinColumn(name = "item_compra_id"),
    inverseJoinColumns = @JoinColumn(name = "usuario_id")
  )
  private List<Usuario> destinatarios; // Integrantes específicos si es un gasto grupal

  @ManyToOne
  @JoinColumn(name = "destinatario_individual_id")
  private Usuario destinatarioIndividual; // Por si es para un único integrante específico

  public ItemCompra() {}

  public ItemCompra(String nombre, Long usuarioId, EstadoItemCompra estado) {
    this.nombre = nombre;
    this.usuarioId = usuarioId;
    this.estado = estado;
    this.cantidad = 1; // Por defecto arranca en 1
  }

  // --- GETTERS Y SETTERS ---

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

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    this.usuarioId = usuarioId;
  }

  public EstadoItemCompra getEstado() {
    return estado;
  }

  public void setEstado(EstadoItemCompra estado) {
    this.estado = estado;
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

  public List<Usuario> getDestinatarios() {
    return destinatarios;
  }

  public void setDestinatarios(List<Usuario> destinatarios) {
    this.destinatarios = destinatarios;
  }

  // Métodos útiles de ayuda
  public boolean estaEnCarrito() {
    return EstadoItemCompra.EN_CARRITO.equals(this.estado);
  }

  public boolean estaArchivado() {
    return EstadoItemCompra.ARCHIVADO.equals(this.estado);
  }

  public Usuario getDestinatarioIndividual() {
    return destinatarioIndividual;
  }

  public void setDestinatarioIndividual(Usuario destinatarioIndividual) {
    this.destinatarioIndividual = destinatarioIndividual;
  }
}
