package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorLiquidacion {

  private final ServicioLiquidacion servicioLiquidacion;

  @Autowired
  public ControladorLiquidacion(ServicioLiquidacion servicioLiquidacion) {
    this.servicioLiquidacion = servicioLiquidacion;
  }

  @GetMapping("/liquidacion")
  public ModelAndView irALiquidacion() {
    Map<String, Object> modelo = new HashMap<>();

    Hogar hogar = new Hogar("Hogar Compartido");

    Usuario juan = new Usuario();
    juan.setId(1L);
    juan.setNombre("Juan");

    Usuario maria = new Usuario();
    maria.setId(2L);
    maria.setNombre("Maria");

    hogar.getIntegrantes().add(juan);
    hogar.getIntegrantes().add(maria);

    List<Gasto> gastos = new ArrayList<>();
    Gasto gastoUno = new Gasto();
    gastoUno.setMonto(10000.0);
    gastoUno.setPagadorId(1L);
    gastoUno.setDescripcion("Supermercado");
    gastos.add(gastoUno);

    List<TransferenciaSugerida> transferencias = servicioLiquidacion.obtenerLiquidacionDelMes(hogar,gastos);

    modelo.put("transferencias", transferencias);
    modelo.put("hogar", hogar);
    modelo.put("usuarioLogueado", "Juan");

    return new ModelAndView("liquidacion", modelo);
  }
}
