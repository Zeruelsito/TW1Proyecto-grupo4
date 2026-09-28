package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Gasto;
import com.tallerwebi.dominio.ServicioRegistroGasto;
import com.tallerwebi.dominio.excepcion.DescripcionGastoVaciaException;
import com.tallerwebi.dominio.excepcion.MontoGastoInvalidoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorRegistroGasto {

  private final ServicioRegistroGasto servicioRegistroGasto;

  public ControladorRegistroGasto(ServicioRegistroGasto servicioRegistroGasto) {
    this.servicioRegistroGasto = servicioRegistroGasto;
  }

  @RequestMapping(path = "/gastos", method = RequestMethod.GET)
  public ModelAndView irAGastos() {
    ModelAndView mav = new ModelAndView("gastos");
    mav.addObject("listaGastos", servicioRegistroGasto.obtenerTodosLosGastos());
    mav.addObject("integrantes", servicioRegistroGasto.obtenerIntegrantes());
    mav.addObject(
      "sugerenciaProximoPagador",
      servicioRegistroGasto.obtenerSugerenciaProximoPagador()
    );
    mav.addObject("gasto", new Gasto());
    return mav;
  }

  @RequestMapping(path = "/gastos/guardar", method = RequestMethod.POST)
  public ModelAndView registrarGasto(@ModelAttribute("gasto") Gasto gasto) {
    ModelAndView mav = new ModelAndView();

    try {
      this.servicioRegistroGasto.registrarGasto(gasto);
      mav.setViewName("redirect:/gastos");
      return mav;
    } catch (DescripcionGastoVaciaException e) {
      mav.addObject("mensajeError", "La descripción del gasto no puede estar vacía.");
    } catch (MontoGastoInvalidoException e) {
      mav.addObject("mensajeError", "El monto del gasto debe ser mayor a cero.");
    }

    mav.setViewName("gastos");
    mav.addObject("listaGastos", servicioRegistroGasto.obtenerTodosLosGastos());
    mav.addObject("integrantes", servicioRegistroGasto.obtenerIntegrantes());
    mav.addObject(
      "sugerenciaProximoPagador",
      servicioRegistroGasto.obtenerSugerenciaProximoPagador()
    );
    mav.addObject("gasto", gasto);
    return mav;
  }
}
