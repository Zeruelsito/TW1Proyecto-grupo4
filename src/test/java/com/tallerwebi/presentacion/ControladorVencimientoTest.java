package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.ServicioVencimiento;
import com.tallerwebi.dominio.Vencimiento;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorVencimientoTest {

  private ServicioVencimiento servicioVencimiento;
  private ControladorVencimiento controladorVencimiento;

  @BeforeEach
  public void init() {
    servicioVencimiento = mock(ServicioVencimiento.class);
    controladorVencimiento = new ControladorVencimiento(servicioVencimiento);
  }

  @Test
  public void dadoQueExistenVencimientosCuandoConsultoLaVistaEntoncesDevuelveLaVistaConLosVencimientos() {
    // given
    List<Vencimiento> vencimientos = givenExistenVencimientosProximos();

    // when
    ModelAndView mav = whenIrAVencimientos();

    // then
    thenLaVistaEs("vencimientos", mav);
    thenElModeloContieneLosVencimientos(vencimientos, mav);
  }

  @Test
  public void dadoQueNoExistenVencimientosCuandoConsultoLaVistaEntoncesDevuelveLaVistaConUnaListaVacia() {
    // given
    List<Vencimiento> vencimientos = givenNoExistenVencimientosProximos();

    // when
    ModelAndView mav = whenIrAVencimientos();

    // then
    thenLaVistaEs("vencimientos", mav);
    thenElModeloContieneLosVencimientos(vencimientos, mav);
  }

  private List<Vencimiento> givenExistenVencimientosProximos() {
    List<Vencimiento> vencimientos = new ArrayList<>();
    vencimientos.add(new Vencimiento(1L, "Luz", LocalDate.now().plusDays(3)));
    vencimientos.add(new Vencimiento(2L, "Agua", LocalDate.now().plusDays(7)));

    when(servicioVencimiento.obtenerProximos(any(LocalDate.class))).thenReturn(vencimientos);
    return vencimientos;
  }

  private List<Vencimiento> givenNoExistenVencimientosProximos() {
    List<Vencimiento> vencimientos = new ArrayList<>();
    when(servicioVencimiento.obtenerProximos(any(LocalDate.class))).thenReturn(vencimientos);
    return vencimientos;
  }

  private ModelAndView whenIrAVencimientos() {
    return controladorVencimiento.irAVencimientos();
  }

  private void thenLaVistaEs(String vistaEsperada, ModelAndView mav) {
    assertThat(mav.getViewName(), equalTo(vistaEsperada));
  }

  private void thenElModeloContieneLosVencimientos(
    List<Vencimiento> vencimientosEsperados,
    ModelAndView mav
  ) {
    assertThat(mav.getModel().get("vencimientos"), equalTo(vencimientosEsperados));
  }
}
