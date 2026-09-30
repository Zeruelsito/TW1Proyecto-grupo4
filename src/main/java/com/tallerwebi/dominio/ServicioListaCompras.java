package com.tallerwebi.dominio;

import com.tallerwebi.dominio.ItemCompra;
import com.tallerwebi.dominio.EstadoItemCompra;
import com.tallerwebi.dominio.excepcion.ItemCompraInvalidoException;
import com.tallerwebi.dominio.excepcion.ItemCompraNoExiste;
import com.tallerwebi.dominio.excepcion.SinItemsMarcadosException;
import com.tallerwebi.dominio.repository.RepositorioItemCompra;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service("servicioListaCompras")
@Transactional
public class ServicioListaCompras {

    private static final int LARGO_MAXIMO_NOMBRE = 100;
    private static final int LARGO_MAXIMO_DESCRIPCION = 200;
    private static final String PREFIJO_DESCRIPCION = "Compras: ";
    private static final String SUFIJO_RECORTE = "...";

    private final RepositorioItemCompra repositorioItemCompra;

    public ServicioListaCompras(RepositorioItemCompra repositorioItemCompra) {
        this.repositorioItemCompra = repositorioItemCompra;
    }

    public List<ItemCompra> obtenerItemsActivos() {
        return repositorioItemCompra.obtenerActivos();
    }

    public void agregarItem(String nombre, Long idUsuario) {
        validarNombre(nombre);
        repositorioItemCompra.guardar(
            new ItemCompra(nombre.trim(), idUsuario, EstadoItemCompra.PENDIENTE)
        );
    }

    public void alternarItem(Long idItem) {
        ItemCompra item = buscarItemActivo(idItem);
        if (item.estaEnCarrito()) {
            item.setEstado(EstadoItemCompra.PENDIENTE);
        } else {
            item.setEstado(EstadoItemCompra.EN_CARRITO);
        }
    }

    public void eliminarItem(Long idItem) {
        repositorioItemCompra.eliminar(buscarItemActivo(idItem));
    }

    public String convertirEnGasto() {
        List<ItemCompra> enCarrito = repositorioItemCompra.obtenerPorEstado(
        EstadoItemCompra.EN_CARRITO
        );
        if (enCarrito.isEmpty()) {
            throw new SinItemsMarcadosException("Marcá al menos un ítem como comprado.");
        }

        String descripcion = armarDescripcion(enCarrito);
        for (ItemCompra item : enCarrito) {
            item.setEstado(EstadoItemCompra.ARCHIVADO);
        }
        return descripcion;
    }

    private String armarDescripcion(List<ItemCompra> items) {
        List<String> nombres = new ArrayList<>();
        for (ItemCompra item : items) {
            nombres.add(item.getNombre());
        }
        String descripcion = PREFIJO_DESCRIPCION + String.join(", ", nombres);

        if (descripcion.length() > LARGO_MAXIMO_DESCRIPCION) {
            int largoUtil = LARGO_MAXIMO_DESCRIPCION - SUFIJO_RECORTE.length();
            descripcion = descripcion.substring(0, largoUtil) + SUFIJO_RECORTE;
        }
        return descripcion;
    }

    private ItemCompra buscarItemActivo(Long idItem) {
        ItemCompra item = repositorioItemCompra.obtenerPorId(idItem);
        if (item == null || item.estaArchivado()) {
            throw new ItemCompraNoExiste("El ítem ya no está en la lista.");
        }
        return item;
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ItemCompraInvalidoException("El nombre del ítem no puede estar vacío.");
        }
        if (nombre.trim().length() > LARGO_MAXIMO_NOMBRE) {
            throw new ItemCompraInvalidoException("El nombre del ítem no puede superar los 100 caracteres.");
        }
    }
}
