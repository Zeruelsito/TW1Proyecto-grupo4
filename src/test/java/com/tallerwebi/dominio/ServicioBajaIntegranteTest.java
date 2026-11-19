package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.HogarSinIntegrantesException;
import com.tallerwebi.dominio.excepcion.IntegranteConSaldoPendienteException;
import com.tallerwebi.dominio.excepcion.PagoNoCorrespondeALiquidacionException;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.excepcion.UsuarioYaDadoDeBajaException;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ServicioBajaIntegranteTest {

  private RepositorioUsuario repositorioUsuarioMock;
  private RepositorioGasto repositorioGastoMock;
  private RepositorioHogar repositorioHogarMock;
  private ServicioCalculoBaja servicioCalculoBajaMock;
  private ServicioHogar servicioHogarMock;
  private ServicioPagoRealizado servicioPagoRealizadoMock;
  private ServicioPagoPendiente servicioPagoPendienteMock;
  private HttpSession sessionMock;

  private ServicioBajaIntegranteImpl servicioBajaIntegrante;

  private Hogar hogarTest;
  private Usuario usuario1;
  private Usuario usuario2;

  @BeforeEach
  public void setUp() {
    repositorioUsuarioMock = Mockito.mock(RepositorioUsuario.class);
    repositorioGastoMock = Mockito.mock(RepositorioGasto.class);
    repositorioHogarMock = Mockito.mock(RepositorioHogar.class);
    servicioCalculoBajaMock = Mockito.mock(ServicioCalculoBaja.class);
    servicioHogarMock = Mockito.mock(ServicioHogar.class);
    servicioPagoRealizadoMock = Mockito.mock(ServicioPagoRealizado.class);
    servicioPagoPendienteMock = Mockito.mock(ServicioPagoPendiente.class);
    sessionMock = Mockito.mock(HttpSession.class);

    servicioBajaIntegrante =
      new ServicioBajaIntegranteImpl(
        repositorioUsuarioMock,
        repositorioGastoMock,
        repositorioHogarMock,
        servicioCalculoBajaMock,
        servicioHogarMock,
        servicioPagoRealizadoMock,
        servicioPagoPendienteMock,
        sessionMock
      );

    usuario1 = new Usuario();
    usuario1.setId(1L);
    usuario1.setNombre("Carlos");
    usuario1.setIngresoMensual(50000.0);

    usuario2 = new Usuario();
    usuario2.setId(2L);
    usuario2.setNombre("Juan");
    usuario2.setIngresoMensual(60000.0);

    hogarTest = new Hogar();
    hogarTest.setId(10L);
    hogarTest.setPisoMinimoHogar(1000.0);
    hogarTest.setPrecioHoraTarea(100.0);
    hogarTest.setIntegrantes(new ArrayList<>(Arrays.asList(usuario1, usuario2)));

    when(sessionMock.getAttribute("HOGAR_ID")).thenReturn(10L);
    when(servicioHogarMock.seleccionarHogarPorId(10L)).thenReturn(hogarTest);
    when(servicioPagoPendienteMock.obtenerPendientes(any())).thenReturn(Collections.emptyList());
    when(servicioCalculoBajaMock.calcularDeudaHorasTarea(any(), any(), anyList()))
      .thenReturn(new double[] { 0.0, 0.0 });
    when(servicioCalculoBajaMock.liquidacionDelSaliente(anyString(), any()))
      .thenReturn(Collections.emptyList());
    when(
      servicioCalculoBajaMock.armarFilasCuota(
        anyList(),
        anyList(),
        anyDouble(),
        any(),
        anyBoolean()
      )
    )
      .thenReturn(Collections.emptyList());
  }

  @Test
  public void queLanceExcepcionSiUsuarioNoExisteAlSimularBaja() {
    when(repositorioUsuarioMock.buscarPorId(99L)).thenReturn(null);

    assertThrows(UsuarioNoEncontrado.class, () -> servicioBajaIntegrante.simularBaja(99L));
  }

  @Test
  public void queLanceExcepcionSiUsuarioYaEstaDeBaja() {
    usuario1.setFechaBaja(LocalDateTime.now().minusDays(5));
    when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(usuario1);

    assertThrows(UsuarioYaDadoDeBajaException.class, () -> servicioBajaIntegrante.simularBaja(1L));
  }

  @Test
  public void queLanceExcepcionSiElHogarSeQuedaSinIntegrantesAlDarDeBaja() {
    when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(usuario1);
    when(repositorioUsuarioMock.obtenerIntegrantesDelHogar(10L))
      .thenReturn(Arrays.asList(usuario1));

    assertThrows(HogarSinIntegrantesException.class, () -> servicioBajaIntegrante.simularBaja(1L));
  }

  @Test
  public void queSimuleBajaCorrectamenteSinDeudaPendiente() {
    when(repositorioUsuarioMock.buscarPorId(2L)).thenReturn(usuario2);
    when(repositorioUsuarioMock.obtenerIntegrantesDelHogar(10L))
      .thenReturn(Arrays.asList(usuario1, usuario2));
    when(repositorioGastoMock.obtenerTodosPorHogar(10L)).thenReturn(Collections.emptyList());
    Map<String, Double> balances = new LinkedHashMap<>();
    balances.put("Carlos", 0.0);
    balances.put("Juan", 0.0);
    when(
      servicioCalculoBajaMock.calcularBalancesConBaja(
        anyList(),
        anyList(),
        any(),
        anyList(),
        anyDouble(),
        anyLong()
      )
    )
      .thenReturn(balances);

    SimulacionBaja simulacion = servicioBajaIntegrante.simularBaja(2L);

    assertThat(simulacion, notNullValue());
    assertThat(simulacion.getUsuarioSaliente(), is(usuario2));
    assertThat(simulacion.isSaldoPendiente(), is(false));
  }

  @Test
  public void queLanceExcepcionSiSeIntentaConfirmarBajaConSaldoPendiente() {
    when(repositorioUsuarioMock.buscarPorId(2L)).thenReturn(usuario2);
    when(repositorioUsuarioMock.obtenerIntegrantesDelHogar(10L))
      .thenReturn(Arrays.asList(usuario1, usuario2));
    when(repositorioGastoMock.obtenerTodosPorHogar(10L)).thenReturn(Collections.emptyList());
    Map<String, Double> balances = new LinkedHashMap<>();
    balances.put("Carlos", 500.0);
    balances.put("Juan", -500.0);
    when(
      servicioCalculoBajaMock.calcularBalancesConBaja(
        anyList(),
        anyList(),
        any(),
        anyList(),
        anyDouble(),
        anyLong()
      )
    )
      .thenReturn(balances);

    assertThrows(
      IntegranteConSaldoPendienteException.class,
      () -> servicioBajaIntegrante.confirmarBaja(2L)
    );
  }

  @Test
  public void queConfirmeBajaExitosamenteSiNoHaySaldoPendiente() {
    when(repositorioUsuarioMock.buscarPorId(2L)).thenReturn(usuario2);
    when(repositorioUsuarioMock.obtenerIntegrantesDelHogar(10L))
      .thenReturn(Arrays.asList(usuario1, usuario2));
    when(repositorioGastoMock.obtenerTodosPorHogar(10L)).thenReturn(Collections.emptyList());
    Map<String, Double> balances = new LinkedHashMap<>();
    balances.put("Carlos", 0.0);
    balances.put("Juan", 0.0);
    when(
      servicioCalculoBajaMock.calcularBalancesConBaja(
        anyList(),
        anyList(),
        any(),
        anyList(),
        anyDouble(),
        anyLong()
      )
    )
      .thenReturn(balances);
    when(servicioHogarMock.obtenerHogares(2L)).thenReturn(Collections.emptyList());

    servicioBajaIntegrante.confirmarBaja(2L);

    verify(repositorioHogarMock, times(1)).actualizar(hogarTest);
    assertThat(hogarTest.getIntegrantes(), not(hasItem(usuario2)));
  }

  @Test
  public void quePermitaRegistrarPagoExitosoDeBajaSiCorrespondeALiquidacion() {
    when(repositorioUsuarioMock.buscarPorId(2L)).thenReturn(usuario2);
    when(repositorioUsuarioMock.obtenerIntegrantesDelHogar(10L))
      .thenReturn(Arrays.asList(usuario1, usuario2));
    when(repositorioGastoMock.obtenerTodosPorHogar(10L)).thenReturn(Collections.emptyList());
    Map<String, Double> balances = new LinkedHashMap<>();
    balances.put("Carlos", 500.0);
    balances.put("Juan", -500.0);
    when(
      servicioCalculoBajaMock.calcularBalancesConBaja(
        anyList(),
        anyList(),
        any(),
        anyList(),
        anyDouble(),
        anyLong()
      )
    )
      .thenReturn(balances);
    when(servicioCalculoBajaMock.liquidacionDelSaliente(anyString(), any()))
      .thenReturn(
        Arrays.asList(new TransferenciaSugerida("Juan", "juan@mail.com", "Carlos", 500.0))
      );

    servicioBajaIntegrante.registrarPago(2L, usuario2, usuario1, 500.0);

    verify(servicioPagoRealizadoMock, times(1)).registrarPagoExitoso(any(PagoRealizado.class));
  }

  @Test
  public void queLanceExcepcionSiPagoNoCorrespondeALiquidacionDeBaja() {
    when(repositorioUsuarioMock.buscarPorId(2L)).thenReturn(usuario2);
    when(repositorioUsuarioMock.obtenerIntegrantesDelHogar(10L))
      .thenReturn(Arrays.asList(usuario1, usuario2));
    when(repositorioGastoMock.obtenerTodosPorHogar(10L)).thenReturn(Collections.emptyList());
    Map<String, Double> balances = new LinkedHashMap<>();
    balances.put("Carlos", 0.0);
    balances.put("Juan", 0.0);
    when(
      servicioCalculoBajaMock.calcularBalancesConBaja(
        anyList(),
        anyList(),
        any(),
        anyList(),
        anyDouble(),
        anyLong()
      )
    )
      .thenReturn(balances);

    assertThrows(
      PagoNoCorrespondeALiquidacionException.class,
      () -> servicioBajaIntegrante.registrarPago(2L, usuario2, usuario1, 500.0)
    );
  }

  @Test
  public void queObtengaIntegrantesActivosCorrectamente() {
    List<Usuario> activos = Arrays.asList(usuario1, usuario2);
    when(repositorioUsuarioMock.obtenerIntegrantesDelHogar(10L)).thenReturn(activos);

    List<Usuario> resultado = servicioBajaIntegrante.obtenerIntegrantesActivos();

    assertThat(resultado, notNullValue());
    assertThat(resultado, hasSize(2));
    verify(repositorioUsuarioMock, times(1)).obtenerIntegrantesDelHogar(10L);
  }
}
