package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class ControladorUsuario {

  @Autowired
  private ServicioUsuario serviceUsuario;
}
