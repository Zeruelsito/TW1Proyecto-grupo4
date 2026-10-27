package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  @Transactional(readOnly = true)
  public List<TransferenciaSugerida> obtenerLiquidacionDelMes(Hogar hogar, List<Gasto> gastos) {
    if (hogar == null || hogar.getIntegrantes() == null || hogar.getIntegrantes().isEmpty()) {
      return Collections.emptyList();
    }

    List<Usuario> integrantes = hogar.getIntegrantes();
    double totalGastosHogar = calcularTotalGastos(gastos);
    double cuotaConsumoIndividual = totalGastosHogar / integrantes.size();

    Map<String, Double> balances = new HashMap<>();
    Map<String, String> emailToNombre = new HashMap<>(); // <--- Mapa auxiliar para recuperar el nombre por email

    for (Usuario usuario : integrantes) {
      double totalPagadoPorUsuario = calcularTotalPagadoPorUsuario(usuario, gastos);
      double creditoTareas = obtenerCreditoTareas(usuario);

      double balanceNeto = (totalPagadoPorUsuario + creditoTareas) - cuotaConsumoIndividual;

      String email = usuario.getEmail();
      String nombre = usuario.getNombre() != null ? usuario.getNombre() : "Usuario";

      emailToNombre.put(email, nombre); // Guardamos la relación email -> nombre
      balances.put(email, balanceNeto); // Usamos el email como clave única en los balances
    }

    return calcularLiquidacionOptimaConNombres(balances, emailToNombre);
  }

  private double calcularTotalGastos(List<Gasto> gastos) {
    if (gastos == null) {
      return 0.0;
    }
    double total = 0.0;
    for (Gasto gasto : gastos) {
      if (gasto.getMonto() != null && Boolean.TRUE.equals(gasto.getPagado())) {
        total += gasto.getMonto();
      }
    }
    return total;
  }

  private double calcularTotalPagadoPorUsuario(Usuario usuario, List<Gasto> gastos) {
    if (gastos == null || usuario == null || usuario.getId() == null) {
      return 0.0;
    }
    double totalPagado = 0.0;
    for (Gasto gasto : gastos) {
      if (
        gasto.getUsuarioPagador() != null &&
        gasto.getUsuarioPagador().getId() != null &&
        usuario.getId().equals(gasto.getUsuarioPagador().getId())
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

  // Método que calcula la liquidación óptima mapeando los emails a nombres reales
  public List<TransferenciaSugerida> calcularLiquidacionOptimaConNombres(
    Map<String, Double> balances,
    Map<String, String> emailToNombre
  ) {
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
        String emailEmisor = deudor.nombre; // Aquí se guarda el email
        String nombreEmisor = emailToNombre.getOrDefault(emailEmisor, emailEmisor);

        String emailReceptor = acreedor.nombre; // Aquí se guarda el email
        String nombreReceptor = emailToNombre.getOrDefault(emailReceptor, emailReceptor);

        // Pasamos el nombre para mostrar y el email para validar
        transferencias.add(
          new TransferenciaSugerida(nombreEmisor, emailEmisor, nombreReceptor, monto)
        );
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

  @Override
  public List<TransferenciaSugerida> calcularLiquidacionOptima(Map<String, Double> balances) {
    // Método por defecto por si se llama desde la interfaz sin el mapa de nombres
    return calcularLiquidacionOptimaConNombres(balances, Collections.emptyMap());
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

    String nombre; // Guarda el email como clave única
    double monto;

    PersonaBalance(String nombre, double monto) {
      this.nombre = nombre;
      this.monto = monto;
    }
  }
}
