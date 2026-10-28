package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tallerwebi.dominio.CambioParametro;
import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.ServicioHogar;
import com.tallerwebi.dominio.excepcion.HogarNoEncontradoException;
import com.tallerwebi.dominio.excepcion.ParametroInvalidoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

public class ControladorHogarTest {

  private MockMvc mockMvc;
  private ServicioHogar servicioHogarMock;
  private ControladorHogar controladorHogar;

  @BeforeEach
  public void setUp() {
    servicioHogarMock = mock(ServicioHogar.class);
    controladorHogar = new ControladorHogar(servicioHogarMock);

    // Configuramos el ViewResolver para evitar el bucle de vista circular en Spring MVC
    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setPrefix("/WEB-INF/views/");
    viewResolver.setSuffix(".jsp");

    mockMvc =
      MockMvcBuilders.standaloneSetup(controladorHogar).setViewResolvers(viewResolver).build();
  }

  @Test
  public void queMuestreLaVistaHogaresYSeleccioneHogarPorDefectoSiNoEstaEnSesion()
    throws Exception {
    Hogar hogarMock = new Hogar("Hogar Principal");
    List<Hogar> listaHogares = Collections.singletonList(hogarMock);

    when(servicioHogarMock.obtenerHogares(1L)).thenReturn(listaHogares);
    when(servicioHogarMock.obtenerHistorial()).thenReturn(Collections.emptyList());

    mockMvc
      .perform(get("/hogar"))
      .andExpect(status().isOk())
      .andExpect(view().name("hogar"))
      .andExpect(model().attributeExists("hogar"))
      .andExpect(model().attributeExists("hogares"))
      .andExpect(model().attributeExists("historial"));

    verify(servicioHogarMock, atLeastOnce()).obtenerHogares(1L);
  }

  @Test
  public void queGuardeParametrosExitosamenteYRedirija() throws Exception {
    doNothing().when(servicioHogarMock).actualizarParametros(150.0, 500.0);

    mockMvc
      .perform(
        post("/hogar/parametros")
          .param("precioHoraTarea", "150.0")
          .param("pisoMinimoHogar", "500.0")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/hogar"));

    verify(servicioHogarMock, times(1)).actualizarParametros(150.0, 500.0);
  }

  @Test
  public void queRedirijaAunSiLanzaExcepcionParametroInvalidoAlGuardar() throws Exception {
    doThrow(new ParametroInvalidoException())
      .when(servicioHogarMock)
      .actualizarParametros(anyDouble(), anyDouble());

    mockMvc
      .perform(
        post("/hogar/parametros")
          .param("precioHoraTarea", "-10.0")
          .param("pisoMinimoHogar", "500.0")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/hogar"));
  }

  @Test
  public void queCambieDeHogarExitosamenteYRedirija() throws Exception {
    Hogar hogarNuevo = new Hogar("Casa de Campo");
    when(servicioHogarMock.seleccionarHogarPorId(2L)).thenReturn(hogarNuevo);

    mockMvc
      .perform(post("/hogar/cambiar").param("hogarId", "2"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/hogar"));

    verify(servicioHogarMock, times(2)).seleccionarHogarPorId(2L);
  }

  @Test
  public void queManejeExcepcionHogarNoEncontradoAlCambiarDeHogar() throws Exception {
    doThrow(new HogarNoEncontradoException()).when(servicioHogarMock).seleccionarHogarPorId(99L);

    mockMvc
      .perform(post("/hogar/cambiar").param("hogarId", "99"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/hogar"));
  }
}
