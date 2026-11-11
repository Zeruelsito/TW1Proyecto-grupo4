package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioUsuarioTest {

  private RepositorioUsuario repositorioUsuarioMock;
  private ServicioUsuarioImpl servicioUsuario;
  private List<Usuario> usuariosEnMemoria;

  @BeforeEach
  public void init() {
    repositorioUsuarioMock = mock(RepositorioUsuario.class);
    servicioUsuario = new ServicioUsuarioImpl(repositorioUsuarioMock);
    usuariosEnMemoria = new ArrayList<>();

    // Simulando comportamiento del repositorio en memoria
    when(repositorioUsuarioMock.buscarPorId(any(Long.class)))
      .thenAnswer(invocation -> {
        Long id = invocation.getArgument(0);
        return usuariosEnMemoria
          .stream()
          .filter(u -> u.getId().equals(id))
          .findFirst()
          .orElse(null);
      });

    doAnswer(invocation -> {
        Usuario u = invocation.getArgument(0);
        if (u.getId() == null) {
          u.setId((long) (usuariosEnMemoria.size() + 1));
        }
        if (!usuariosEnMemoria.contains(u)) {
          usuariosEnMemoria.add(u);
        }
        return null;
      })
      .when(repositorioUsuarioMock)
      .modificar(any(Usuario.class));
  }

  @Test
  public void dadoUnUsuarioКогдаLoRegistroEntoncesSeAgregaALaLista() {
    // Given
    Usuario usuario = new Usuario("Maria", "maria@kumo.com", "1234");
    usuario.setId(1L);

    // When (Acá faltaba invocar al servicio para que lo guarde en su lista interna)
    servicioUsuario.registrarUsuario(usuario);

    // Then
    List<Usuario> usuarios = servicioUsuario.obtenerTodos();
    assertThat(usuarios, hasSize(1));
    assertThat(usuarios.get(0).getNombre(), equalTo("Maria"));
  }

  @Test
  public void dadoUnIdCuandoBuscoUsuarioEntoncesRetornaElUsuarioDelRepositorio() {
    // Given
    Usuario usuario = new Usuario("Juan", "juan@kumo.com", "1234");
    usuario.setId(10L);
    usuariosEnMemoria.add(usuario);

    // When
    Usuario encontrado = servicioUsuario.obtenerUsuarioPorId(10L);

    // Then
    assertNotNull(encontrado);
    assertThat(encontrado.getNombre(), equalTo("Juan"));
  }

  @Test
  public void dadoUnIdYDatosNuevosCuandoActualizoPerfilEntoncesModificaYRetornaUsuario()
    throws IOException {
    // Given
    Usuario usuario = new Usuario("Maria", "maria@kumo.com", "1234");
    usuario.setId(1L);
    usuariosEnMemoria.add(usuario);

    Usuario usuarioActualizado = new Usuario("Maria Actualizada", "maria.nueva@kumo.com", "1234");
    usuarioActualizado.setIngresoMensual(70000.0);

    // When
    Usuario resultado = servicioUsuario.actualizarPerfil(1L, usuarioActualizado, "nuevaPass123");

    // Then
    assertNotNull(resultado);
    assertThat(resultado.getNombre(), equalTo("Maria Actualizada"));
    assertThat(resultado.getIngresoMensual(), equalTo(70000.0));
    assertThat(resultado.getPassword(), equalTo("nuevaPass123"));
    verify(repositorioUsuarioMock, times(1)).modificar(any(Usuario.class));
  }

  @Test
  public void dadoUnIdInexistenteCuandoActualizoPerfilEntoncesLanzaExcepcion() {
    // Given
    Usuario usuarioActualizado = new Usuario("Carlos", "carlos@kumo.com", "1234");

    // Then
    assertThrows(
      RuntimeException.class,
      () -> servicioUsuario.actualizarPerfil(999L, usuarioActualizado, "1234")
    );
    verify(repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }
}
