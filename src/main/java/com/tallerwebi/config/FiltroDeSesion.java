package com.tallerwebi.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class FiltroDeSesion implements HandlerInterceptor {

  @Override
  public boolean preHandle(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler
  ) throws Exception {
    HttpSession session = request.getSession();

    // Verificamos si existe el usuario logueado en la sesión
    Object usuarioLogueado = session.getAttribute("usuarioLogueado");

    // Si no está logueado, lo mandamos al login
    if (usuarioLogueado == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return false; // Corta la ejecución para que no llegue al controlador
    }

    return true; // Deja continuar si está logueado
  }
}
