package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioLiquidacionImpl implements ServicioLiquidacion {

  private static final double MARGEN_TOLERANCIA = 0.01;

  private final ServicioCreditoTarea servicioCreditoTarea;

  @Autowired
  public ServicioLiquidacionImpl(ServicioCreditoTarea servicioCreditoTarea) {
    this.servicioCreditoTarea = servicioCreditoTarea;
  }

  public ServicioLiquidacionImpl() {
    this.servicioCreditoTarea = null;
  }

  @Override
  public List<TransferenciaSugerida> obtenerLiquidacionDelMes(Hogar hogar, List<Gasto> gastos) {
    if (hogar == null || hogar.getIntegrantes() == null || hogar.getIntegrantes().isEmpty()) {
      return Collections.emptyList();
    }

    List<Usuario> integrantes = hogar.getIntegrantes();
    double totalGastosHogar = calcularTotalGastos(gastos);
    double cuotaConsumoIndividual = totalGastosHogar / integrantes.size();

    Map<String, Double> balances = new HashMap<>();

    for (Usuario usuario : integrantes) {
      double totalPagadoPorUsuario = calcularTotalPagadoPorUsuario(usuario, gastos);
      double creditoTareas = obtenerCreditoTareas(usuario);

      double balanceNeto = (totalPagadoPorUsuario + creditoTareas) - cuotaConsumoIndividual;
      String nombreUsuario = usuario.getNombre() != null
        ? usuario.getNombre()
        : "Usuario " + usuario.getId();

      balances.put(nombreUsuario, balanceNeto);
    }

    return calcularLiquidacionOptima(balances);
  }

  private double calcularTotalGastos(List<Gasto> gastos) {
    if (gastos == null) {
      return 0.0;
    }
    double total = 0.0;
    for (Gasto gasto : gastos) {
      if (gasto.getMonto() != null) {
        total += gasto.getMonto();
      }
    }
    return total;
  }

  private double calcularTotalPagadoPorUsuario(Usuario usuario, List<Gasto> gastos) {
    if (gastos == null) {
      return 0.0;
    }
    double totalPagado = 0.0;
    for (Gasto gasto : gastos) {
      if (
        gasto.getPagadorId() != null &&
        usuario.getId() != null &&
        usuario.getId().equals(gasto.getPagadorId())
      ) {
        totalPagado += gasto.getMonto();
      }
    }
    return totalPagado;
  }

  private double obtenerCreditoTareas(Usuario usuario) {
    if (servicioCreditoTarea != null && usuario.getId() != null) {
      return servicioCreditoTarea.obtenerCreditoAcumuladoPorUsuario(usuario.getId());
    }
    return 0.0;
  }

  @Override
  public List<TransferenciaSugerida> calcularLiquidacionOptima(Map<String, Double> balances) {
    List<TransferenciaSugerida> transferencias = new ArrayList<>();
    List<PersonaBalance> deudores = new ArrayList<>();
    List<PersonaBalance> acreedores = new ArrayList<>();

    clasificarBalances(balances, deudores, acreedores);

    int indiceDeudor = 0;
    int indiceAcreedor = 0;

    while (indiceDeudor < deudores.size() && indiceAcreedor < acreedores.size()) {
      PersonaBalance deudor = deudores.get(indiceDeudor);
      PersonaBalance acreedor = acreedores.get(indiceAcreedor);

      double monto = Math.min(deudor.monto, acreedor.monto);
      monto = Math.round(monto * 100.0) / 100.0;

      if (monto > 0) {
        transferencias.add(new TransferenciaSugerida(deudor.nombre, acreedor.nombre, monto));
      }

      deudor.monto -= monto;
      acreedor.monto -= monto;

      if (deudor.monto <= MARGEN_TOLERANCIA) {
        indiceDeudor++;
      }
      if (acreedor.monto <= MARGEN_TOLERANCIA) {
        indiceAcreedor++;
      }
    }

    return transferencias;
  }

  private void clasificarBalances(
    Map<String, Double> balances,
    List<PersonaBalance> deudores,
    List<PersonaBalance> acreedores
  ) {
    for (Map.Entry<String, Double> entry : balances.entrySet()) {
      double monto = entry.getValue();
      if (monto < -MARGEN_TOLERANCIA) {
        deudores.add(new PersonaBalance(entry.getKey(), -monto));
      } else if (monto > MARGEN_TOLERANCIA) {
        acreedores.add(new PersonaBalance(entry.getKey(), monto));
      }
    }
  }

  private static class PersonaBalance {

    String nombre;
    double monto;

    PersonaBalance(String nombre, double monto) {
      this.nombre = nombre;
      this.monto = monto;
    }
  }
}
