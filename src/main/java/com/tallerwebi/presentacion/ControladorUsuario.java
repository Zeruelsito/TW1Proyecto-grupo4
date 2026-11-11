package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class ControladorUsuario {

  private final ServicioUsuario servicioUsuario;

  @Autowired
  public ControladorUsuario(ServicioUsuario servicioUsuario) {
    this.servicioUsuario = servicioUsuario;
  }

  @GetMapping
  public String verPerfil(Model model, HttpServletRequest request, HttpSession session) {
    Long idUsuarioLogueado = SessionUtils.obtenerUsuarioIdDeSesion(session);

    Usuario usuario = servicioUsuario.obtenerUsuarioPorId(idUsuarioLogueado);
    model.addAttribute("usuario", usuario);

    String urlAnterior = request.getHeader("Referer");
    model.addAttribute("urlAnterior", urlAnterior);

    return "perfil";
  }

  @PostMapping("/guardar")
  public String guardarPerfil(
    @ModelAttribute("usuario") Usuario usuarioFormulario,
    @RequestParam(value = "nuevaPassword", required = false) String nuevaPassword,
    RedirectAttributes redirectAttributes,
    HttpSession session
  ) {
    try {
      Long idUsuarioLogueado = SessionUtils.obtenerUsuarioIdDeSesion(session);

      servicioUsuario.actualizarPerfil(idUsuarioLogueado, usuarioFormulario, nuevaPassword);

      redirectAttributes.addFlashAttribute("exito", "Perfil actualizado correctamente.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
        "error",
        "Hubo un error al actualizar el perfil: " + e.getMessage()
      );
      return "redirect:/perfil";
    }

    return "redirect:/perfil";
  }
}
