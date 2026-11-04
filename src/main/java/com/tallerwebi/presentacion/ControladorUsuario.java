package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class ControladorUsuario {

  @Autowired
  private ServicioUsuario servicioUsuario;

  @GetMapping
  public String verPerfil(Model model, HttpServletRequest request) {
    Long idUsuarioLogueado = 1L; // O de tu sesión actual

    Usuario usuario = servicioUsuario.obtenerUsuarioPorId(idUsuarioLogueado);
    model.addAttribute("usuario", usuario);

    // Capturamos la URL anterior (de donde vino el usuario) para el botón cancelar
    String urlAnterior = request.getHeader("Referer");
    model.addAttribute("urlAnterior", urlAnterior);

    return "perfil";
  }

  @PostMapping("/guardar")
  public String guardarPerfil(
    @ModelAttribute("usuario") Usuario usuarioFormulario,
    @RequestParam(value = "nuevaPassword", required = false) String nuevaPassword,
    RedirectAttributes redirectAttributes
  ) {
    try {
      Long idUsuarioLogueado = usuarioFormulario.getId();
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
