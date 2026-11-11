package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.ItemCompraInvalidoException;
import com.tallerwebi.dominio.excepcion.ItemCompraNoExiste;
import com.tallerwebi.dominio.excepcion.SinItemsMarcadosException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorListaCompras {

  private static final String ATRIBUTO_USUARIO_ID = "USUARIO_ID";
  private static final String CLAVE_HOGAR_ID = "HOGAR_ID";
  private static final String VISTA_LISTA_COMPRAS = "lista-compras";
  private static final String REDIRECT_LISTA_COMPRAS = "redirect:/lista-compras";
  private static final String REDIRECT_LOGIN = "redirect:/login";

  private final ServicioListaCompras servicioListaCompras;
  private final ServicioHogar servicioHogar;
  private final HttpSession session;

  public ControladorListaCompras(
    ServicioListaCompras servicioListaCompras,
    ServicioHogar servicioHogar,
    HttpSession session
  ) {
    this.servicioListaCompras = servicioListaCompras;
    this.servicioHogar = servicioHogar;
    this.session = session;
  }

  @RequestMapping(path = "/lista-compras", method = RequestMethod.GET)
  public ModelAndView irAListaDeCompras(
    HttpServletRequest request,
    RedirectAttributes redirectAttributes
  ) {
    Long hogarId = (Long) session.getAttribute(CLAVE_HOGAR_ID);
    if (hogarId == null) {
      redirectAttributes.addFlashAttribute(
        "alertaHogar",
        "Para ver la lista de compras debes pertenecer a un hogar."
      );
      return new ModelAndView("redirect:/dashboard");
    }

    if (obtenerIdUsuario(request) == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    return armarVista(hogarId, null);
  }

  @RequestMapping(path = "/lista-compras/agregar", method = RequestMethod.POST)
  public ModelAndView agregarItem(
    @RequestParam(value = "nombre", required = false) String nombre,
    HttpServletRequest request
  ) {
    Long idUsuario = obtenerIdUsuario(request);
    if (idUsuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    try {
      servicioListaCompras.agregarItem(nombre, idUsuario);
    } catch (ItemCompraInvalidoException e) {
      Long hogarId = (Long) session.getAttribute(CLAVE_HOGAR_ID);
      return armarVista(hogarId, e.getMessage());
    }
    return new ModelAndView(REDIRECT_LISTA_COMPRAS);
  }

  @RequestMapping(path = "/lista-compras/{id}/alternar", method = RequestMethod.POST)
  public ModelAndView alternarItem(@PathVariable("id") Long idItem, HttpServletRequest request) {
    if (obtenerIdUsuario(request) == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    try {
      servicioListaCompras.alternarItem(idItem);
    } catch (ItemCompraNoExiste e) {
      Long hogarId = (Long) session.getAttribute(CLAVE_HOGAR_ID);
      return armarVista(hogarId, e.getMessage());
    }
    return new ModelAndView(REDIRECT_LISTA_COMPRAS);
  }

  @RequestMapping(path = "/lista-compras/{id}/actualizar-detalles", method = RequestMethod.POST)
  public ModelAndView actualizarDetalles(
    @PathVariable("id") Long idItem,
    @RequestParam(value = "precioUnitario", required = false) Double precioUnitario,
    @RequestParam(value = "cantidad", required = false) Integer cantidad,
    @RequestParam(value = "destino", required = false) DestinoItemCompra destino,
    @RequestParam(
      value = "destinatarioIndividualId",
      required = false
    ) Long destinatarioIndividualId,
    @RequestParam(value = "destinatariosIds", required = false) List<Long> destinatariosIds,
    HttpServletRequest request
  ) {
    if (obtenerIdUsuario(request) == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    try {
      servicioListaCompras.actualizarDetallesItem(
        idItem,
        precioUnitario,
        cantidad,
        destino,
        destinatarioIndividualId,
        destinatariosIds
      );
    } catch (Exception e) {
      Long hogarId = (Long) session.getAttribute(CLAVE_HOGAR_ID);
      return armarVista(hogarId, e.getMessage());
    }
    return new ModelAndView(REDIRECT_LISTA_COMPRAS);
  }

  @RequestMapping(path = "/lista-compras/{id}/eliminar", method = RequestMethod.POST)
  public ModelAndView eliminarItem(@PathVariable("id") Long idItem, HttpServletRequest request) {
    if (obtenerIdUsuario(request) == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    try {
      servicioListaCompras.eliminarItem(idItem);
    } catch (ItemCompraNoExiste e) {
      Long hogarId = (Long) session.getAttribute(CLAVE_HOGAR_ID);
      return armarVista(hogarId, e.getMessage());
    }
    return new ModelAndView(REDIRECT_LISTA_COMPRAS);
  }

  @RequestMapping(path = "/lista-compras/convertir-en-gasto-masivo", method = RequestMethod.POST)
  public ModelAndView convertirEnGastoMasivo(
    @RequestBody List<ItemCompraActualizacion> dtos,
    HttpServletRequest request,
    RedirectAttributes redirectAttributes
  ) {
    Long usuarioId = obtenerIdUsuario(request);
    Long hogarId = (Long) session.getAttribute(CLAVE_HOGAR_ID);
    if (usuarioId == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    try {
      servicioListaCompras.actualizarDetallesYConvertirEnGasto(dtos, usuarioId, hogarId);
      redirectAttributes.addFlashAttribute(
        "mensajeExito",
        "¡Compra convertida en gastos con éxito!"
      );
      return new ModelAndView(REDIRECT_LISTA_COMPRAS);
    } catch (Exception e) {
      return armarVista(hogarId, e.getMessage());
    }
  }

  private Long obtenerIdUsuario(HttpServletRequest request) {
    return (Long) request.getSession().getAttribute(ATRIBUTO_USUARIO_ID);
  }

  private ModelAndView armarVista(Long hogarId, String mensajeError) {
    List<ItemCompra> items = servicioListaCompras.obtenerItemsActivos();
    long cantidadEnCarrito = items.stream().filter(ItemCompra::estaEnCarrito).count();

    Hogar hogar = servicioHogar.buscarPorId(hogarId);
    List<Usuario> integrantes = hogar != null ? hogar.getIntegrantes() : null;

    ModelAndView mav = new ModelAndView(VISTA_LISTA_COMPRAS);
    mav.addObject("items", items);
    mav.addObject("cantidadEnCarrito", cantidadEnCarrito);
    mav.addObject("integrantes", integrantes);
    if (mensajeError != null) {
      mav.addObject("mensajeError", mensajeError);
    }
    return mav;
  }
}
