package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.excepcion.HogarNoEncontradoException;
import com.tallerwebi.dominio.excepcion.ParametroInvalidoException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioHogarTest {

  private RepositorioHogar repositorioHogarMock;
  private ServicioHogar servicio;

  @BeforeEach
  public void init() {
    repositorioHogarMock = mock(RepositorioHogar.class);
    servicio = new ServicioHogarImpl(repositorioHogarMock);
  }

  @Test
  public void debeActualizarLosParametrosDelHogar() {
    servicio.actualizarParametros(1500.0, 20000.0);

    Hogar hogar = servicio.obtenerHogar(); //verificacion
    assertThat(hogar.getPrecioHoraTarea(), equalTo(1500.0));
    assertThat(hogar.getPisoMinimoHogar(), equalTo(20000.0));
  }

  @Test
  public void noDebeActualizarLosParametrosConValoresNegativos() {
    assertThrows(
      ParametroInvalidoException.class,
      () -> servicio.actualizarParametros(-1.0, 20000.0)
    );
  }

  @Test
  public void noDebeActualizarLosParametrosConValoresVacios() {
    //si le paso alguno de los 2 parametros ennullo lanza esta excepcion
    assertThrows(
      ParametroInvalidoException.class,
      () -> servicio.actualizarParametros(null, 13000.0)
    );
    assertThrows(
      ParametroInvalidoException.class,
      () -> servicio.actualizarParametros(1200.0, null)
    );
  }

  @Test
  public void elHistorialDebeEstarVacioSiNoSecambioNingunParametro() {
    assertThat(servicio.obtenerHistorial(), hasSize(0));
  }

  @Test //guarda elregistro de todo
  public void debeRegistrarEnElHistorialElValorAnteriorYElNuevo() {
    servicio.actualizarParametros(1000.0, 0.0);

    servicio.actualizarParametros(1500.0, 0.0);

    List<CambioParametro> historial = servicio.obtenerHistorial();

    assertEquals(2, historial.size());
    assertEquals(1000.0, historial.get(0).getValorAnterior());
    assertEquals(1500.0, historial.get(0).getValorNuevo());
  }

  @Test
  public void debeAlternarEntreHogares() {
    Hogar hogar2 = new Hogar("hogar2");
    hogar2.setId(2L);
    servicio.registrarHogar(hogar2);

    servicio.seleccionarHogar(2L);
    assertThat(servicio.obtenerHogar().getNombreHogar(), equalTo("hogar2"));
  }

  @Test
  public void noDebeSeleccionarUnHogarQueNoExiste() {
    assertThrows(HogarNoEncontradoException.class, () -> servicio.seleccionarHogar(99L)); //error de ejhemplo
  }

  @Test
  public void cadaHogarDebeTenerSusPropiosParametrosEHistorial() {
    Hogar hogar2 = new Hogar("hogar2");
    hogar2.setId(2L);
    servicio.registrarHogar(hogar2);
    servicio.actualizarParametros(1500.00, 2000.00);

    servicio.seleccionarHogar(2L);

    assertThat(servicio.obtenerHogar().getPrecioHoraTarea(), equalTo(0.0));
    assertTrue(servicio.obtenerHistorial().isEmpty());
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
