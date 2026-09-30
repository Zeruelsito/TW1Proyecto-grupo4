package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.CambioParametro;
import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.ServicioHogar;
import com.tallerwebi.dominio.excepcion.HogarNoEncontradoException;
import com.tallerwebi.dominio.excepcion.ParametroInvalidoException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorHogarTest {

  private ServicioHogar servicioHogar;
  private ControladorHogar controladorHogar;

  @BeforeEach
  public void init() {
    servicioHogar = mock(ServicioHogar.class);
    controladorHogar = new ControladorHogar(servicioHogar);
  }

  @Test
  public void cuandoConsultoLaVistaEntoncesDevuelveElHogarLosHogaresYElHistorial() {
    // given
    Hogar hogar = givenExisteUnHogarActivo();
    List<Hogar> hogares = givenExistenHogares();
    List<CambioParametro> historial = givenExisteHistorial();

    // when
    ModelAndView mav = controladorHogar.irAHogar();

    // then
    thenLaVistaEs("hogar", mav);
    assertThat(mav.getModel().get("hogar"), equalTo(hogar));
    assertThat(mav.getModel().get("hogares"), equalTo(hogares));
    assertThat(mav.getModel().get("historial"), equalTo(historial));
  }

  @Test
  public void cuandoGuardoParametrosValidosEntoncesLlamaAlServicioYRedirigeAHogar() {
    // when
    ModelAndView mav = controladorHogar.guardarParametros(1500.0, 20000.0);

    // then
    verify(servicioHogar).actualizarParametros(1500.0, 20000.0);
    thenLaVistaEs("redirect:/hogar", mav);
  }

  @Test
  public void cuandoGuardoParametrosInvalidosEntoncesRedirigeAHogarSinFallar() {
    // given
    doThrow(new ParametroInvalidoException())
      .when(servicioHogar)
      .actualizarParametros(-1.0, 20000.0);

    // when
    ModelAndView mav = controladorHogar.guardarParametros(-1.0, 20000.0);

    // then
    thenLaVistaEs("redirect:/hogar", mav);
  }

  @Test
  public void cuandoCambioDeHogarEntoncesLlamaAlServicioYRedirigeAHogar() {
    // when
    ModelAndView mav = controladorHogar.cambiarHogar(2L);

    // then
    verify(servicioHogar).seleccionarHogar(2L);
    thenLaVistaEs("redirect:/hogar", mav);
  }

  @Test
  public void cuandoCambioAUnHogarQueNoExisteEntoncesRedirigeAHogarSinFallar() {
    // given
    doThrow(new HogarNoEncontradoException()).when(servicioHogar).seleccionarHogar(99L);

    // when
    ModelAndView mav = controladorHogar.cambiarHogar(99L);

    // then
    thenLaVistaEs("redirect:/hogar", mav);
  }

  private Hogar givenExisteUnHogarActivo() {
    Hogar hogar = new Hogar("Hogar 1");
    when(servicioHogar.obtenerHogar()).thenReturn(hogar);
    return hogar;
  }

  private List<Hogar> givenExistenHogares() {
    List<Hogar> hogares = new ArrayList<>();
    hogares.add(new Hogar("Hogar 2"));
    when(servicioHogar.obtenerHogares()).thenReturn(hogares);
    return hogares;
  }

  private List<CambioParametro> givenExisteHistorial() {
    List<CambioParametro> historial = new ArrayList<>();
    historial.add(new CambioParametro("Precio por hora", 1000.0, 1500.0));
    when(servicioHogar.obtenerHistorial()).thenReturn(historial);
    return historial;
  }

  private void thenLaVistaEs(String vistaEsperada, ModelAndView mav) {
    assertThat(mav.getViewName(), equalTo(vistaEsperada));
  }
}
