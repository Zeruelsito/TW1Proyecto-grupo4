package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpSession;

public final class SessionUtils {

  private SessionUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static Long obtenerUsuarioIdDeSesion(HttpSession session) {
    if (session == null) {
      return 1L;
    }

    Long usuarioId = null;
    Object idObj = session.getAttribute("USUARIO_ID");
    if (idObj instanceof Long) {
      usuarioId = (Long) idObj;
    } else if (idObj instanceof Integer) {
      usuarioId = ((Integer) idObj).longValue();
    }
    if (usuarioId == null) {
      Usuario usuarioLogueadoSesion = (Usuario) session.getAttribute("usuarioLogueado");
      if (usuarioLogueadoSesion != null) {
        usuarioId = usuarioLogueadoSesion.getId();
      }
    }
    return usuarioId != null ? usuarioId : 1L;
  }
}
