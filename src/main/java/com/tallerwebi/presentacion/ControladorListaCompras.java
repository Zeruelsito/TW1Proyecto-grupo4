package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.ItemCompraInvalidoException;
import com.tallerwebi.dominio.excepcion.ItemCompraNoExiste;
import com.tallerwebi.dominio.excepcion.SinItemsMarcadosException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorListaCompras {

  private static final String ATRIBUTO_USUARIO_ID = "USUARIO_ID";
  private static final String VISTA_LISTA_COMPRAS = "lista-compras";
  private static final String REDIRECT_LISTA_COMPRAS = "redirect:/lista-compras";
  private static final String REDIRECT_LOGIN = "redirect:/login";
  private static final String REDIRECT_GASTOS = "redirect:/gastos?descripcion=";

  private final ServicioListaCompras servicioListaCompras;
  private final ServicioUsuario servicioUsuario;
  private final HttpSession session;

  public ControladorListaCompras(
    ServicioListaCompras servicioListaCompras,
    ServicioUsuario servicioUsuario,
    HttpSession session
  ) {
    this.servicioListaCompras = servicioListaCompras;
    this.servicioUsuario = servicioUsuario;
    this.session = session;
  }

  @RequestMapping(path = "/lista-compras", method = RequestMethod.GET)
  public ModelAndView irAListaDeCompras(
    HttpServletRequest request,
    RedirectAttributes redirectAttributes
  ) {
    Long hogarId = (Long) session.getAttribute("HOGAR_ID");
    if (hogarId == null) {
      redirectAttributes.addFlashAttribute(
        "alertaHogar",
        "Para ver la liquidación y otras secciones debes pertenecer a un hogar."
      );
      return new ModelAndView("redirect:/dashboard");
    }

    if (obtenerIdUsuario(request) == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    return armarVista(null);
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
      return armarVista(e.getMessage());
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
      return armarVista(e.getMessage());
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
      return armarVista(e.getMessage());
    }
    return new ModelAndView(REDIRECT_LISTA_COMPRAS);
  }

  @RequestMapping(path = "/lista-compras/convertir-en-gasto", method = RequestMethod.POST)
  public ModelAndView convertirEnGasto(HttpServletRequest request) {
    if (obtenerIdUsuario(request) == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    try {
      String descripcion = servicioListaCompras.convertirEnGasto();
      return new ModelAndView(
        REDIRECT_GASTOS + URLEncoder.encode(descripcion, StandardCharsets.UTF_8)
      );
    } catch (SinItemsMarcadosException e) {
      return armarVista(e.getMessage());
    }
  }

  private Long obtenerIdUsuario(HttpServletRequest request) {
    return (Long) request.getSession().getAttribute(ATRIBUTO_USUARIO_ID);
  }

  private ModelAndView armarVista(String mensajeError) {
    List<ItemCompra> items = servicioListaCompras.obtenerItemsActivos();
    long cantidadEnCarrito = items.stream().filter(ItemCompra::estaEnCarrito).count();

    ModelAndView mav = new ModelAndView(VISTA_LISTA_COMPRAS);
    Long idUsuario = SessionUtils.obtenerUsuarioIdDeSesion(session);
    Usuario usuarioActual = servicioUsuario.obtenerUsuarioPorId(idUsuario);
    mav.addObject(
      "nombreUsuario",
      usuarioActual.getNombre() != null ? usuarioActual.getNombre() : "Usuario"
    );
    mav.addObject("items", items);
    mav.addObject("cantidadEnCarrito", cantidadEnCarrito);
    if (mensajeError != null) {
      mav.addObject("mensajeError", mensajeError);
    }
    return mav;
  }
}
