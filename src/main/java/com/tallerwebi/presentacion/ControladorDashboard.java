package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioCalculosGasto;
import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorDashboard {

  @Autowired
  ServicioUsuario servicioUsuario;

  @Autowired
  ServicioCalculosGasto servicioCalculosGasto;

  @GetMapping("/dashboard")
  public ModelAndView irADashboard() {
    Map<String, Object> model = new ModelMap();

    if (servicioUsuario.obtenerTodos().isEmpty()) {
      Usuario usuario1 = new Usuario("eze", 150000d);
      Usuario usuario2 = new Usuario("maca", 100000d);
      servicioUsuario.registrarUsuario(usuario1);
      servicioUsuario.registrarUsuario(usuario2);
    }
    List<Usuario> usuariosAgregados = servicioUsuario.obtenerTodos();
    Double valorPisoMinimo = 30000d;

    // Calcular capacidad individual
    servicioCalculosGasto.calcularTotalSumaDeCapacidadesDeCadaUsuario(
      usuariosAgregados,
      valorPisoMinimo
    );

    // Establecer total pozo acumulado
    servicioCalculosGasto.establecerMontoTotalDeLasSumasDeCapacidades(usuariosAgregados);

    // Calcular porcentajes PRIMERO
    servicioCalculosGasto.calcularPorcentajesDeCapacidadMensualDeUsuarios(usuariosAgregados);

    //  Calcular cuotas DESPUÉS (requiere que el porcentaje esté calculado)
    servicioCalculosGasto.calcularCuotasMensualesDeUsuarios(usuariosAgregados, valorPisoMinimo);

    // Calcular horasTarea
    servicioCalculosGasto.calcularHorasDeTareasDeUsuarios(usuariosAgregados, 5000d);

    model.put("usuarios", usuariosAgregados);
    return new ModelAndView("dashboard", model);
  }
}
