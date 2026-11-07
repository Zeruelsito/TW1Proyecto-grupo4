package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.ItemCompraInvalidoException;
import com.tallerwebi.dominio.excepcion.ItemCompraNoExiste;
import com.tallerwebi.dominio.excepcion.SinItemsMarcadosException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("servicioListaCompras")
@Transactional
public class ServicioListaComprasImpl implements ServicioListaCompras {

  private static final int LARGO_MAXIMO_NOMBRE = 100;
  private static final String PREFIJO_DESCRIPCION = "Ticket Compra: ";

  private final RepositorioItemCompra repositorioItemCompra;
  private final ServicioRegistroGasto servicioRegistroGasto;
  private final ServicioHogar servicioHogar;
  private final ServicioUsuario servicioUsuario;

  public ServicioListaComprasImpl(
    RepositorioItemCompra repositorioItemCompra,
    ServicioRegistroGasto servicioRegistroGasto,
    ServicioHogar servicioHogar,
    ServicioUsuario servicioUsuario
  ) {
    this.repositorioItemCompra = repositorioItemCompra;
    this.servicioRegistroGasto = servicioRegistroGasto;
    this.servicioHogar = servicioHogar;
    this.servicioUsuario = servicioUsuario;
  }

  @Override
  public List<ItemCompra> obtenerItemsActivos() {
    return repositorioItemCompra.obtenerActivos();
  }

  @Override
  public void agregarItem(String nombre, Long idUsuario) {
    validarNombre(nombre);
    repositorioItemCompra.guardar(
      new ItemCompra(nombre.trim(), idUsuario, EstadoItemCompra.PENDIENTE)
    );
  }

  @Override
  public void alternarItem(Long idItem) {
    ItemCompra item = buscarItemActivo(idItem);
    if (item.estaEnCarrito()) {
      item.setEstado(EstadoItemCompra.PENDIENTE);
    } else {
      item.setEstado(EstadoItemCompra.EN_CARRITO);
    }
    repositorioItemCompra.guardar(item);
  }

  @Override
  public void eliminarItem(Long idItem) {
    repositorioItemCompra.eliminar(buscarItemActivo(idItem));
  }

  @Override
  public void actualizarDetallesItem(
    Long idItem,
    Double precioUnitario,
    Integer cantidad,
    DestinoItemCompra destino,
    Long destinatarioIndividualId,
    List<Long> destinatariosIds
  ) {
    ItemCompra item = buscarItemActivo(idItem);
    validarValoresItem(precioUnitario, cantidad, destino);

    item.setPrecioUnitario(precioUnitario);
    item.setCantidad(cantidad);
    item.setDestino(destino);

    asignarDestinatarios(item, destino, destinatarioIndividualId, destinatariosIds);
    repositorioItemCompra.guardar(item);
  }

  @Override
  public String convertirEnGasto(Long usuarioId, Long hogarId) {
    List<ItemCompra> enCarrito = repositorioItemCompra.obtenerPorEstado(
      EstadoItemCompra.EN_CARRITO
    );
    if (enCarrito.isEmpty()) {
      throw new SinItemsMarcadosException("Marcá al menos un ítem como comprado.");
    }

    validarItemsEnCarrito(enCarrito);

    Hogar hogar = servicioHogar.buscarPorId(hogarId);
    List<Usuario> integrantesHogar = (hogar != null && hogar.getIntegrantes() != null)
      ? hogar.getIntegrantes()
      : new ArrayList<>();
    Usuario usuarioComprador = servicioUsuario.obtenerUsuarioPorId(usuarioId);

    ContenedorGastosCompra contenedor = procesarItemsCarrito(
      enCarrito,
      usuarioComprador,
      integrantesHogar
    );

    registrarGastoHogar(contenedor, hogarId, usuarioComprador, integrantesHogar);
    registrarGastosIndividuales(contenedor, hogarId, usuarioComprador);
    registrarGastosGrupales(contenedor, hogarId, usuarioComprador);

    archivarItems(enCarrito);

    return "Compra convertida en gastos con éxito.";
  }

