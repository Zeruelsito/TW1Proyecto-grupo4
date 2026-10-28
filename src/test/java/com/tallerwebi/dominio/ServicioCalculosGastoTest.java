package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.excepcion.MontoInvalidoPisoMinimoException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioCalculosGastoTest {

  private ServicioCalculosGasto servicioCalculosGasto;

  @BeforeEach
  public void setUp() {
    servicioCalculosGasto = new ServicioCalculosGastoImpl();
  }

  @Test
  public void queLanceExcepcionSiPisoMinimoEsNullOmenorAIgualACero() {
    assertThrows(
      MontoInvalidoPisoMinimoException.class,
      () -> {
        servicioCalculosGasto.calculoCapacidadRelativaDePago(new Usuario(), null);
      }
    );

    assertThrows(
      MontoInvalidoPisoMinimoException.class,
      () -> {
        servicioCalculosGasto.calculoCapacidadRelativaDePago(new Usuario(), 0.0);
      }
    );

    assertThrows(
      MontoInvalidoPisoMinimoException.class,
      () -> {
        servicioCalculosGasto.calculoCapacidadRelativaDePago(new Usuario(), -150.0);
      }
    );
  }

  @Test
  public void queRetorneCeroSiUsuarioEsNullEnCalculoCapacidadRelativa() {
    Double resultado = servicioCalculosGasto.calculoCapacidadRelativaDePago(null, 500.0);
    assertEquals(0.0, resultado);
  }

  @Test
  public void queCalculeCorrectamenteLaCapacidadRelativaDePago() {
    Usuario usuario = new Usuario();
    usuario.setIngresoMensual(1500.0);

    Double capacidad = servicioCalculosGasto.calculoCapacidadRelativaDePago(usuario, 500.0);

    assertEquals(1000.0, capacidad);
    assertEquals(1000.0, usuario.getCapacidadRelativaPagoMensual());
  }

  @Test
  public void queCapacidadRelativaSeaCeroSiIngresoEsMenorAPisoMinimo() {
    Usuario usuario = new Usuario();
    usuario.setIngresoMensual(300.0);

    Double capacidad = servicioCalculosGasto.calculoCapacidadRelativaDePago(usuario, 500.0);

    assertEquals(0.0, capacidad);
    assertEquals(0.0, usuario.getCapacidadRelativaPagoMensual());
  }

  @Test
  public void queCalculeTotalSumaDeCapacidadesYEstablezcaMontoPozo() {
    Usuario u1 = new Usuario();
    u1.setIngresoMensual(1500.0);
    Usuario u2 = new Usuario();
    u2.setIngresoMensual(2500.0);

    List<Usuario> usuarios = Arrays.asList(u1, u2);

    servicioCalculosGasto.calcularTotalSumaDeCapacidadesDeCadaUsuario(usuarios, 500.0);

    assertEquals(1000.0, u1.getCapacidadRelativaPagoMensual());
    assertEquals(2000.0, u2.getCapacidadRelativaPagoMensual());

    servicioCalculosGasto.establecerMontoTotalDeLasSumasDeCapacidades(usuarios);
    assertEquals(3000.0, servicioCalculosGasto.getMontoCapacidadTotalDeLosUsuarios());
  }

  @Test
  public void queCalculePorcentajesDeCapacidadCorrectamente() {
    Usuario u1 = new Usuario();
    u1.setCapacidadRelativaPagoMensual(1000.0);
    Usuario u2 = new Usuario();
    u2.setCapacidadRelativaPagoMensual(3000.0);

    List<Usuario> usuarios = Arrays.asList(u1, u2);

    // Establecemos el pozo total (1000 + 3000 = 4000)
    servicioCalculosGasto.establecerMontoTotalDeLasSumasDeCapacidades(usuarios);
    servicioCalculosGasto.calcularPorcentajesDeCapacidadMensualDeUsuarios(usuarios);

    assertEquals(25.0, u1.getPorcentajeSegunCapacidadDePagoMensual());
    assertEquals(75.0, u2.getPorcentajeSegunCapacidadDePagoMensual());
  }

  @Test
  public void queCalculeCuotasMensualesDeUsuariosCorrectamente() {
    Usuario u1 = new Usuario();
    u1.setPorcentajeSegunCapacidadDePagoMensual(30.0);
    Usuario u2 = new Usuario();
    u2.setPorcentajeSegunCapacidadDePagoMensual(70.0);

    List<Usuario> usuarios = Arrays.asList(u1, u2);
    Double montoGastosOcasionales = 2000.0;

    servicioCalculosGasto.calcularCuotasMensualesDeUsuarios(usuarios, montoGastosOcasionales);

    assertEquals(600.0, u1.getCuotaMensual());
    assertEquals(1400.0, u2.getCuotaMensual());
  }

  @Test
  public void queCalculeHorasDeTareasDeUsuariosConsiderandoBrechaYRedondeo() {
    Usuario u1 = new Usuario();
    u1.setCuotaMensual(200.0); // Cuota menor -> brecha de 300 con la máxima (500)
    Usuario u2 = new Usuario();
    u2.setCuotaMensual(500.0); // Cuota máxima -> 0 horas

    List<Usuario> usuarios = Arrays.asList(u1, u2);
    Double valorHoraTarea = 100.0; // Brecha de 300 / 100 = 3.0 horas

    servicioCalculosGasto.calcularHorasDeTareasDeUsuarios(usuarios, valorHoraTarea);

    assertEquals(3.0, u1.getHorasTareaARealizar());
    assertEquals(0.0, u2.getHorasTareaARealizar());
  }
}
