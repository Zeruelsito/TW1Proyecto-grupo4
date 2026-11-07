package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.ItemCompraInvalidoException;
import com.tallerwebi.dominio.excepcion.ItemCompraNoExiste;
import com.tallerwebi.dominio.excepcion.SinItemsMarcadosException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioListaComprasTest {

  private ServicioListaCompras servicioListaCompras;
  private RepositorioItemCompra repositorioItemCompraMock;
  private ServicioRegistroGasto servicioRegistroGastoMock;
  private ServicioHogar servicioHogarMock;
  private ServicioUsuario servicioUsuarioMock;

  @BeforeEach
  public void init() {
    repositorioItemCompraMock = mock(RepositorioItemCompra.class);
    servicioRegistroGastoMock = mock(ServicioRegistroGasto.class);
    servicioHogarMock = mock(ServicioHogar.class);
    servicioUsuarioMock = mock(ServicioUsuario.class);

    servicioListaCompras =
      new ServicioListaComprasImpl(
        repositorioItemCompraMock,
        servicioRegistroGastoMock,
        servicioHogarMock,
        servicioUsuarioMock
      );
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
    leche.setPrecioUnitario(100.0);
    leche.setCantidad(1);
    leche.setDestino(DestinoItemCompra.HOGAR);

    ItemCompra pan = new ItemCompra("Pan", 1L, EstadoItemCompra.EN_CARRITO);
    pan.setPrecioUnitario(50.0);
    pan.setCantidad(2);
    pan.setDestino(DestinoItemCompra.HOGAR);

    when(repositorioItemCompraMock.obtenerPorEstado(EstadoItemCompra.EN_CARRITO))
      .thenReturn(List.of(leche, pan));

    // ejecucion
    String resultado = servicioListaCompras.convertirEnGasto(1L, 1L);

    // validacion
    assertThat(resultado, equalTo("Compra convertida en gastos con éxito."));
    assertThat(leche.getEstado(), equalTo(EstadoItemCompra.ARCHIVADO));
    assertThat(pan.getEstado(), equalTo(EstadoItemCompra.ARCHIVADO));
  }

  @Test
  public void convertirEnGastoSinItemsEnCarritoDeberiaLanzarExcepcion() {
    // preparacion
    when(repositorioItemCompraMock.obtenerPorEstado(EstadoItemCompra.EN_CARRITO))
      .thenReturn(List.of());

    // ejecucion y validacion
    assertThrows(
      SinItemsMarcadosException.class,
      () -> servicioListaCompras.convertirEnGasto(1L, 1L)
    );
  }

  @Test
  public void actualizarDetallesConPrecioInvalidoDeberiaLanzarExcepcion() {
    ItemCompra item = new ItemCompra("Leche", 1L, EstadoItemCompra.EN_CARRITO);
    when(repositorioItemCompraMock.obtenerPorId(1L)).thenReturn(item);

    assertThrows(
      ItemCompraInvalidoException.class,
      () ->
        servicioListaCompras.actualizarDetallesItem(
          1L,
          -10.0,
          1,
          DestinoItemCompra.HOGAR,
          null,
          null
        )
    );
  }

  @Test
  public void convertirEnGastoConMultiplesDestinosDeberiaGenerarVariosGastos() {
    ItemCompra itemHogar = new ItemCompra("Fideos", 1L, EstadoItemCompra.EN_CARRITO);
    itemHogar.setPrecioUnitario(100.0);
    itemHogar.setCantidad(1);
    itemHogar.setDestino(DestinoItemCompra.HOGAR);

    when(repositorioItemCompraMock.obtenerPorEstado(EstadoItemCompra.EN_CARRITO))
      .thenReturn(List.of(itemHogar));

    Hogar hogar = new Hogar();
    hogar.setIntegrantes(List.of(new Usuario()));
    when(servicioHogarMock.buscarPorId(1L)).thenReturn(hogar);
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(new Usuario());

    String resultado = servicioListaCompras.convertirEnGasto(1L, 1L);

    assertThat(resultado, equalTo("Compra convertida en gastos con éxito."));
    verify(servicioRegistroGastoMock, atLeastOnce())
      .registrarGastoConParticipantes(any(Gasto.class), eq(1L), anyList());
  }
}
