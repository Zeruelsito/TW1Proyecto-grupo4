package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tallerwebi.dominio.ServicioVencimiento;
import com.tallerwebi.dominio.ServicioVencimientoImpl;
import com.tallerwebi.dominio.Vencimiento;
import com.tallerwebi.dominio.excepcion.VencimientoInvalidoException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ServicioVencimientoTest {

  @Test
  public void debeDevolverLosVencimientosOrdenadosPorFecha() {
    ServicioVencimiento servicio = new ServicioVencimientoImpl();

    Vencimiento luz = new Vencimiento(1L, "Luz", LocalDate.of(2026, 10, 20));
    Vencimiento internet = new Vencimiento(2L, "Internet", LocalDate.of(2026, 10, 5));
    Vencimiento alquiler = new Vencimiento(3L, "Alquiler", LocalDate.of(2026, 10, 10));

    servicio.registrar(luz);
    servicio.registrar(internet);
    servicio.registrar(alquiler);

    List<Vencimiento> resultado = servicio.obtenerOrdenadosPorFecha();

    assertEquals(internet, resultado.get(0));
    assertEquals(alquiler, resultado.get(1));
    assertEquals(luz, resultado.get(2));
  }

  @Test
  public void noDebeRegistrarUnVencimientoSinFecha() {
    ServicioVencimiento servicio = new ServicioVencimientoImpl();
    Vencimiento sinFecha = new Vencimiento(1L, "Luz", null);

    assertThrows(VencimientoInvalidoException.class, () -> servicio.registrar(sinFecha));
  }

  @Test
  public void debeDevolverSoloLosVencimientosProximosOrdenados() {
    ServicioVencimiento servicio = new ServicioVencimientoImpl();
    LocalDate hoy = LocalDate.of(2026, 10, 8);

    Vencimiento luz = new Vencimiento(1L, "Luz", LocalDate.of(2026, 10, 20));
    Vencimiento internet = new Vencimiento(2L, "Internet", LocalDate.of(2026, 10, 5));
    Vencimiento alquiler = new Vencimiento(3L, "Alquiler", LocalDate.of(2026, 10, 10));

    servicio.registrar(luz);
    servicio.registrar(internet);
    servicio.registrar(alquiler);

    List<Vencimiento> resultado = servicio.obtenerProximos(hoy);

    assertEquals(2, resultado.size());
    assertEquals(alquiler, resultado.get(0));
    assertEquals(luz, resultado.get(1));
  }

  @Test
  public void debeIncluirElVencimientoQueVenceHoy() { //osea si cae justo el dia de hoy
    ServicioVencimiento servicio = new ServicioVencimientoImpl();
    LocalDate hoy = LocalDate.of(2026, 10, 8);
    Vencimiento venceHoy = new Vencimiento(1L, "Gas", hoy);
    servicio.registrar(venceHoy);

    List<Vencimiento> resultado = servicio.obtenerProximos(hoy);

    assertEquals(1, resultado.size());
    assertEquals(venceHoy, resultado.get(0));
  }
}
