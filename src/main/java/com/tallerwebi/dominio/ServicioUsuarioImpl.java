package com.tallerwebi.dominio;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ServiceUsuario")
@Transactional
public class ServicioUsuarioImpl implements ServicioUsuario {

  private List<Usuario> usuariosMock = new ArrayList<Usuario>();
  private RepositorioUsuario repositorioUsuario;

  @Autowired
  public ServicioUsuarioImpl(RepositorioUsuario repositorioUsuario) {
    this.repositorioUsuario = repositorioUsuario;
  }

  public ServicioUsuarioImpl() {}

  @Override
  public void registrarUsuario(Usuario usuario) {
    usuariosMock.add(usuario);
  }

  @Override
  public List<Usuario> obtenerTodos() {
    return usuariosMock;
  }

  @Override
  public Usuario obtenerUsuarioPorId(Long id) {
    return repositorioUsuario.buscarPorId(id);
  }

  @Override
  @Transactional
  public Usuario actualizarPerfil(Long id, Usuario usuarioActualizado, String nuevaPassword)
    throws IOException {
    // 1. Buscamos el usuario existente en la base de datos
    Usuario usuarioDb = repositorioUsuario.buscarPorId(id);

    if (usuarioDb == null) {
      throw new RuntimeException("Usuario no encontrado con ID: " + id);
    }

    // 2. Actualizamos los campos editables
    usuarioDb.setNombre(usuarioActualizado.getNombre());
    usuarioDb.setEmail(usuarioActualizado.getEmail());
    usuarioDb.setIngresoMensual(usuarioActualizado.getIngresoMensual());

    // 3. Contraseña opcional
    if (nuevaPassword != null && !nuevaPassword.trim().isEmpty()) {
      usuarioDb.setPassword(nuevaPassword);
    }

    repositorioUsuario.modificar(usuarioDb);

    return usuarioDb;
  }
}
