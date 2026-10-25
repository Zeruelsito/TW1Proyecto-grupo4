package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.RepositorioHogar;
import com.tallerwebi.dominio.ServicioCalculosGasto;
import com.tallerwebi.dominio.ServicioHogar;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorDashboard {

  @Autowired
  ServicioUsuario servicioUsuario;

  @Autowired
  ServicioCalculosGasto servicioCalculosGasto;

  @Autowired
  ServicioHogar servicioHogar;

  @Autowired
  RepositorioHogar repositorioHogar;

  @GetMapping("/dashboard")
  public ModelAndView irADashboard(HttpSession session) {
    Map<String, Object> model = new HashMap<>();

    Long usuarioId = obtenerUsuarioIdDeSesion(session);
    Usuario usuarioActual = servicioUsuario.obtenerUsuarioPorId(usuarioId);

    if (usuarioActual == null) {
      usuarioActual = new Usuario("Juan", 300000.0);
      usuarioActual.setId(usuarioId);
    }

    model.put(
      "nombreUsuario",
      usuarioActual.getNombre() != null ? usuarioActual.getNombre() : "Usuario"
    );

    List<Hogar> hogaresUsuario = servicioHogar.obtenerHogares(usuarioId);
    model.put("hogaresUsuario", hogaresUsuario);

    if (hogaresUsuario != null && !hogaresUsuario.isEmpty()) {
      procesarMetricasHogar(hogaresUsuario.get(0), model);
    }

    return new ModelAndView("dashboard", model);
  }

  // Método principal simplificado
  private void procesarMetricasHogar(Hogar hogarActual, Map<String, Object> model) {
    List<Usuario> usuariosAgregados = obtenerIntegrantesOTodos(hogarActual);
    verificarYAsignarIngresos(usuariosAgregados);

    Double valorPisoMinimo = verificarPisoMinimo(hogarActual);

    if (usuariosAgregados != null && !usuariosAgregados.isEmpty()) {
      servicioCalculosGasto.calcularTotalSumaDeCapacidadesDeCadaUsuario(
        usuariosAgregados,
        valorPisoMinimo
      );
      servicioCalculosGasto.establecerMontoTotalDeLasSumasDeCapacidades(usuariosAgregados);
      servicioCalculosGasto.calcularPorcentajesDeCapacidadMensualDeUsuarios(usuariosAgregados);
      servicioCalculosGasto.calcularCuotasMensualesDeUsuarios(usuariosAgregados, valorPisoMinimo);

      Double precioHora = hogarActual.getPrecioHoraTarea() != null
        ? hogarActual.getPrecioHoraTarea()
        : 5000d;
      servicioCalculosGasto.calcularHorasDeTareasDeUsuarios(usuariosAgregados, precioHora);
    }

    model.put("usuarios", usuariosAgregados);
  }

  // Métodos auxiliares para mantener baja la complejidad ciclomática
  private List<Usuario> obtenerIntegrantesOTodos(Hogar hogarActual) {
    List<Usuario> integrantes = hogarActual.getIntegrantes();
    if (integrantes == null || integrantes.isEmpty()) {
      return servicioUsuario.obtenerTodos();
    }
    return integrantes;
  }

  private void verificarYAsignarIngresos(List<Usuario> usuarios) {
    for (Usuario u : usuarios) {
      if (u.getIngresoMensual() == null) {
        u.setIngresoMensual(150000.0);
      }
    }
  }

  private Double verificarPisoMinimo(Hogar hogarActual) {
    Double valorPisoMinimo = hogarActual.getPisoMinimoHogar();
    if (valorPisoMinimo == null || valorPisoMinimo <= 0.0) {
      valorPisoMinimo = 50000.0;
      hogarActual.setPisoMinimoHogar(valorPisoMinimo);
    }
    return valorPisoMinimo;
  }

  @PostMapping("/crear-hogar")
  public ModelAndView crearHogar(
    @RequestParam("nombreHogar") String nombreHogar,
    @RequestParam("pin") String pin,
    HttpSession session
  ) {
    Long usuarioId = obtenerUsuarioIdDeSesion(session);
    Usuario usuario = servicioUsuario.obtenerUsuarioPorId(usuarioId);

    if (usuario != null) {
      Hogar nuevoHogar = new Hogar(nombreHogar, pin);
      nuevoHogar.getIntegrantes().add(usuario);
      servicioHogar.crearHogar(nuevoHogar);
    }

    return new ModelAndView("redirect:/dashboard");
  }

  @PostMapping("/unirse-hogar")
  public ModelAndView unirseHogar(
    @RequestParam("nombreHogar") String nombreHogar,
    @RequestParam("pin") String pin,
    HttpSession session
  ) {
    Long usuarioId = obtenerUsuarioIdDeSesion(session);
    Usuario usuario = servicioUsuario.obtenerUsuarioPorId(usuarioId);

    if (usuario != null) {
      servicioHogar.unirseAHogar(nombreHogar, pin, usuario);
    }

    return new ModelAndView("redirect:/dashboard");
  }

  private Long obtenerUsuarioIdDeSesion(HttpSession session) {
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
