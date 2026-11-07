package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioListaCompras {
  List<ItemCompra> obtenerItemsActivos();
  void agregarItem(String nombre, Long idUsuario);
  void alternarItem(Long idItem);
  void eliminarItem(Long idItem);
  void actualizarDetallesItem(
    Long idItem,
    Double precioUnitario,
    Integer cantidad,
    DestinoItemCompra destino,
    Long destinatarioIndividualId,
    List<Long> destinatariosIds
  );
  String convertirEnGasto(Long usuarioId, Long hogarId);
  void actualizarDetallesYConvertirEnGasto(
    List<ItemCompraActualizacion> dtos,
    Long usuarioId,
    Long hogarId
  );
}
