package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tallerwebi.dominio.excepcion.MontoInvalidoPisoMinimoException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioCalculosGastoTest {

  private ServicioCalculosGastoImpl servicioCalculosGasto;

  @BeforeEach
  public void init() {
    servicioCalculosGasto = new ServicioCalculosGastoImpl();
  }

  @Test
  public void dadoUnUsuarioYUnPisoMinimoValidoКогдаCalculoCapacidadRelativaEntoncesRetornaCapacidadCorrecta() {
    // Given
    Usuario usuario = givenUnUsuarioConIngreso(100000.0);
    Double pisoMinimo = 40000.0;

    // When
    Double capacidad = whenCalculoCapacidadRelativa(usuario, pisoMinimo);

    // Then
    assertThat(capacidad, is(60000.0));
    assertThat(usuario.getCapacidadRelativaPagoMensual(), is(60000.0));
  }

  @Test
  public void dadoUnPisoMinimoInvalidoКогдаCalculoCapacidadRelativaEntoncesLanzaExcepcion() {
    // Given
    Usuario usuario = givenUnUsuarioConIngreso(100000.0);
    Double pisoInvalido = 0.0;

    // Then
    assertThrows(
      MontoInvalidoPisoMinimoException.class,
      () -> whenCalculoCapacidadRelativa(usuario, pisoInvalido)
    );
  }

  @Test
  public void dadaUnaListaDeUsuariosCuandoCalculoTotalSumaDeCapacidadesEntoncesEstableceElPozoCorrectamente() {
    // Given
    List<Usuario> usuarios = givenCuatroIntegrantesConDiferentesIngresos();
    Double pisoMinimo = 20000.0;

    // When
    servicioCalculosGasto.calcularTotalSumaDeCapacidadesDeCadaUsuario(usuarios, pisoMinimo);
    servicioCalculosGasto.establecerMontoTotalDeLasSumasDeCapacidades(usuarios);

    // Then
    Double totalPozo = servicioCalculosGasto.getMontoCapacidadTotalDeLosUsuarios();
    assertThat(totalPozo, notNullValue());
    // Ingresos: 30k(cap:10k), 50k(cap:30k), 70k(cap:50k), 90k(cap:70k) -> Total = 160000.0
    assertThat(totalPozo, is(160000.0));
  }

  @Test
  public void dadaUnaListaDeUsuariosКогдаCalculoPorcentajesDeCapacidadEntoncesAsignaPorcentajesCorrectos() {
    // Given
    List<Usuario> usuarios = givenCuatroIntegrantesConDiferentesIngresos();
    Double pisoMinimo = 20000.0;

    servicioCalculosGasto.calcularTotalSumaDeCapacidadesDeCadaUsuario(usuarios, pisoMinimo);
    servicioCalculosGasto.establecerMontoTotalDeLasSumasDeCapacidades(usuarios);

    // When
    servicioCalculosGasto.calcularPorcentajesDeCapacidadMensualDeUsuarios(usuarios);

    // Then
    // Capacidades: 10k, 30k, 50k, 70k sobre 160k total (6.25%, 18.75%, 31.25%, 43.75%)
    assertThat(usuarios.get(0).getPorcentajeSegunCapacidadDePagoMensual(), is(6.25));
    assertThat(usuarios.get(1).getPorcentajeSegunCapacidadDePagoMensual(), is(18.75));
    assertThat(usuarios.get(2).getPorcentajeSegunCapacidadDePagoMensual(), is(31.25));
    assertThat(usuarios.get(3).getPorcentajeSegunCapacidadDePagoMensual(), is(43.75));
  }

  @Test
  public void dadaUnaListaDeUsuariosConPorcentajesКогдаCalculoCuotasMensualesEntoncesAsignaCuotasCorrectas() {
    // Given
    Usuario u1 = givenUnUsuarioConIngreso(30000.0);
    u1.setPorcentajeSegunCapacidadDePagoMensual(50.0);
    Usuario u2 = givenUnUsuarioConIngreso(50000.0);
    u2.setPorcentajeSegunCapacidadDePagoMensual(50.0);

    List<Usuario> usuarios = Arrays.asList(u1, u2);
    Double montoGastosOcasionales = 20000.0;

    // When
    servicioCalculosGasto.calcularCuotasMensualesDeUsuarios(usuarios, montoGastosOcasionales);

    // Then
    assertThat(u1.getCuotaMensual(), is(10000.0));
    assertThat(u2.getCuotaMensual(), is(10000.0));
  }

  @Test
  public void dadoUnUsuarioCuandoCalculoCuotaMensualIndividualEntoncesRetornaMontoEsperado() {
    // Given
    List<Usuario> integrantes = givenCuatroIntegrantesConDiferentesIngresos();
    Usuario usuario = integrantes.get(0);
    Double montoGastos = 40000.0;
    Double pisoMinimo = 20000.0;

    // When
    Double cuota = servicioCalculosGasto.calcularCuotaMensualPorUsuario(
      usuario,
      montoGastos,
      pisoMinimo,
      integrantes
    );

    // Then
    assertNotNull(cuota);
    // Parte piso: 20000 / 4 = 5000. Porcentaje capacidad de u1 (10k de 160k = 6.25%).
    // 6.25% de 40000 = 2500. Total = 5000 + 2500 = 7500.0
    assertThat(cuota, is(7500.0));
  }

  @Test
  public void dadaUnaListaDeUsuariosSinCapacidadCuandoCalculoPorcentajeAporteEntoncesReparteEnPartesIguales() {
    // Given
    Usuario u1 = givenUnUsuarioConIngreso(10000.0);
    Usuario u2 = givenUnUsuarioConIngreso(10000.0);
    List<Usuario> integrantes = Arrays.asList(u1, u2);
    Double pisoMinimo = 20000.0; // Nadie supera el piso mínimo

    // When
    Double porcentaje = servicioCalculosGasto.calcularPorcentajeDeAporteDeGastosPorUsuario(
      u1,
      integrantes,
      pisoMinimo
    );

    // Then
    // Como la suma de capacidades es <= 0, reparte 100 / 2 = 50.0%
    assertThat(porcentaje, is(50.0));
  }

  @Test
  public void dadoUnUsuarioConBajoAporteCuandoCalculoValorHorasTareaEntoncesRetornaValorADeber() {
    // Given
    List<Usuario> integrantes = givenCuatroIntegrantesConDiferentesIngresos();
    Usuario usuarioConMenorIngreso = integrantes.get(0); // Ingreso 30k, capacidad menor
    Double valorHora = 1000.0;
    Double gastosOcasionales = 50000.0;
    Double pisoMinimo = 20000.0;

    // When
    Double valorHoras = servicioCalculosGasto.calcularValorDeHorasDeTareaPorUsuario(
      usuarioConMenorIngreso,
      valorHora,
      gastosOcasionales,
      pisoMinimo,
      integrantes
    );

    // Then
    assertNotNull(valorHoras);
    // Debe calcular el saldo pendiente basado en la brecha con el aporte mínimo esperado
    assertThat(valorHoras, greaterThanOrEqualTo(0.0));
  }

  @Test
  public void dadasCuotasDeUsuariosКогдаCalculoHorasDeTareasEntoncesAsignaHorasCorrectamente() {
    // Given
    Usuario u1 = givenUnUsuarioConIngreso(30000.0);
    u1.setCuotaMensual(5000.0); // Cuota menor

    Usuario u2 = givenUnUsuarioConIngreso(90000.0);
    u2.setCuotaMensual(15000.0); // Cuota máxima

    List<Usuario> usuarios = Arrays.asList(u1, u2);
    Double valorHoraTarea = 1000.0;

    // When
    servicioCalculosGasto.calcularHorasDeTareasDeUsuarios(usuarios, valorHoraTarea);

    // Then
    // Cuota máxima = 15000. Brecha para u1 = 15000 - 5000 = 10000 / 1000 = 10 horas.
    assertThat(u1.getHorasTareaARealizar(), is(10.0));
    assertThat(u2.getHorasTareaARealizar(), is(0.0));
  }

  private Usuario givenUnUsuarioConIngreso(Double ingreso) {
    Usuario usuario = new Usuario("Test", "test@kumo.com", "1234");
    usuario.setIngresoMensual(ingreso);
    return usuario;
  }

  private List<Usuario> givenCuatroIntegrantesConDiferentesIngresos() {
    Usuario u1 = givenUnUsuarioConIngreso(30000.0);
    u1.setId(1L);
    Usuario u2 = givenUnUsuarioConIngreso(50000.0);
    u2.setId(2L);
    Usuario u3 = givenUnUsuarioConIngreso(70000.0);
    u3.setId(3L);
    Usuario u4 = givenUnUsuarioConIngreso(90000.0);
    u4.setId(4L);
    return Arrays.asList(u1, u2, u3, u4);
  }

  private Double whenCalculoCapacidadRelativa(Usuario usuario, Double pisoMinimo) {
    return servicioCalculosGasto.calculoCapacidadRelativaDePago(usuario, pisoMinimo);
  }
}
