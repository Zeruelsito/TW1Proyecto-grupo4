package com.tallerwebi.dominio;

import java.util.List;
import java.util.Map;

public interface ServicioLiquidacion {
  List<TransferenciaSugerida> calcularLiquidacionOptima(Map<String, Double> balances);
  List<TransferenciaSugerida> obtenerLiquidacionDelMes(Hogar hogar, List<Gasto> gastos);
}
