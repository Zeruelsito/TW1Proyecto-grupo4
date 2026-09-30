package com.tallerwebi.dominio;

//import com.tallerwebi.dominio.ItemCompra;
//import com.tallerwebi.dominio.EstadoItemCompra;
import java.util.List;

public interface RepositorioItemCompra {
  ItemCompra guardar(ItemCompra item);
  ItemCompra obtenerPorId(Long id);
  List<ItemCompra> obtenerActivos();
  List<ItemCompra> obtenerPorEstado(EstadoItemCompra estado);
  void eliminar(ItemCompra item);
}