  @Override
  public void actualizarDetallesYConvertirEnGasto(
    List<ItemCompraActualizacion> dtos,
    Long usuarioId,
    Long hogarId
  ) {
    if (dtos != null) {
      for (ItemCompraActualizacion dto : dtos) {
        actualizarDetallesItem(
          dto.getId(),
          dto.getPrecioUnitario(),
          dto.getCantidad(),
          dto.getDestino(),
          dto.getDestinatarioIndividualId(),
          dto.getDestinatariosIds()
        );
      }
    }
    convertirEnGasto(usuarioId, hogarId);
  }

  /* --- Métodos Auxiliares Privados para Reducir Complejidad PMD --- */

  private void validarValoresItem(
    Double precioUnitario,
    Integer cantidad,
    DestinoItemCompra destino
  ) {
    if (precioUnitario == null || precioUnitario <= 0) {
      throw new ItemCompraInvalidoException("El precio unitario debe ser mayor a cero.");
    }
    if (cantidad == null || cantidad <= 0) {
      throw new ItemCompraInvalidoException("La cantidad debe ser mayor a cero.");
    }
    if (destino == null) {
      throw new ItemCompraInvalidoException("Debe seleccionar un destino para el ítem.");
    }
  }

  private void asignarDestinatarios(
    ItemCompra item,
    DestinoItemCompra destino,
    Long destinatarioIndividualId,
    List<Long> destinatariosIds
  ) {
    item.setDestinatarioIndividual(null);
    item.setDestinatarios(null);

    if (DestinoItemCompra.INDIVIDUAL.equals(destino) && destinatarioIndividualId != null) {
      Usuario usuarioDestino = servicioUsuario.obtenerUsuarioPorId(destinatarioIndividualId);
      item.setDestinatarioIndividual(usuarioDestino);
    } else if (
      DestinoItemCompra.GRUPAL.equals(destino) &&
      destinatariosIds != null &&
      !destinatariosIds.isEmpty()
    ) {
      List<Usuario> grupo = new ArrayList<>();
      for (Long idUsuarioGrupo : destinatariosIds) {
        Usuario usuarioGrupo = servicioUsuario.obtenerUsuarioPorId(idUsuarioGrupo);
        if (usuarioGrupo != null) {
          grupo.add(usuarioGrupo);
        }
      }
      item.setDestinatarios(grupo);
    }
  }

  private void validarItemsEnCarrito(List<ItemCompra> enCarrito) {
    for (ItemCompra item : enCarrito) {
      if (
        item.getPrecioUnitario() == null || item.getCantidad() == null || item.getDestino() == null
      ) {
        throw new ItemCompraInvalidoException(
          "Todos los ítems en el carrito deben tener precio, cantidad y destino configurados antes de generar el gasto."
        );
      }
    }
  }

  private ContenedorGastosCompra procesarItemsCarrito(
    List<ItemCompra> enCarrito,
    Usuario comprador,
    List<Usuario> integrantesHogar
  ) {
    ContenedorGastosCompra contenedor = new ContenedorGastosCompra();

    for (ItemCompra item : enCarrito) {
      double subtotal = item.getPrecioUnitario() * item.getCantidad();
      String etiquetaCantidad = item.getNombre() + " (x" + item.getCantidad() + ")";

      if (DestinoItemCompra.HOGAR.equals(item.getDestino())) {
        contenedor.totalHogar += subtotal;
        contenedor.nombresHogar.add(etiquetaCantidad);
      } else if (DestinoItemCompra.INDIVIDUAL.equals(item.getDestino())) {
        Usuario dest = item.getDestinatarioIndividual() != null
          ? item.getDestinatarioIndividual()
          : comprador;
        contenedor.totalesIndividuales.put(
          dest,
          contenedor.totalesIndividuales.getOrDefault(dest, 0.0) + subtotal
        );
        contenedor.nombresIndividuales
          .computeIfAbsent(dest, k -> new ArrayList<>())
          .add(etiquetaCantidad);
      } else if (DestinoItemCompra.GRUPAL.equals(item.getDestino())) {
        List<Usuario> grupo = (item.getDestinatarios() != null &&
            !item.getDestinatarios().isEmpty())
          ? item.getDestinatarios()
          : integrantesHogar;
        String claveGrupo = generarClaveGrupo(grupo);

        contenedor.gruposMap.put(claveGrupo, grupo);
        contenedor.totalesGrupales.put(
          claveGrupo,
          contenedor.totalesGrupales.getOrDefault(claveGrupo, 0.0) + subtotal
        );
        contenedor.nombresGrupales
          .computeIfAbsent(claveGrupo, k -> new ArrayList<>())
          .add(etiquetaCantidad);
      }
    }
    return contenedor;
  }

