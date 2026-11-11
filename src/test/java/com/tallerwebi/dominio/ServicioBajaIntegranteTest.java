package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.HogarSinIntegrantesException;
import com.tallerwebi.dominio.excepcion.IntegranteConSaldoPendienteException;
import com.tallerwebi.dominio.excepcion.PagoNoCorrespondeALiquidacionException;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.excepcion.UsuarioYaDadoDeBajaException;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ServicioBajaIntegranteTest {

  private RepositorioUsuario repositorioUsuarioMock;
  private RepositorioGasto repositorioGastoMock;
  private ServicioLiquidacion servicioLiquidacionMock;
  private ServicioCalculosGasto servicioCalculosGastoMock;
  private ServicioHogar servicioHogarMock;
  private ServicioCreditoTarea servicioCreditoTareaMock;
  private ServicioPagoRealizado servicioPagoRealizadoMock;
  private HttpSession sessionMock;

  private ServicioBajaIntegranteImpl servicioBajaIntegrante;

  private Hogar hogarTest;
  private Usuario usuario1;
  private Usuario usuario2;

  @BeforeEach
  public void setUp() {
    repositorioUsuarioMock = Mockito.mock(RepositorioUsuario.class);
    repositorioGastoMock = Mockito.mock(RepositorioGasto.class);
    servicioLiquidacionMock = Mockito.mock(ServicioLiquidacion.class);
    servicioCalculosGastoMock = Mockito.mock(ServicioCalculosGasto.class);
    servicioHogarMock = Mockito.mock(ServicioHogar.class);
    servicioCreditoTareaMock = Mockito.mock(ServicioCreditoTarea.class);
    servicioPagoRealizadoMock = Mockito.mock(ServicioPagoRealizado.class);
    sessionMock = Mockito.mock(HttpSession.class);

    servicioBajaIntegrante =
      new ServicioBajaIntegranteImpl(
        repositorioUsuarioMock,
        repositorioGastoMock,
        servicioLiquidacionMock,
        servicioCalculosGastoMock,
        servicioHogarMock,
        servicioCreditoTareaMock,
        servicioPagoRealizadoMock,
        sessionMock
      );

    hogarTest = new Hogar();
    hogarTest.setId(10L);
    hogarTest.setPisoMinimoHogar(1000.0);
    hogarTest.setPrecioHoraTarea(100.0);

    usuario1 = new Usuario();
    usuario1.setId(1L);
    usuario1.setNombre("Carlos");
    usuario1.setIngresoMensual(50000.0);

    usuario2 = new Usuario();
    usuario2.setId(2L);
    usuario2.setNombre("Juan");
    usuario2.setIngresoMensual(60000.0);

    when(sessionMock.getAttribute("HOGAR_ID")).thenReturn(10L);
  }

  @Test
  public void queLanceExcepcionSiUsuarioNoExisteAlSimularBaja() {
    when(repositorioUsuarioMock.buscarPorId(99L)).thenReturn(null);

    assertThrows(
      UsuarioNoEncontrado.class,
      () -> {
        servicioBajaIntegrante.simularBaja(99L);
      }
    );
  }

  @Test
  public void queLanceExcepcionSiUsuarioYaEstaDeBaja() {
    usuario1.setFechaBaja(LocalDateTime.now().minusDays(5));
    when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(usuario1);

    assertThrows(
      UsuarioYaDadoDeBajaException.class,
      () -> {
        servicioBajaIntegrante.simularBaja(1L);
      }
    );
  }

  @Test
  public void queLanceExcepcionSiElHogarSeQuedaSinIntegrantesAlDarDeBaja() {
    when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(usuario1);
    when(repositorioUsuarioMock.obtenerIntegrantesDelHogar(10L))
      .thenReturn(Arrays.asList(usuario1));

    assertThrows(
      HogarSinIntegrantesException.class,
      () -> {
        servicioBajaIntegrante.simularBaja(1L);
      }
    );
  }

  @Test
  public void queSimuleBajaCorrectamenteSinDeudaPendiente() {
    when(repositorioUsuarioMock.buscarPorId(2L)).thenReturn(usuario2);
    when(repositorioUsuarioMock.obtenerIntegrantesDelHogar(10L))
      .thenReturn(Arrays.asList(usuario1, usuario2));
    when(repositorioGastoMock.obtenerTodosPorHogar(10L)).thenReturn(Collections.emptyList());
    when(servicioLiquidacionMock.calcularLiquidacionOptima(any()))
      .thenReturn(Collections.emptyList());
    when(servicioHogarMock.obtenerHogar()).thenReturn(hogarTest);
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(anyLong())).thenReturn(0.0);

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

    Gasto gasto = new Gasto();
    gasto.setMonto(1000.0);
    gasto.setUsuarioPagador(usuario1);
    gasto.setParticipantes(Arrays.asList(usuario1, usuario2));
    when(repositorioGastoMock.obtenerTodosPorHogar(10L)).thenReturn(Arrays.asList(gasto));

    List<TransferenciaSugerida> transferencias = Arrays.asList(
      new TransferenciaSugerida("Juan", "juan@mail.com", "Carlos", 500.0)
    );
    when(servicioLiquidacionMock.calcularLiquidacionOptima(any())).thenReturn(transferencias);
    when(servicioHogarMock.obtenerHogar()).thenReturn(hogarTest);

    assertThrows(
      IntegranteConSaldoPendienteException.class,
      () -> {
        servicioBajaIntegrante.confirmarBaja(2L);
      }
    );
  }

  @Test
  public void queConfirmeBajaExitosamenteSiNoHaySaldoPendiente() {
    when(repositorioUsuarioMock.buscarPorId(2L)).thenReturn(usuario2);
    when(repositorioUsuarioMock.obtenerIntegrantesDelHogar(10L))
      .thenReturn(Arrays.asList(usuario1, usuario2));
    when(repositorioGastoMock.obtenerTodosPorHogar(10L)).thenReturn(Collections.emptyList());
    when(servicioLiquidacionMock.calcularLiquidacionOptima(any()))
      .thenReturn(Collections.emptyList());
    when(servicioHogarMock.obtenerHogar()).thenReturn(hogarTest);

    servicioBajaIntegrante.confirmarBaja(2L);

    verify(repositorioUsuarioMock, times(1)).modificar(usuario2);
    assertThat(usuario2.getFechaBaja(), notNullValue());
  }

  @Test
  public void quePermitaRegistrarPagoExitosoDeBajaSiCorrespondeALiquidacion() {
    when(repositorioUsuarioMock.buscarPorId(2L)).thenReturn(usuario2);
    when(repositorioUsuarioMock.obtenerIntegrantesDelHogar(10L))
      .thenReturn(Arrays.asList(usuario1, usuario2));
    when(repositorioGastoMock.obtenerTodosPorHogar(10L)).thenReturn(Collections.emptyList());

    List<TransferenciaSugerida> transferencias = Arrays.asList(
      new TransferenciaSugerida("Juan", "juan@mail.com", "Carlos", 500.0)
    );
    when(servicioLiquidacionMock.calcularLiquidacionOptima(any())).thenReturn(transferencias);
    when(servicioHogarMock.seleccionarHogarPorId(10L)).thenReturn(hogarTest);

    servicioBajaIntegrante.registrarPago(2L, usuario2, usuario1, 500.0);

    verify(servicioPagoRealizadoMock, times(1)).registrarPagoExitoso(any(PagoRealizado.class));
  }

  @Test
  public void queLanceExcepcionSiPagoNoCorrespondeALiquidacionDeBaja() {
    when(repositorioUsuarioMock.buscarPorId(2L)).thenReturn(usuario2);
    when(repositorioUsuarioMock.obtenerIntegrantesDelHogar(10L))
      .thenReturn(Arrays.asList(usuario1, usuario2));
    when(repositorioGastoMock.obtenerTodosPorHogar(10L)).thenReturn(Collections.emptyList());
    when(servicioLiquidacionMock.calcularLiquidacionOptima(any()))
      .thenReturn(Collections.emptyList());

    assertThrows(
      PagoNoCorrespondeALiquidacionException.class,
      () -> {
        servicioBajaIntegrante.registrarPago(2L, usuario2, usuario1, 500.0);
      }
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
