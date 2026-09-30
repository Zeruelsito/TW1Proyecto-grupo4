package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tallerwebi.dominio.excepcion.HogarNoEncontradoException;
import com.tallerwebi.dominio.excepcion.ParametroInvalidoException;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ServicioHogarTest {

  @Test
  public void debeActualizarLosParametrosDelHogar() {
    ServicioHogar servicio = new ServicioHogarImpl();

    servicio.actualizarParametros(1500.0, 20000.0);

    Hogar hogar = servicio.obtenerHogar(); //verificacion
    assertThat(hogar.getPrecioHoraTarea(), equalTo(1500.0));
    assertThat(hogar.getPisoMinimoHogar(), equalTo(20000.0));
  }

  @Test
  public void noDebeActualizarLosParametrosConValoresNegativos() {
    ServicioHogar servicio = new ServicioHogarImpl();

    assertThrows(
      ParametroInvalidoException.class,
      () -> servicio.actualizarParametros(-1.0, 20000.0)
    );
  }

  @Test
  public void noDebeActualizarLosParametrosConValoresVacios() {
    ServicioHogar servicio = new ServicioHogarImpl();

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
    ServicioHogar servicio = new ServicioHogarImpl();

    assertThat(servicio.obtenerHistorial(), hasSize(0));
  }

  @Test //guarda elregistro de todo
  public void debeRegistrarEnElHistorialElValorAnteriorYElNuevo() {
    ServicioHogar servicio = new ServicioHogarImpl();
    servicio.actualizarParametros(1000.0, 0.0);

    servicio.actualizarParametros(1500.0, 0.0);

    List<CambioParametro> historial = servicio.obtenerHistorial();

    assertEquals(2, historial.size());
    assertEquals(1000.0, historial.get(0).getValorAnterior());
    assertEquals(1500.0, historial.get(0).getValorNuevo());
  }

  @Test
  public void debeAlternarEntreHogares() {
    ServicioHogar servicio = new ServicioHogarImpl();
    Hogar hogar2 = new Hogar("hogar2");
    hogar2.setId(2L);
    servicio.registrarHogar(hogar2);

    servicio.seleccionarHogar(2L);
    assertThat(servicio.obtenerHogar().getNombreHogar(), equalTo("hogar2"));
  }

  @Test
  public void noDebeSeleccionarUnHogarQueNoExiste() {
    ServicioHogar servicio = new ServicioHogarImpl();

    assertThrows(HogarNoEncontradoException.class, () -> servicio.seleccionarHogar(99L)); //error de ejhemplo
  }

  @Test
  public void cadaHogarDebeTenerSusPropiosParametrosEHistorial() {
    ServicioHogar servicio = new ServicioHogarImpl();
    Hogar hogar2 = new Hogar("hogar2");
    hogar2.setId(2L);
    servicio.registrarHogar(hogar2);
    servicio.actualizarParametros(1500.00, 2000.00);

    servicio.seleccionarHogar(2L);

    assertThat(servicio.obtenerHogar().getPrecioHoraTarea(), equalTo(0.0));
    assertTrue(servicio.obtenerHistorial().isEmpty());
  }
}
