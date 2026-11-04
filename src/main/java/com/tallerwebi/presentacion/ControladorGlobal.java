package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Gasto;
import com.tallerwebi.dominio.ServicioRegistroGasto;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ControladorGlobal {

  private final ServicioRegistroGasto servicioRegistroGasto;
  private final ServicioUsuario servicioUsuario;

  @Autowired
  public ControladorGlobal(
    ServicioRegistroGasto servicioRegistroGasto,
    ServicioUsuario servicioUsuario
  ) {
    this.servicioRegistroGasto = servicioRegistroGasto;
    this.servicioUsuario = servicioUsuario;
  }

  // Inyecta el nombre del usuario logueado en TODAS las vistas automáticamente
  @ModelAttribute("nombreUsuario")
  public String obtenerNombreUsuarioActual(HttpSession session) {
    Long usuarioId = SessionUtils.obtenerUsuarioIdDeSesion(session);
    Usuario usuario = servicioUsuario.obtenerUsuarioPorId(usuarioId);
    if (usuario != null && usuario.getNombre() != null) {
      return usuario.getNombre();
    }

    return "Usuario";
  }

  @ModelAttribute("vencidosNotif")
  public List<Gasto> obtenerGastosVencidosNotificacion(HttpSession session) {
    Long hogarId = (Long) session.getAttribute("HOGAR_ID");
    if (hogarId == null) {
      return Collections.emptyList();
    }
    return servicioRegistroGasto.obtenerVencimientosVencidos(LocalDateTime.now(), hogarId);
  }

  @ModelAttribute("proximosNotif")
  public List<Gasto> obtenerGastosProximosNotificacion(HttpSession session) {
    Long hogarId = (Long) session.getAttribute("HOGAR_ID");
    if (hogarId == null) {
      return Collections.emptyList();
    }
    return servicioRegistroGasto.obtenerVencimientosProximos(LocalDateTime.now(), hogarId);
  }
}
