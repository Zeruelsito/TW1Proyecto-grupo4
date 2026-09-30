package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioUsuario {
  void registrarUsuario(Usuario usuario);
  List<Usuario> obtenerTodos();
}
