package com.tallerwebi.dominio;

import java.io.IOException;
import java.util.List;

public interface ServicioUsuario {
  void registrarUsuario(Usuario usuario);
  List<Usuario> obtenerTodos();
  Usuario obtenerUsuarioPorId(Long id);
  Usuario actualizarPerfil(Long id, Usuario usuarioActualizado, String nuevaPassword)
    throws IOException;
}
