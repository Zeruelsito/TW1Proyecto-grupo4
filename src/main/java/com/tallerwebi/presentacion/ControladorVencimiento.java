package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioVencimiento;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorVencimiento {

  private final ServicioVencimiento servicioVencimiento;

  @Autowired
  public ControladorVencimiento(ServicioVencimiento servicioVencimiento) {
    this.servicioVencimiento = servicioVencimiento;
  }

  @GetMapping("/vencimientos")
  public ModelAndView irAVencimientos() {
    ModelAndView mav = new ModelAndView("vencimientos");
    mav.addObject("vencimientos", servicioVencimiento.obtenerProximos(LocalDate.now()));
    return mav;
  }
}
