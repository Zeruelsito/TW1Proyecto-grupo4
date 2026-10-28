package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
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
  private ServicioHogarImpl servicioHogar;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void queLanceExcepcionSiParametrosSonNulosNegativosEnActualizarParametros() {
    assertThrows(
      ParametroInvalidoException.class,
      () -> {
        servicioHogar.actualizarParametros(null, 100.0);
      }
    );

    assertThrows(
      ParametroInvalidoException.class,
      () -> {
        servicioHogar.actualizarParametros(50.0, null);
      }
    );

    assertThrows(
      ParametroInvalidoException.class,
      () -> {
        servicioHogar.actualizarParametros(-10.0, 100.0);
      }
    );

    assertThrows(
      ParametroInvalidoException.class,
      () -> {
        servicioHogar.actualizarParametros(50.0, -5.0);
      }
    );
  }

  @Test
  public void queActualiceParametrosYAgregueAlHistorialSiSonDiferentes() {
    // Inicializamos los valores base del hogar por defecto
    Hogar hogarActual = servicioHogar.obtenerHogares(1L).stream().findFirst().orElse(null); // o validamos directamente sobre el hogar interno

    // Actualizamos con nuevos valores
    servicioHogar.actualizarParametros(150.0, 500.0);

    List<CambioParametro> historial = servicioHogar.obtenerHistorial();

    assertFalse(historial.isEmpty());
    assertEquals(2, historial.size()); // Un cambio por el precio de hora y otro por el piso mínimo
  }

  @Test
  public void queSeleccioneHogarPorIdExitosamente() throws Exception {
    Hogar hogarEsperado = new Hogar("Casa de Prueba");
    when(repositorioHogarMock.buscarPorId(1L)).thenReturn(hogarEsperado);

    Hogar hogarObtenido = servicioHogar.seleccionarHogarPorId(1L);

    assertNotNull(hogarObtenido);
    assertEquals("Casa de Prueba", hogarObtenido.getNombreHogar());
    verify(repositorioHogarMock, times(1)).buscarPorId(1L);
  }

  @Test
  public void queCreeHogarCorrectamente() {
    Hogar nuevoHogar = new Hogar("Nuevo Hogar");

    doNothing().when(repositorioHogarMock).guardar(nuevoHogar);

    servicioHogar.crearHogar(nuevoHogar);

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

    boolean resultado = servicioHogar.unirseAHogar("hogar dulce hogar", "1234", usuario);

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

    boolean resultado = servicioHogar.unirseAHogar("Hogar Dulce Hogar", "9999", usuario);

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

    boolean resultado = servicioHogar.unirseAHogar("Hogar Dulce Hogar", "1234", usuario);

    assertTrue(resultado);
    assertEquals(1, hogar.getIntegrantes().size());
    // Como ya era integrante, no debería llamar a actualizar
    verify(repositorioHogarMock, never()).actualizar(any());
  }
}
