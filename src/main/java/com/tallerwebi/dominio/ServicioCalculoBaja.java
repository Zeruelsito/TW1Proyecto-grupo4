package com.tallerwebi.dominio;

import java.util.List;
import java.util.Map;

public interface ServicioCalculoBaja {
  double[] calcularDeudaHorasTarea(Usuario saliente, Hogar hogar, List<Gasto> gastos);

  Map<String, Double> calcularBalancesConBaja(
    List<Usuario> integrantes,
    List<Usuario> restantes,
    Usuario saliente,
    List<Gasto> gastos,
    double deudaHorasPesos,
    Long hogarId
  );

  List<TransferenciaSugerida> liquidacionDelSaliente(
    String nombreSaliente,
    Map<String, Double> balances
  );

  double totalGastosFijosDelMes(List<Gasto> gastos);

  boolean parametrosValidos(Hogar hogar);

  List<FilaCuota> armarFilasCuota(
    List<Usuario> integrantes,
    List<Usuario> restantes,
    double totalFijos,
    Hogar hogar,
    boolean cuotasCalculables
  );

  void recalcularYGuardarCuotasRestantes(List<Usuario> restantes, Hogar hogar);
}
