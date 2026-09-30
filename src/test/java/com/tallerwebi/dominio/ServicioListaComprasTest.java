package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
 
import com.tallerwebi.dominio.ServicioListaCompras;
import com.tallerwebi.dominio.enums.EstadoItemCompra;
import com.tallerwebi.dominio.excepcion.ItemCompraInvalidoException;
import com.tallerwebi.dominio.excepcion.ItemCompraNoExiste;
import com.tallerwebi.dominio.excepcion.SinItemsMarcadosException;
import com.tallerwebi.dominio.repository.RepositorioItemCompra;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
 
public class ServicioListaComprasTest {
 
  private ServicioListaCompras servicioListaCompras;
  private RepositorioItemCompra repositorioItemCompraMock;
 
  @BeforeEach
  public void init() {
    repositorioItemCompraMock = mock(RepositorioItemCompra.class);
    servicioListaCompras = new ServicioListaCompras(repositorioItemCompraMock);
  }
 
  @Test
  public void agregarItemConNombreValidoDeberiaGuardarlo() {
    // ejecucion
    servicioListaCompras.agregarItem("Leche", 1L);
 
    // validacion
    verify(repositorioItemCompraMock, times(1)).guardar(any(ItemCompra.class));
  }
 
  @Test
  public void agregarItemConNombreVacioDeberiaLanzarExcepcion() {
    // ejecucion y validacion
    assertThrows(
      ItemCompraInvalidoException.class,
      () -> servicioListaCompras.agregarItem("  ", 1L)
    );
    verify(repositorioItemCompraMock, times(0)).guardar(any(ItemCompra.class));
  }
 
  @Test
  public void agregarItemConNombreMuyLargoDeberiaLanzarExcepcion() {
    // preparacion
    String nombreLargo = "a".repeat(101);
 
    // ejecucion y validacion
    assertThrows(
      ItemCompraInvalidoException.class,
      () -> servicioListaCompras.agregarItem(nombreLargo, 1L)
    );
  }
 
  @Test
  public void alternarItemPendienteDeberiaPasarloAlCarrito() {
    // preparacion
    ItemCompra item = new ItemCompra("Leche", 1L, EstadoItemCompra.PENDIENTE);
    when(repositorioItemCompraMock.obtenerPorId(1L)).thenReturn(item);
 
    // ejecucion
    servicioListaCompras.alternarItem(1L);
 
    // validacion
    assertThat(item.getEstado(), equalTo(EstadoItemCompra.EN_CARRITO));
  }
 
  @Test
  public void alternarItemArchivadoDeberiaLanzarExcepcion() {
    // preparacion
    ItemCompra item = new ItemCompra("Leche", 1L, EstadoItemCompra.ARCHIVADO);
    when(repositorioItemCompraMock.obtenerPorId(1L)).thenReturn(item);
 
    // ejecucion y validacion
    assertThrows(ItemCompraNoExiste.class, () -> servicioListaCompras.alternarItem(1L));
  }
 
  @Test
  public void eliminarItemDeberiaBorrarloDelRepositorio() {
    // preparacion
    ItemCompra item = new ItemCompra("Leche", 1L, EstadoItemCompra.PENDIENTE);
    when(repositorioItemCompraMock.obtenerPorId(1L)).thenReturn(item);
 
    // ejecucion
    servicioListaCompras.eliminarItem(1L);
 
    // validacion
    verify(repositorioItemCompraMock, times(1)).eliminar(item);
  }
 
  @Test
  public void convertirEnGastoDeberiaArmarLaDescripcionYArchivarLosItems() {
    // preparacion
    ItemCompra leche = new ItemCompra("Leche", 1L, EstadoItemCompra.EN_CARRITO);
    ItemCompra pan = new ItemCompra("Pan", 1L, EstadoItemCompra.EN_CARRITO);
    when(repositorioItemCompraMock.obtenerPorEstado(EstadoItemCompra.EN_CARRITO))
      .thenReturn(List.of(leche, pan));
 
    // ejecucion
    String descripcion = servicioListaCompras.convertirEnGasto();
 
    // validacion
    assertThat(descripcion, equalTo("Compras: Leche, Pan"));
    assertThat(leche.getEstado(), equalTo(EstadoItemCompra.ARCHIVADO));
    assertThat(pan.getEstado(), equalTo(EstadoItemCompra.ARCHIVADO));
  }
 
  @Test
  public void convertirEnGastoSinItemsEnCarritoDeberiaLanzarExcepcion() {
    // preparacion
    when(repositorioItemCompraMock.obtenerPorEstado(EstadoItemCompra.EN_CARRITO))
      .thenReturn(List.of());
 
    // ejecucion y validacion
    assertThrows(SinItemsMarcadosException.class, () -> servicioListaCompras.convertirEnGasto());
  }
}