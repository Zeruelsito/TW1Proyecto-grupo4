package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServiceCalculosGasto;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControllerDashboard {

  @Autowired
  private ServiceCalculosGasto serviceGasto;

  @GetMapping("/dashboard")
  public String verDashboard() {
    return "dashboard";
  }

  @PostMapping("/dashboard/enviarDato")
  public ModelAndView procesarDato(@RequestParam("montoValor") double valorHora) {
    double resultado = serviceGasto.establecerValorHora(valorHora);
    Map<String, Object> model = new ModelMap();
    model.put("resultadoValorHora", resultado);
    return new ModelAndView("dashboard", model);
  }
}