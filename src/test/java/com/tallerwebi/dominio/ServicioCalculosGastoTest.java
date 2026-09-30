package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tallerwebi.dominio.excepcion.MontoInvalidoPisoMinimoException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ServicioCalculosGastoTest {

  ServicioCalculosGasto servicioCalculosGasto = new ServicioCalculosGastoImpl();

  @Test
  public void dadoUnParticipantePuedoCalcularSuCapacidadRelativaDePago() {
    Double valorPisoMinimoSeguro = givenPisoMinimoSeguro();
    Double montoEsperado = 5000d;
    Usuario usuario = givenUnUsuario();

    Double capacidadRelativaDePago = whenCalculoCapacidadRelativaDePago(
      usuario,
      valorPisoMinimoSeguro
    );

    thenLaCapacidadCoincideConElMontoEsperado(capacidadRelativaDePago, montoEsperado);
  }

  @Test
  public void dadoUnMontoInvalidodeberiaLanzarUnExceptionSiPisoMinimo() {
    Double valorPisoMinimoSeguro = givenPisoMinimoFalse();
    Usuario usuario = givenUnUsuario();
    thenSeLanzaMontoInvalidoPisoMinimoException(usuario, valorPisoMinimoSeguro);
  }

  @Test
  public void dadoVariosUsuariosCalcularLaSumaDeSusCapacidades() {
    Double valorPisoMinimoSeguro = givenPisoMinimoSeguro();
    List<Usuario> usuarios = givenListaUsuarios();
    Double montoEsperado = 620000d;
    Double montoCapacidadDeTodosLosUsuario = whenSeCalculaLaSumaDeLasCapacidades(
      usuarios,
      valorPisoMinimoSeguro
    );
    thenLaSumaDebeDarElMontoEsperado(montoEsperado, montoCapacidadDeTodosLosUsuario);
  }

  @Test
  public void dadoVariosUsuariosCalcularSusPorcentajesDelTotalDeSusCapacidades() {
    Double valorPisoMinimoSeguro = givenPisoMinimoSeguro();
    List<Usuario> usuarios = givenListaUsuarios();
    List<Usuario> usuariosConPorcentajes =
      whenSeCalculaPorcentajesDeUsuariosAsignaLaSumaTotalDelPozoYLaSumaDeSusCapacidades(
        usuarios,
        valorPisoMinimoSeguro
      );
    thenLosPorcentajesDeLosUsuariosEstanSeteados(usuariosConPorcentajes);
  }

  @Test
  public void dadoElPozoTotalDelMesAsignarLascuotasMensualesAjustandoseAcadaUsuario() {
    Double valorPisoMinimoSeguro = 160000d;
    List<Usuario> usuarios = givenListaUsuarios();
    Usuario usuario4 = new Usuario("Ian", 600000d);
    usuarios.add(usuario4);
    List<Usuario> usuariosConPorcentajes =
      whenSeCalculaPorcentajesDeUsuariosAsignaLaSumaTotalDelPozoYLaSumaDeSusCapacidades(
        usuarios,
        valorPisoMinimoSeguro
      );
    servicioCalculosGasto.calcularCuotasMensualesDeUsuarios(
      usuariosConPorcentajes,
      valorPisoMinimoSeguro
    );
    thenSeDefineLaCuotaMensualPorPorcentaje(usuariosConPorcentajes);
  }

  @Test
  public void dadoUsuariosPoderAsignarHoraDeTareasSegunSuAporte() {
    Double valorPisoMinimoSeguro = 90000d;
    List<Usuario> usuarios = givenListaUsuarios();
    Usuario usuario4 = new Usuario("Ian", 600000d);
    usuarios.add(usuario4);
    List<Usuario> usuariosConPorcentajes =
      whenSeCalculaPorcentajesDeUsuariosAsignaLaSumaTotalDelPozoYLaSumaDeSusCapacidades(
        usuarios,
        valorPisoMinimoSeguro
      );
    servicioCalculosGasto.calcularCuotasMensualesDeUsuarios(
      usuariosConPorcentajes,
      valorPisoMinimoSeguro
    );

    servicioCalculosGasto.calcularHorasDeTareasDeUsuarios(usuariosConPorcentajes, 10000d);

    thenSeDefineLasHorasDeTrabajoACadaUsuario(usuariosConPorcentajes);
  }

  private void thenSeDefineLasHorasDeTrabajoACadaUsuario(List<Usuario> usuariosConPorcentajes) {
    Double montoEsperado1 = 42d;
    Double montoEsperado2 = 40d;
    Double montoEsperado3 = 30d;
    Double montoEsperado4 = 0d;

    assertThat(usuariosConPorcentajes.get(0).getHorasTareaARealizar(), equalTo(montoEsperado1));
    assertThat(usuariosConPorcentajes.get(1).getHorasTareaARealizar(), equalTo(montoEsperado2));
    assertThat(usuariosConPorcentajes.get(2).getHorasTareaARealizar(), equalTo(montoEsperado3));
    assertThat(usuariosConPorcentajes.get(3).getHorasTareaARealizar(), equalTo(montoEsperado4));
  }

  private void thenSeDefineLaCuotaMensualPorPorcentaje(List<Usuario> usuariosConPorcentajes) {
    Double montoEsperado1 = 160000d;
    Double montoEsperado2 = 160000d;
    Double montoEsperado3 = 160000d;
    Double montoEsperado4 = 440014d;

    assertThat(usuariosConPorcentajes.get(0).getCuotaMensual(), equalTo(montoEsperado1));
    assertThat(usuariosConPorcentajes.get(1).getCuotaMensual(), equalTo(montoEsperado2));
    assertThat(usuariosConPorcentajes.get(2).getCuotaMensual(), equalTo(montoEsperado3));
    assertThat(usuariosConPorcentajes.get(3).getCuotaMensual(), equalTo(montoEsperado4));
  }

  private Usuario givenUnUsuario() {
    return new Usuario("eze", 15000d);
  }

  private Double givenPisoMinimoSeguro() {
    return 10000d;
  }

  private Double givenPisoMinimoFalse() {
    return 0d;
  }

  private List<Usuario> givenListaUsuarios() {
    List<Usuario> usuarios = new ArrayList<>();
    Usuario usuario1 = new Usuario("Lucas", 150000d);
    Usuario usuario2 = new Usuario("Maca", 200000d);
    Usuario usuario3 = new Usuario("Lucas", 300000d);
    usuarios.add(usuario1);
    usuarios.add(usuario2);
    usuarios.add(usuario3);
    return usuarios;
  }

  private Double whenCalculoCapacidadRelativaDePago(Usuario usuario, Double valorPisoMinimoSeguro) {
    return servicioCalculosGasto.calculoCapacidadRelativaDePago(usuario, valorPisoMinimoSeguro);
  }

  private Double whenSeCalculaLaSumaDeLasCapacidades(
    List<Usuario> usuarios,
    Double valorPisoMinimoSeguro
  ) {
    servicioCalculosGasto.calcularTotalSumaDeCapacidadesDeCadaUsuario(
      usuarios,
      valorPisoMinimoSeguro
    );
    servicioCalculosGasto.establecerMontoTotalDeLasSumasDeCapacidades(usuarios);
    return servicioCalculosGasto.getMontoCapacidadTotalDeLosUsuarios();
  }

  private List<
    Usuario
  > whenSeCalculaPorcentajesDeUsuariosAsignaLaSumaTotalDelPozoYLaSumaDeSusCapacidades(
    List<Usuario> usuarios,
    Double valorPisoMinimoSeguro
  ) {
    servicioCalculosGasto.calcularTotalSumaDeCapacidadesDeCadaUsuario(
      usuarios,
      valorPisoMinimoSeguro
    );
    servicioCalculosGasto.establecerMontoTotalDeLasSumasDeCapacidades(usuarios);
    servicioCalculosGasto.calcularPorcentajesDeCapacidadMensualDeUsuarios(usuarios);
    return usuarios;
  }

  private void thenSeLanzaMontoInvalidoPisoMinimoException(
    Usuario usuario,
    Double valorPisoMinimoSeguro
  ) {
    assertThrows(
      MontoInvalidoPisoMinimoException.class,
      () -> whenCalculoCapacidadRelativaDePago(usuario, valorPisoMinimoSeguro)
    );
  }

  private void thenLaCapacidadCoincideConElMontoEsperado(
    Double capacidadRelativaDePago,
    Double montoEsperado
  ) {
    assertThat(capacidadRelativaDePago, equalTo(montoEsperado));
  }

  private void thenLaSumaDebeDarElMontoEsperado(
    Double montoEsperado,
    Double montoCapacidadDeTodosLosUsuario
  ) {
    assertThat(montoCapacidadDeTodosLosUsuario, equalTo(montoEsperado));
  }

  private void thenLosPorcentajesDeLosUsuariosEstanSeteados(List<Usuario> usuariosConPorcentajes) {
    Double porcentajeUsuario1Esperado = 22.58;
    Double porcentajeUsuario2Esperado = 30.65d;
    Double porcentajeUsuario3Esperado = 46.77d;

    assertThat(
      usuariosConPorcentajes.get(0).getPorcentajeSegunCapacidadDePagoMensual(),
      equalTo(porcentajeUsuario1Esperado)
    );
    assertThat(
      usuariosConPorcentajes.get(1).getPorcentajeSegunCapacidadDePagoMensual(),
      equalTo(porcentajeUsuario2Esperado)
    );
    assertThat(
      usuariosConPorcentajes.get(2).getPorcentajeSegunCapacidadDePagoMensual(),
      equalTo(porcentajeUsuario3Esperado)
    );
  }
}
