package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.HogarSinIntegrantesException;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.excepcion.UsuarioYaDadoDeBajaException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioBajaIntegranteTest {

  private ServicioBajaIntegrante servicioBaja;
  private RepositorioUsuario repositorioUsuarioMock;
  private RepositorioGasto repositorioGastoMock;
  private ServicioHogar servicioHogarMock;
  private ServicioCreditoTarea servicioCreditoTareaMock;
  private Usuario juan;
  private Usuario maria;
  private Usuario pedro;
  private Hogar hogar;

  @BeforeEach
  public void init() {
    repositorioUsuarioMock = mock(RepositorioUsuario.class);
    repositorioGastoMock = mock(RepositorioGasto.class);
    servicioHogarMock = mock(ServicioHogar.class);
    servicioCreditoTareaMock = mock(ServicioCreditoTarea.class);
    servicioBaja =
      new ServicioBajaIntegranteImpl(
        repositorioUsuarioMock,
        repositorioGastoMock,
        new ServicioLiquidacionImpl(),
        new ServicioCalculosGastoImpl(),
        servicioHogarMock,
        servicioCreditoTareaMock
      );

    juan = new Usuario("Juan", 300000.0);
    juan.setId(1L);
    maria = new Usuario("Maria", 600000.0);
    maria.setId(2L);
    pedro = new Usuario("Pedro", 900000.0);
    pedro.setId(3L);

    hogar = new Hogar("Hogar 1");
    hogar.setPisoMinimoHogar(200000.0);
    hogar.setPrecioHoraTarea(5000.0);

    when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(juan);
    when(repositorioUsuarioMock.obtenerIntegrantesDelHogar())
      .thenReturn(List.of(juan, maria, pedro));
    when(repositorioGastoMock.obtenerTodos()).thenReturn(List.of());
    when(servicioHogarMock.obtenerHogar()).thenReturn(hogar);
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(anyLong())).thenReturn(0.0);
  }

  @Test
  public void simularBajaNoDeberiaGuardarNingunCambio() {
    // ejecucion
    servicioBaja.simularBaja(1L);

    // validacion
    assertThat(juan.getFechaBaja(), nullValue());
    verify(repositorioUsuarioMock, times(0)).modificar(any());
  }

  @Test
  public void siElQueSeVaNoPagoNadaDeberiaPagarleSuParteAlQuePago() {
    // preparacion: Maria pagó 30000 entre 3 -> Juan le debe 10000
    Gasto gasto = new Gasto("G1", TipoGasto.OCASIONAL, "Super", 30000.0, maria, "Comida");
    when(repositorioGastoMock.obtenerTodos()).thenReturn(List.of(gasto));

    // ejecucion
    SimulacionBaja simulacion = servicioBaja.simularBaja(1L);

    // validacion
    TransferenciaSugerida transferencia = simulacion.getLiquidacionFinal().get(0);
    assertThat(simulacion.getBalanceFinal(), equalTo(-10000.0));
    assertThat(transferencia.getEmisor(), equalTo("Juan"));
    assertThat(transferencia.getReceptor(), equalTo("Maria"));
    assertThat(transferencia.getMonto(), equalTo(10000.0));
  }

  @Test
  public void losGastosFijosDeberianRepartirseEntreLosQueQuedan() {
    // preparacion: 90000 de fijos -> 30000 entre 3, 45000 entre 2
    Gasto alquiler = new Gasto("G2", TipoGasto.FIJO, "Alquiler", 90000.0, maria, "Casa");
    when(repositorioGastoMock.obtenerTodos()).thenReturn(List.of(alquiler));

    // ejecucion
    SimulacionBaja simulacion = servicioBaja.simularBaja(1L);

    // validacion
    FilaCuota filaMaria = simulacion.getCuotasRestantes().get(0);
    assertThat(filaMaria.getParteFijaAntes(), equalTo(30000.0));
    assertThat(filaMaria.getParteFijaDespues(), equalTo(45000.0));
  }

  @Test
  public void sinPisoMinimoNoDeberiaCalcularLasCuotas() {
    // preparacion
    hogar.setPisoMinimoHogar(0.0);

    // ejecucion
    SimulacionBaja simulacion = servicioBaja.simularBaja(1L);

    // validacion
    assertFalse(simulacion.isCuotasCalculadas());
    assertThat(simulacion.getCuotasRestantes().get(0).getCuotaDespues(), nullValue());
  }

  @Test
  public void simularBajaDeUnUsuarioQueNoExisteDeberiaLanzarExcepcion() {
    // preparacion
    when(repositorioUsuarioMock.buscarPorId(99L)).thenReturn(null);

    // ejecucion y validacion
    assertThrows(UsuarioNoEncontrado.class, () -> servicioBaja.simularBaja(99L));
  }

  @Test
  public void simularBajaDeUnUsuarioYaDadoDeBajaDeberiaLanzarExcepcion() {
    // preparacion
    juan.setFechaBaja(LocalDateTime.now().minusDays(1));

    // ejecucion y validacion
    assertThrows(UsuarioYaDadoDeBajaException.class, () -> servicioBaja.simularBaja(1L));
  }

  @Test
  public void darDeBajaAlUltimoIntegranteDeberiaLanzarExcepcion() {
    // preparacion
    when(repositorioUsuarioMock.obtenerIntegrantesDelHogar()).thenReturn(List.of(juan));

    // ejecucion y validacion
    assertThrows(HogarSinIntegrantesException.class, () -> servicioBaja.confirmarBaja(1L));
    verify(repositorioUsuarioMock, times(0)).modificar(any());
  }

  @Test
  public void confirmarBajaDeberiaRegistrarLaFechaYRecalcularLasCuotas() {
    // ejecucion
    servicioBaja.confirmarBaja(1L);

    // validacion
    assertThat(juan.getFechaBaja(), notNullValue());
    assertThat(maria.getCuotaMensual(), notNullValue());
    verify(repositorioUsuarioMock, times(1)).modificar(juan);
    verify(repositorioUsuarioMock, times(1)).modificar(maria);
    verify(repositorioUsuarioMock, times(1)).modificar(pedro);
  }
}
