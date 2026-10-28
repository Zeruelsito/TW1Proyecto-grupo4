package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.ParametroInvalidoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ServicioHogarTest {

  @Mock
  private RepositorioHogar repositorioHogarMock;

  @InjectMocks
  private ServicioHogarImpl servicio; // Esta es la instancia que usa el mock inyectado

  @BeforeEach
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void debeActualizarLosParametrosDelHogar() {
    servicio.actualizarParametros(1500.0, 20000.0);
  }

  @Test
  public void queLanceExcepcionSiParametrosSonNulosNegativosEnActualizarParametros() {
    assertThrows(
      ParametroInvalidoException.class,
      () -> {
        servicio.actualizarParametros(null, 100.0);
      }
    );

    assertThrows(
      ParametroInvalidoException.class,
      () -> {
        servicio.actualizarParametros(50.0, null);
      }
    );

    assertThrows(
      ParametroInvalidoException.class,
      () -> {
        servicio.actualizarParametros(-10.0, 100.0);
      }
    );

    assertThrows(
      ParametroInvalidoException.class,
      () -> {
        servicio.actualizarParametros(50.0, -5.0);
      }
    );
  }

  @Test
  public void queActualiceParametrosYAgregueAlHistorialSiSonDiferentes() {
    // Inicializamos los valores base del hogar por defecto
    Hogar hogarActual = servicio.obtenerHogares(1L).stream().findFirst().orElse(null); // o validamos directamente sobre el hogar interno

    // Actualizamos con nuevos valores
    servicio.actualizarParametros(150.0, 500.0);

    List<CambioParametro> historial = servicio.obtenerHistorial();

    assertFalse(historial.isEmpty());
    assertEquals(2, historial.size()); // Un cambio por el precio de hora y otro por el piso mínimo
  }

  @Test
  public void queSeleccioneHogarPorIdExitosamente() throws Exception {
    Hogar hogarEsperado = new Hogar("Casa de Prueba");
    when(repositorioHogarMock.buscarPorId(1L)).thenReturn(hogarEsperado);

    Hogar hogarObtenido = servicio.seleccionarHogarPorId(1L);

    assertNotNull(hogarObtenido);
    assertEquals("Casa de Prueba", hogarObtenido.getNombreHogar());
    verify(repositorioHogarMock, times(1)).buscarPorId(1L);
  }

  @Test
  public void queCreeHogarCorrectamente() {
    Hogar nuevoHogar = new Hogar("Nuevo Hogar");

    doNothing().when(repositorioHogarMock).guardar(nuevoHogar);

    servicio.crearHogar(nuevoHogar);

    verify(repositorioHogarMock, times(1)).guardar(nuevoHogar);
  }

  @Test
  public void queUnUsuarioSePuedaUnirAHogarExitosamente() {
    Hogar hogar = new Hogar("Hogar Dulce Hogar");
    hogar.setPin("1234");
    hogar.setIntegrantes(new ArrayList<>());

    Usuario usuario = new Usuario();
    usuario.setId(1L);

    when(repositorioHogarMock.obtenerTodos()).thenReturn(Arrays.asList(hogar));

    boolean resultado = servicio.unirseAHogar("hogar dulce hogar", "1234", usuario);

    assertTrue(resultado);
    assertTrue(hogar.getIntegrantes().contains(usuario));
    verify(repositorioHogarMock, times(1)).actualizar(hogar);
  }

  @Test
  public void queFalleAlUnirseAHogarSiPinEsIncorrecto() {
    Hogar hogar = new Hogar("Hogar Dulce Hogar");
    hogar.setPin("1234");
    hogar.setIntegrantes(new ArrayList<>());

    Usuario usuario = new Usuario();
    usuario.setId(1L);

    when(repositorioHogarMock.obtenerTodos()).thenReturn(Arrays.asList(hogar));

    boolean resultado = servicio.unirseAHogar("Hogar Dulce Hogar", "9999", usuario);

    assertFalse(resultado);
    assertTrue(hogar.getIntegrantes().isEmpty());
    verify(repositorioHogarMock, never()).actualizar(any());
  }

  @Test
  public void queNoDupliqueIntegranteSiYaPerteneceAlHogar() {
    Usuario usuario = new Usuario();
    usuario.setId(1L);

    Hogar hogar = new Hogar("Hogar Dulce Hogar");
    hogar.setPin("1234");
    hogar.getIntegrantes().add(usuario); // Ya es integrante

    when(repositorioHogarMock.obtenerTodos()).thenReturn(Arrays.asList(hogar));

    boolean resultado = servicio.unirseAHogar("Hogar Dulce Hogar", "1234", usuario);

    assertTrue(resultado);
    assertEquals(1, hogar.getIntegrantes().size());
    // Como ya era integrante, no debería llamar a actualizar
    verify(repositorioHogarMock, never()).actualizar(any());
  }

  @Test
  public void debeGuardarElHogarAlCrearlo() {
    Hogar hogar = new Hogar("Casa");

    servicio.crearHogar(hogar);

    verify(repositorioHogarMock).guardar(hogar);
  }

  @Test
  public void debeObtenerLosHogaresDeUnUsuario() {
    Hogar hogar = new Hogar("Casa");
    when(repositorioHogarMock.obtenerHogaresDeUsuario(1L)).thenReturn(List.of(hogar));

    List<Hogar> hogares = servicio.obtenerHogares(1L);

    assertThat(hogares, hasSize(1));
    assertThat(hogares.get(0).getNombreHogar(), equalTo("Casa"));
  }

  @Test
  public void debeUnirseAUnHogarConNombreYPinCorrectos() {
    Hogar hogar = new Hogar("Casa", "1234");
    Usuario usuario = new Usuario();
    usuario.setId(11L);
    when(repositorioHogarMock.obtenerTodos()).thenReturn(List.of(hogar));

    boolean seUnio = servicio.unirseAHogar("casa", "1234", usuario); //el nombre ignora mayusculas

    assertTrue(seUnio);
    assertThat(hogar.getIntegrantes(), hasSize(1));
  }

  @Test
  public void noDebeAgregarDosVecesAlMismoIntegrante() {
    Hogar hogar = new Hogar("Casa", "1234");
    Usuario usuario = new Usuario();
    usuario.setId(11L);
    hogar.getIntegrantes().add(usuario);
    when(repositorioHogarMock.obtenerTodos()).thenReturn(List.of(hogar));

    boolean seUnio = servicio.unirseAHogar("Casa", "1234", usuario);

    assertTrue(seUnio);
    assertThat(hogar.getIntegrantes(), hasSize(1));
  }

  @Test
  public void noDebeUnirseAUnHogarConPinIncorrecto() {
    Hogar hogar = new Hogar("Casa", "1234");
    Usuario usuario = new Usuario();
    usuario.setId(11L);
    when(repositorioHogarMock.obtenerTodos()).thenReturn(List.of(hogar));

    boolean seUnio = servicio.unirseAHogar("Casa", "0000", usuario);

    assertFalse(seUnio);
    assertThat(hogar.getIntegrantes(), hasSize(0));
  }

  @Test
  public void noDebeUnirseAUnHogarQueNoExiste() {
    Usuario usuario = new Usuario();
    usuario.setId(11L);

    boolean seUnio = servicio.unirseAHogar("casita", "1234", usuario);

    assertFalse(seUnio);
  }
}
