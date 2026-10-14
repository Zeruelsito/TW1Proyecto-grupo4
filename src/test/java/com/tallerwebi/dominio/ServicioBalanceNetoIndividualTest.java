package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ServicioBalanceNetoIndividualTest {

  ServicioCalculosGasto servicioCalculosGasto = new ServicioCalculosGastoImpl();

  @Test
  public void deberiaCalcularCorrectamenteUsandoElServicio() {
    List<Gasto> gastosHechos = givenUnaListaDeGastosRegistrados();

    List<Usuario> usuarios = new ArrayList<>();
    Usuario usuario1 = new Usuario("Lucas", 250000d);
    Usuario usuario2 = new Usuario("Maca", 200000d);
    Usuario usuario3 = new Usuario("Lucas", 300000d);

    usuario1.setCuotaMensual(200000d);
    usuario2.setCuotaMensual(130000d);
    usuario3.setCuotaMensual(220000d);

    usuarios.add(usuario1);
    usuarios.add(usuario2);
    usuarios.add(usuario3);

    Map<Usuario, Double> listaAcreedores = new HashMap<Usuario, Double>();
    Map<Usuario, Double> listaDeudores = new HashMap<Usuario, Double>();

    for (Gasto gasto : gastosHechos) {}

    Double valorPisoMinimo = 150000d;
  }

  private List<Gasto> givenUnaListaDeGastosRegistrados() {
    List<Gasto> gastosHechos = new ArrayList<>();
    Usuario usuario1 = new Usuario("Juan", "juan@kumo.com", "1234");
    Gasto gasto1 = new Gasto("G001", TipoGasto.FIJO, "alquiler", 300000d, usuario1, "VARIOS");
    Gasto gasto2 = new Gasto("G002", TipoGasto.FIJO, "agua", 100000d, usuario1, "VARIOS");
    Gasto gasto3 = new Gasto("G003", TipoGasto.FIJO, "gas", 200000d, usuario1, "VARIOS");
    Gasto gasto4 = new Gasto("G004", TipoGasto.FIJO, "comida", 50000d, usuario1, "VARIOS");
    Gasto gasto5 = new Gasto("G005", TipoGasto.FIJO, "comida", 40000d, usuario1, "VARIOS");
    gastosHechos.add(gasto1);
    gastosHechos.add(gasto2);
    gastosHechos.add(gasto3);
    gastosHechos.add(gasto4);
    return gastosHechos;
  }
}
