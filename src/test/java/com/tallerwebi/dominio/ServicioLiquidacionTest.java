package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioLiquidacionTest {

  private ServicioLiquidacion servicioLiquidacion;

  @BeforeEach
  public void init() {
    this.servicioLiquidacion = new ServicioLiquidacionImpl();
  }

  @Test
  public void siTodosTienenSaldoCeroNoDebeGenerarTransferencias() {
    //given
    Map<String, Double> balances = givenUnMapaConBalancesConSaldosCero();
    //when
    List<TransferenciaSugerida> resultado = whenCalculoLaLiquidacion(balances);
    //then
    thenObtengoUnaListaVacia(resultado);
  }

  @Test
  public void siUnUsuarioDebeYOtroEsAcreedorDebeGenerarUnaSolaTransferencia() {
    // Juan consumió $1000 de más (-1000), Maria pagó $1000 de más (+1000)
    //given
    Map<String, Double> balances = givenUnMapaConUnAcreedorYUnDeudor();
    //when
    List<TransferenciaSugerida> resultado = whenCalculoLaLiquidacion(balances);
    //then
    thenObtengoUnaListaConUnaSolaTransferencia(resultado);
  }

  @Test
  public void tresIntegrantesDebenSimplificarTransaccionesCorrectamente() {
    // Juan debe $2000, Pedro debe $1000, Maria puso $3000
    //given
    Map<String, Double> balances = givenUnMapaConBalancesDeTresIntegrantes();
    //when
    List<TransferenciaSugerida> resultado = whenCalculoLaLiquidacion(balances);
    //then
    thenObtengoUnaListaConDosTransferencias(resultado);
  }

  @Test
  public void debeCalcularLiquidacionDelMesConObjetosEnMemoria() {
    // Given
    Hogar hogar = givenUnHogarConDosIntegrantes();
    // Gasto total: $10.000 (pagado por Juan con ID 1L)
    Gasto gastoServicio = givenUnGasto();
    List<Gasto> gastos = List.of(gastoServicio);

    // Mock del servicio de tareas
    ServicioCreditoTarea servicioCreditoTareaMock = mock(ServicioCreditoTarea.class);
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(1L)).thenReturn(0.0);
    when(servicioCreditoTareaMock.obtenerCreditoAcumuladoPorUsuario(2L)).thenReturn(2000.0);

    ServicioLiquidacion servicio = new ServicioLiquidacionImpl(servicioCreditoTareaMock);

    // When
    List<TransferenciaSugerida> transferencias = servicio.obtenerLiquidacionDelMes(hogar, gastos);

    // Then
    // Consumo indiv: $5000. Juan pagó $10000 (+$5000). María hizo $2000 en tareas (-$3000).
    assertThat(transferencias.size(), equalTo(1));
    assertThat(transferencias.get(0), equalTo(new TransferenciaSugerida("Maria", "Juan", 3000.0)));
  }

  private Gasto givenUnGasto() {
    Gasto gastoServicio = new Gasto(TipoGasto.OCASIONAL, "gasto x", 10000.0, 1L, "VARIOS");
    return gastoServicio;
  }

  private Hogar givenUnHogarConDosIntegrantes() {
    Usuario juan = new Usuario("Juan", "juan@kumo.com", "1234");
    juan.setId(1L);
    Usuario maria = new Usuario("Maria", "maria@kumo.com", "1212");
    maria.setId(2L);
    Hogar hogar = new Hogar("Hogar Compartido");
    hogar.getIntegrantes().add(juan);
    hogar.getIntegrantes().add(maria);
    return hogar;
  }

  private void thenObtengoUnaListaVacia(List<TransferenciaSugerida> resultado) {
    assertTrue(resultado.isEmpty());
  }

  private List<TransferenciaSugerida> whenCalculoLaLiquidacion(Map<String, Double> balances) {
    List<TransferenciaSugerida> resultado = servicioLiquidacion.calcularLiquidacionOptima(balances);
    return resultado;
  }

  private Map<String, Double> givenUnMapaConBalancesConSaldosCero() {
    Map<String, Double> balances = new HashMap<>();
    balances.put("Juan", 0.0);
    balances.put("Maria", 0.0);
    return balances;
  }

  private void thenObtengoUnaListaConUnaSolaTransferencia(List<TransferenciaSugerida> resultado) {
    assertThat(resultado.size(), equalTo(1));
    assertThat(resultado.get(0), equalTo(new TransferenciaSugerida("Juan", "Maria", 1000.0)));
  }

  private Map<String, Double> givenUnMapaConUnAcreedorYUnDeudor() {
    Map<String, Double> balances = new HashMap<>();
    balances.put("Juan", -1000.0);
    balances.put("Maria", 1000.0);
    return balances;
  }

  private void thenObtengoUnaListaConDosTransferencias(List<TransferenciaSugerida> resultado) {
    assertThat(resultado.size(), equalTo(2));
    assertThat(
      resultado,
      containsInAnyOrder(
        new TransferenciaSugerida("Juan", "Maria", 2000.0),
        new TransferenciaSugerida("Pedro", "Maria", 1000.0)
      )
    );
  }

  private Map<String, Double> givenUnMapaConBalancesDeTresIntegrantes() {
    Map<String, Double> balances = new HashMap<>();
    balances.put("Juan", -2000.0);
    balances.put("Pedro", -1000.0);
    balances.put("Maria", 3000.0);
    return balances;
  }
}
