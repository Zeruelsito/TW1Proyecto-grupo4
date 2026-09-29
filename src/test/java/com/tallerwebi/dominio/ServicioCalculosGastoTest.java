package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tallerwebi.dominio.excepcion.MontoInvalidoPisoMinimoException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ServicioCalculosGastoTest {

  ServiceCalculosGasto serviceCalculosGasto = new ServiceCalculosGastoImpl();

  @Test
  public void dadoUnParticipantePuedoCalcularSuCapacidadRelativaDePago() {
    givenPisoMinimoConValor10000();
    Double montoEsperado = 5000d;
    Usuario usuario = givenUnUsuario();

    Double capacidadRelativaDePago = whenCalculoCapacidadRelativaDePago(usuario);

    thenLaCapacidadCoincideConElMontoEsperado(capacidadRelativaDePago, montoEsperado);
  }

  @Test
  public void deberiaLanzarUnExceptionSiPisoMinimoEs0() {
    givenPisoMinimoConValor0();
    Usuario usuario = givenUnUsuario();
    assertThrows(
      MontoInvalidoPisoMinimoException.class,
      () -> whenCalculoCapacidadRelativaDePago(usuario)
    );
  }

  @Test
  public void dadoVariosUsuariosCalcularLaSumaDeSusCapacidades() {
    givenPisoMinimoConValor10000();
    List<Usuario> usuarios = givenListaUsuarios();
    Double montoEsperado = 620000d;
    serviceCalculosGasto.calcularTotalSumaDeCapacidadesDeCadaUsuario(usuarios);
    serviceCalculosGasto.establecerMontoTotalDeLasSumasDeCapacidades(usuarios);
    Double montoCapacidadDeTodosLosUsuario =
      serviceCalculosGasto.getMontoCapacidadTotalDeLosUsuarios();
    assertThat(montoCapacidadDeTodosLosUsuario, equalTo(montoEsperado));
  }

  @Test
  public void dadoVariosUsuariosCalcularSusPorcentajesDelTotalDeSusCapacidades() {
    givenPisoMinimoConValor10000();
    List<Usuario> usuarios = givenListaUsuarios();

    serviceCalculosGasto.calcularTotalSumaDeCapacidadesDeCadaUsuario(usuarios);
    serviceCalculosGasto.establecerMontoTotalDeLasSumasDeCapacidades(usuarios);

    usuarios = serviceCalculosGasto.calcularPorcentajesDeCapacidadMensualDeUsuarios(usuarios);

    Double porcentajeUsuario1Esperado = 22.58;
    Double porcentajeUsuario2Esperado = 30.65d;
    Double porcentajeUsuario3Esperado = 46.77d;

    assertThat(
      usuarios.get(0).getPorcentajeSegunCapacidadDePagoMensual(),
      equalTo(porcentajeUsuario1Esperado)
    );
    assertThat(
      usuarios.get(1).getPorcentajeSegunCapacidadDePagoMensual(),
      equalTo(porcentajeUsuario2Esperado)
    );
    assertThat(
      usuarios.get(2).getPorcentajeSegunCapacidadDePagoMensual(),
      equalTo(porcentajeUsuario3Esperado)
    );
  }

  private List<Usuario> givenListaUsuarios() {
    List<Usuario> usuarios = new ArrayList<>();
    Usuario usuario1 = new Usuario("Lucas", 150000d, 15d);
    Usuario usuario2 = new Usuario("Maca", 200000d, 15d);
    Usuario usuario3 = new Usuario("Lucas", 300000d, 115d);
    usuarios.add(usuario1);
    usuarios.add(usuario2);
    usuarios.add(usuario3);
    return usuarios;
  }

  private void thenLaCapacidadCoincideConElMontoEsperado(
    Double capacidadRelativaDePago,
    Double montoEsperado
  ) {
    assertThat(capacidadRelativaDePago, equalTo(montoEsperado));
  }

  private Usuario givenUnUsuario() {
    return new Usuario("eze", 15000d, 15d);
  }

  private void givenPisoMinimoConValor0() {
    serviceCalculosGasto.establecerPisoMinimoSeguro(0d);
  }

  private void givenPisoMinimoConValor10000() {
    serviceCalculosGasto.establecerPisoMinimoSeguro(10000d);
  }

  private Double whenCalculoCapacidadRelativaDePago(Usuario usuario) {
    return serviceCalculosGasto.calculoCapacidadRelativaDePago(usuario);
  }
}
