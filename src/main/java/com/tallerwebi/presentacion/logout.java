package com.tallerwebi.presentacion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class logout {

  @GetMapping("/logout")
  public String cerrarSesion(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate(); // Destruye la sesión aquí una sola vez
    }
    // 2. Agregar headers para evitar que el navegador guarde caché de páginas protegidas
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0
    response.setDateHeader("Expires", 0);

    return "redirect:/login"; // Redirige al login
  }
}
