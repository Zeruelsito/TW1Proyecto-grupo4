package com.tallerwebi.dominio;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service("ServiceUsuario")
@Transactional
public class ServicioUsuarioImpl implements ServicioUsuario {

  private List<Usuario> usuariosMock = new ArrayList<Usuario>();

  @Override
  public void registrarUsuario(Usuario usuario) {
    usuariosMock.add(usuario);
  }

  @Override
  public List<Usuario> obtenerTodos() {
    return usuariosMock;
  }
}
