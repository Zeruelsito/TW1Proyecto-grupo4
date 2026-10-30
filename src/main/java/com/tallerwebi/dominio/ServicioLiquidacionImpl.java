package com.tallerwebi.dominio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioLiquidacionImpl implements ServicioLiquidacion {

  private static final double MARGEN_TOLERANCIA = 0.01;

  private final ServicioCreditoTarea servicioCreditoTarea;
  private final ServicioPagoRealizado servicioPagoRealizado;

  @Autowired
  public ServicioLiquidacionImpl(
    ServicioCreditoTarea servicioCreditoTarea,
    ServicioPagoRealizado servicioPagoRealizado
  ) {
    this.servicioCreditoTarea = servicioCreditoTarea;
    this.servicioPagoRealizado = servicioPagoRealizado;
  }

  @Override
  @Transactional(readOnly = true)
  public List<TransferenciaSugerida> obtenerLiquidacionDelMes(Hogar hogar, List<Gasto> gastos) {
    if (hogar == null || hogar.getIntegrantes() == null || hogar.getIntegrantes().isEmpty()) {
      return Collections.emptyList();
    }

    List<Usuario> integrantes = hogar.getIntegrantes();
    Map<String, Double> balances = new HashMap<>();
    Map<String, String> emailToNombre = new HashMap<>();

    for (Usuario usuario : integrantes) {
      double totalPagadoPorUsuario = calcularTotalPagadoPorUsuario(usuario, gastos);
      double creditoTareas = obtenerCreditoTareas(usuario);

      // NUEVO: Calculamos el consumo real en base a los participantes de cada gasto
      double consumoIndividual = calcularConsumoIndividualPorUsuario(usuario, hogar, gastos);

      double balanceNeto = (totalPagadoPorUsuario + creditoTareas) - consumoIndividual;

      String email = usuario.getEmail();
      String nombre = usuario.getNombre() != null ? usuario.getNombre() : "Usuario";

      emailToNombre.put(email, nombre);
      balances.put(email, balanceNeto);
    }

    List<TransferenciaSugerida> transferencias = calcularLiquidacionOptimaConNombres(
      balances,
      emailToNombre
    );

    List<PagoRealizado> pagosRealizados = servicioPagoRealizado != null
      ? servicioPagoRealizado.obtenerHistorialPagos(hogar.getId())
      : Collections.emptyList();

    return transferencias
      .stream()
      .filter(t ->
        pagosRealizados
          .stream()
          .noneMatch(p ->
            p.getEmisor().equals(t.getEmisor()) &&
            p.getReceptor().equals(t.getReceptor()) &&
            Math.abs(p.getMonto() - t.getMonto()) < MARGEN_TOLERANCIA
          )
      )
      .collect(Collectors.toList());
  }

  // NUEVO MÉTODO: Calcula cuánto debe consumir cada usuario según los gastos en los que participa
  private double calcularConsumoIndividualPorUsuario(
    Usuario usuario,
    Hogar hogar,
    List<Gasto> gastos
  ) {
    if (gastos == null || usuario == null || usuario.getId() == null) return 0.0;
    double totalConsumo = 0.0;
    List<Usuario> integrantesHogar = hogar.getIntegrantes();

    for (Gasto gasto : gastos) {
      if (gasto.getMonto() != null && Boolean.TRUE.equals(gasto.getPagado())) {
        totalConsumo += acumularSiParticipa(gasto, usuario, integrantesHogar);
      }
    }
    return totalConsumo;
  }

  // MÉTODO AUXILIAR para mantener baja la complejidad ciclomática (PMD)
  private double acumularSiParticipa(Gasto gasto, Usuario usuario, List<Usuario> integrantesHogar) {
    List<Usuario> participantes = gasto.getParticipantes();

    // Si el gasto viejo no tiene participantes asignados, se divide entre todos por defecto
    if (participantes == null || participantes.isEmpty()) {
      participantes = integrantesHogar;
    }

    boolean participa = participantes.stream().anyMatch(p -> p.getId().equals(usuario.getId()));

    if (participa && !participantes.isEmpty()) {
      return gasto.getMonto() / participantes.size();
    }
    return 0.0;
  }

  @Transactional
  public void registrarPagoExitoso(Hogar hogar, String emisor, String receptor, Double monto) {
    if (servicioPagoRealizado != null && hogar != null) {
      PagoRealizado pago = new PagoRealizado(emisor, receptor, monto, LocalDateTime.now(), hogar);
      servicioPagoRealizado.registrarPagoExitoso(pago);
    }
  }

  @Transactional(readOnly = true)
  public List<PagoRealizado> obtenerHistorialPagos(Long hogarId) {
    if (servicioPagoRealizado == null || hogarId == null) return Collections.emptyList();
    return servicioPagoRealizado.obtenerHistorialPagos(hogarId);
  }

  private double calcularTotalPagadoPorUsuario(Usuario usuario, List<Gasto> gastos) {
    if (gastos == null || usuario == null || usuario.getId() == null) return 0.0;
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
        String emailEmisor = deudor.nombre;
        String nombreEmisor = emailToNombre.getOrDefault(emailEmisor, emailEmisor);

        String emailReceptor = acreedor.nombre;
        String nombreReceptor = emailToNombre.getOrDefault(emailReceptor, emailReceptor);

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

    String nombre;
    double monto;

    PersonaBalance(String nombre, double monto) {
      this.nombre = nombre;
      this.monto = monto;
    }
  }
}