  private String generarClaveGrupo(List<Usuario> grupo) {
    StringBuilder sbClave = new StringBuilder();
    grupo
      .stream()
      .map(Usuario::getId)
      .sorted()
      .forEach(idUsuario -> sbClave.append(idUsuario).append("-"));
    return sbClave.toString();
  }

  private void registrarGastoHogar(
    ContenedorGastosCompra contenedor,
    Long hogarId,
    Usuario comprador,
    List<Usuario> integrantesHogar
  ) {
    if (contenedor.totalHogar > 0) {
      Gasto gastoHogar = crearGastoBase(
        PREFIJO_DESCRIPCION + String.join(", ", contenedor.nombresHogar),
        contenedor.totalHogar,
        comprador
      );
      List<Long> idsIntegrantes = new ArrayList<>();
      for (Usuario integrante : integrantesHogar) {
        idsIntegrantes.add(integrante.getId());
      }
      servicioRegistroGasto.registrarGastoConParticipantes(gastoHogar, hogarId, idsIntegrantes);
    }
  }

  private void registrarGastosIndividuales(
    ContenedorGastosCompra contenedor,
    Long hogarId,
    Usuario comprador
  ) {
    for (Map.Entry<Usuario, Double> entry : contenedor.totalesIndividuales.entrySet()) {
      Usuario destinatario = entry.getKey();
      Double monto = entry.getValue();
      List<String> nombres = contenedor.nombresIndividuales.get(destinatario);

      Gasto gastoInd = crearGastoBase(
        PREFIJO_DESCRIPCION +
        "Personal (" +
        destinatario.getNombre() +
        "): " +
        String.join(", ", nombres),
        monto,
        comprador
      );
      servicioRegistroGasto.registrarGastoConParticipantes(
        gastoInd,
        hogarId,
        List.of(destinatario.getId())
      );
    }
  }

  private void registrarGastosGrupales(
    ContenedorGastosCompra contenedor,
    Long hogarId,
    Usuario comprador
  ) {
    for (Map.Entry<String, Double> entry : contenedor.totalesGrupales.entrySet()) {
      String clave = entry.getKey();
      Double monto = entry.getValue();
      List<Usuario> grupo = contenedor.gruposMap.get(clave);
      List<String> nombres = contenedor.nombresGrupales.get(clave);

      Gasto gastoGrupal = crearGastoBase(
        PREFIJO_DESCRIPCION + "Subgrupo: " + String.join(", ", nombres),
        monto,
        comprador
      );
      List<Long> idsGrupo = new ArrayList<>();
      for (Usuario integranteGrupo : grupo) {
        idsGrupo.add(integranteGrupo.getId());
      }
      servicioRegistroGasto.registrarGastoConParticipantes(gastoGrupal, hogarId, idsGrupo);
    }
  }

  private Gasto crearGastoBase(String descripcion, Double monto, Usuario comprador) {
    Gasto gasto = new Gasto();
    gasto.setDescripcion(descripcion);
    gasto.setMonto(monto);
    gasto.setTipoGasto(TipoGasto.OCASIONAL);
    gasto.setCategoria("VARIOS");
    gasto.setPagado(true);
    gasto.setUsuarioPagador(comprador);
    return gasto;
  }

  private void archivarItems(List<ItemCompra> enCarrito) {
    for (ItemCompra item : enCarrito) {
      item.setEstado(EstadoItemCompra.ARCHIVADO);
      repositorioItemCompra.guardar(item);
    }
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
      throw new ItemCompraInvalidoException("El nombre no puede superar los 100 caracteres.");
    }
  }

  /* Clase DTO estática privada para agrupar los totales y simplificar parámetros */
  private static class ContenedorGastosCompra {

    private double totalHogar = 0.0;
    private final List<String> nombresHogar = new ArrayList<>();
    private final Map<Usuario, Double> totalesIndividuales = new HashMap<>();
    private final Map<Usuario, List<String>> nombresIndividuales = new HashMap<>();
    private final Map<String, List<Usuario>> gruposMap = new HashMap<>();
    private final Map<String, Double> totalesGrupales = new HashMap<>();
    private final Map<String, List<String>> nombresGrupales = new HashMap<>();
  }
}
