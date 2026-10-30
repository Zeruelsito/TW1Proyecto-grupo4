package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ControladorLiquidacionTest {

    private MockMvc mockMvc;

    @Mock
    private HttpSession session;

    @Mock
    private ServicioLiquidacion servicioLiquidacion;

    @Mock
    private ServicioRegistroGasto servicioRegistroGasto;

    @Mock
    private ServicioPagoPendiente servicioPagoPendiente;

    @Mock
    private ServicioHogar servicioHogar;

    @Mock
    private ServicioUsuario servicioUsuario;

    @Mock
    private ServicioPagoRealizado servicioPagoRealizado;

    @InjectMocks
    private ControladorLiquidacion controladorLiquidacion;

    private AutoCloseable closeable;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Configuramos un ViewResolver falso para evitar el error de vista circular
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");

        mockMvc = MockMvcBuilders.standaloneSetup(controladorLiquidacion)
                .setViewResolvers(viewResolver)
                .build();
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void queAlAccederALiquidacionSinHogarActivoDevuelvaLaVistaConDatosBasicos() throws Exception {
        Long usuarioId = 1L;
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(usuarioId);
        usuarioMock.setNombre("Ezequiel");

        when(servicioUsuario.obtenerUsuarioPorId(eq(usuarioId))).thenReturn(usuarioMock);
        when(servicioHogar.obtenerHogares(eq(usuarioId))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/liquidacion")
                        .sessionAttr("USUARIO_ID", usuarioId))
                .andExpect(status().isOk())
                .andExpect(view().name("liquidacion"))
                .andExpect(model().attribute("usuarioLogueado", "Ezequiel"))
                .andExpect(model().attribute("listaHogares", hasSize(0)));
    }

    @Test
    public void queAlAccederALiquidacionConHogarActivoCargueGastosYTransferencias() throws Exception {
        Long usuarioId = 1L;
        Long hogarId = 10L;

        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(usuarioId);
        usuarioMock.setNombre("Ezequiel");

        Hogar hogarMock = crearHogarConId(hogarId);

        when(servicioUsuario.obtenerUsuarioPorId(eq(usuarioId))).thenReturn(usuarioMock);
        when(servicioHogar.obtenerHogares(eq(usuarioId))).thenReturn(List.of(hogarMock));
        when(servicioHogar.seleccionarHogarPorId(eq(hogarId))).thenReturn(hogarMock);

        when(servicioRegistroGasto.obtenerTodosLosGastos(eq(hogarId))).thenReturn(Collections.emptyList());
        when(servicioPagoPendiente.obtenerPendientes(any(Hogar.class))).thenReturn(Collections.emptyList());
        when(servicioLiquidacion.obtenerLiquidacionDelMes(any(Hogar.class), any())).thenReturn(Collections.emptyList());
        when(servicioPagoRealizado.obtenerHistorialPagos(eq(hogarId))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/liquidacion")
                        .sessionAttr("USUARIO_ID", usuarioId)
                        .sessionAttr("hogarActivoEnSession", hogarMock))
                .andExpect(status().isOk())
                .andExpect(view().name("liquidacion"))
                .andExpect(model().attribute("usuarioLogueado", "Ezequiel"))
                .andExpect(model().attribute("casa", notNullValue()))
                .andExpect(model().attributeExists("transferenciasPendientes", "historialPagos"));
    }

    private Hogar crearHogarConId(Long id) {
        Hogar hogar = new Hogar();
        hogar.setId(id);
        return hogar;
    }
}