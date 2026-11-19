package com.tallerwebi.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CalculadorCuotasBaja {

  private static final int CUOTA = 0;
  private static final int HORAS = 1;
  private static final double MARGEN_SALDO = 0.01;

  private final RepositorioUsuario repositorioUsuario;
  private final ServicioCalculosGasto servicioCalculosGasto;

  @Autowired
  public CalculadorCuotasBaja(
    RepositorioUsuario repositorioUsuario,
    ServicioCalculosGasto servicioCalculosGasto
  ) {
    this.repositorioUsuario = repositorioUsuario;
    this.servicioCalculosGasto = servicioCalculosGasto;
  }

  public List<TransferenciaSugerida> combinarLiquidacionConDeudaHoras(
    Usuario saliente,
    List<Usuario> restantes,
    List<TransferenciaSugerida> transferenciasGastos,
    double deudaHorasPesos
  ) {
    String nombreSaliente = nombreDe(saliente);
    Map<String, Double> netoHaciaRestante = new LinkedHashMap<>();
    Map<String, String> emailPorNombre = new HashMap<>();
    Map<String, Long> idPorNombre = new HashMap<>();

    cargarDatosRestantes(restantes, netoHaciaRestante, emailPorNombre, idPorNombre);
    aplicarTransferenciasDeGastos(
      transferenciasGastos,
      nombreSaliente,
      netoHaciaRestante,
      emailPorNombre,
      idPorNombre
    );
    aplicarDeudaHorasEquitativa(deudaHorasPesos, restantes, netoHaciaRestante);

    return construirTransferenciasFinales(
      saliente,
      nombreSaliente,
      netoHaciaRestante,
      emailPorNombre,
      idPorNombre
    );
  }

  private void cargarDatosRestantes(
    List<Usuario> restantes,
    Map<String, Double> netoHaciaRestante,
    Map<String, String> emailPorNombre,
    Map<String, Long> idPorNombre
  ) {
    for (Usuario r : restantes) {
      String nombre = nombreDe(r);
      netoHaciaRestante.put(nombre, 0.0);
      emailPorNombre.put(nombre, r.getEmail());
      idPorNombre.put(nombre, r.getId());
    }
  }

  private void aplicarTransferenciasDeGastos(
    List<TransferenciaSugerida> transferencias,
    String nombreSaliente,
    Map<String, Double> netoHaciaRestante,
    Map<String, String> emailPorNombre,
    Map<String, Long> idPorNombre
  ) {
    if (transferencias == null) {
      return;
    }
    for (TransferenciaSugerida t : transferencias) {
      double monto = t.getMonto() != null ? t.getMonto() : 0.0;
      if (nombreSaliente.equals(t.getEmisor())) {
        emailPorNombre.putIfAbsent(t.getReceptor(), t.getEmailEmisor());
        idPorNombre.putIfAbsent(t.getReceptor(), t.getReceptorId());
        netoHaciaRestante.merge(t.getReceptor(), monto, Double::sum);
      } else if (nombreSaliente.equals(t.getReceptor())) {
        emailPorNombre.putIfAbsent(t.getEmisor(), t.getEmailEmisor());
        idPorNombre.putIfAbsent(t.getEmisor(), t.getEmisorId());
        netoHaciaRestante.merge(t.getEmisor(), -monto, Double::sum);
      }
    }
  }

  private void aplicarDeudaHorasEquitativa(
    double deudaHorasPesos,
    List<Usuario> restantes,
    Map<String, Double> netoHaciaRestante
  ) {
    if (deudaHorasPesos <= 0.0 || restantes == null || restantes.isEmpty()) {
      return;
    }
    double parteEquitativa = deudaHorasPesos / restantes.size();
    for (Usuario r : restantes) {
      netoHaciaRestante.merge(nombreDe(r), parteEquitativa, Double::sum);
    }
  }

  private List<TransferenciaSugerida> construirTransferenciasFinales(
    Usuario saliente,
    String nombreSaliente,
    Map<String, Double> netoHaciaRestante,
    Map<String, String> emailPorNombre,
    Map<String, Long> idPorNombre
  ) {
    List<TransferenciaSugerida> resultado = new ArrayList<>();
    for (Map.Entry<String, Double> entry : netoHaciaRestante.entrySet()) {
      double valor = redondear(entry.getValue());
      if (Math.abs(valor) < MARGEN_SALDO) {
        continue;
      }
      String otroNombre = entry.getKey();
      if (valor > 0) {
        resultado.add(
          new TransferenciaSugerida(
            saliente.getId(),
            nombreSaliente,
            saliente.getEmail(),
            idPorNombre.get(otroNombre),
            otroNombre,
            valor
          )
        );
      } else {
        resultado.add(
          new TransferenciaSugerida(
            idPorNombre.get(otroNombre),
            otroNombre,
            emailPorNombre.get(otroNombre),
            saliente.getId(),
            nombreSaliente,
            Math.abs(valor)
          )
        );
      }
    }
    return resultado;
  }

  public double totalGastosFijosDelMes(List<Gasto> gastos) {
    YearMonth mesActual = YearMonth.now();
    double total = 0.0;
    for (Gasto gasto : gastos) {
      if (esGastoFijoDelMes(gasto, mesActual)) {
        total += gasto.getMonto();
      }
    }
    return total;
  }

  public boolean parametrosValidos(Hogar hogar) {
    return hogar != null && hogar.getPisoMinimoHogar() != null && hogar.getPisoMinimoHogar() > 0;
  }

  public List<FilaCuota> armarFilasCuota(
    List<Usuario> integrantes,
    List<Usuario> restantes,
    double totalFijos,
    Hogar hogar,
    boolean cuotasCalculables
  ) {
    double parteFijaAntes = redondear(totalFijos / integrantes.size());
    double parteFijaDespues = redondear(totalFijos / restantes.size());

    Map<Long, Double[]> antes = cuotasCalculables
      ? calcularCuotasSobreCopias(integrantes, hogar)
      : new HashMap<>();
    Map<Long, Double[]> despues = cuotasCalculables
      ? calcularCuotasSobreCopias(restantes, hogar)
      : new HashMap<>();

    List<FilaCuota> filas = new ArrayList<>();
    for (Usuario usuario : restantes) {
      Long id = usuario.getId();
      filas.add(
        new FilaCuota(
          nombreDe(usuario),
          valorDe(antes, id, CUOTA),
          valorDe(despues, id, CUOTA),
          valorDe(antes, id, HORAS),
          valorDe(despues, id, HORAS),
          parteFijaAntes,
          parteFijaDespues
        )
      );
    }
    return filas;
  }

  public void recalcularYGuardarCuotasRestantes(List<Usuario> restantes, Hogar hogar) {
    if (!parametrosValidos(hogar)) {
      return;
    }
    Map<Long, Double[]> nuevas = calcularCuotasSobreCopias(restantes, hogar);
    for (Usuario usuario : restantes) {
      usuario.setCuotaMensual(valorDe(nuevas, usuario.getId(), CUOTA));
      usuario.setHorasTareaARealizar(valorDe(nuevas, usuario.getId(), HORAS));
      repositorioUsuario.modificar(usuario);
    }
  }

  private boolean esGastoFijoDelMes(Gasto gasto, YearMonth mesActual) {
    if (gasto.getTipoGasto() != TipoGasto.FIJO || gasto.getMonto() == null) {
      return false;
    }
    return gasto.getFecha() == null || YearMonth.from(gasto.getFecha()).equals(mesActual);
  }

  private Map<Long, Double[]> calcularCuotasSobreCopias(List<Usuario> originales, Hogar hogar) {
    List<Usuario> copias = crearCopias(originales);
    Double piso = hogar.getPisoMinimoHogar();

    servicioCalculosGasto.calcularTotalSumaDeCapacidadesDeCadaUsuario(copias, piso);
    servicioCalculosGasto.establecerMontoTotalDeLasSumasDeCapacidades(copias);
    Double pozo = servicioCalculosGasto.getMontoCapacidadTotalDeLosUsuarios();

    if (pozo == null || pozo == 0.0) {
      asignarPisoATodos(copias, piso);
    } else {
      calcularCuotasYHoras(copias, piso, hogar.getPrecioHoraTarea());
    }
    return extraerResultado(copias);
  }

  private List<Usuario> crearCopias(List<Usuario> originales) {
    List<Usuario> copias = new ArrayList<>();
    for (Usuario original : originales) {
      Double ingreso = original.getIngresoMensual();
      Usuario copia = new Usuario(original.getNombre(), ingreso == null ? 0.0 : ingreso);
      copia.setId(original.getId());
      copias.add(copia);
    }
    return copias;
  }   

  private void asignarPisoATodos(List<Usuario> copias, Double piso) {
    for (Usuario copia : copias) {
      copia.setCuotaMensual(piso);
      copia.setHorasTareaARealizar(0.0);
    }
  }
  private void calcularCuotasYHoras(List<Usuario> copias, Double piso, Double precioHora) {
    servicioCalculosGasto.calcularPorcentajesDeCapacidadMensualDeUsuarios(copias);
    servicioCalculosGasto.calcularCuotasMensualesDeUsuarios(copias, piso);
    if (precioHora != null && precioHora > 0) {
      servicioCalculosGasto.calcularHorasDeTareasDeUsuarios(copias, precioHora);
    }
  }

  private Map<Long, Double[]> extraerResultado(List<Usuario> copias) {
    Map<Long, Double[]> resultado = new HashMap<>();
    for (Usuario copia : copias) {
      Double horas = copia.getHorasTareaARealizar();
      resultado.put(
        copia.getId(),
        new Double[] { copia.getCuotaMensual(), horas == null ? 0.0 : horas }
      );
    }
    return resultado;
  }

  private Double valorDe(Map<Long, Double[]> valores, Long id, int posicion) {
    Double[] fila = valores.get(id);
    return fila == null ? null : fila[posicion];
  }

  private String nombreDe(Usuario usuario) {
    return usuario.getNombre() != null ? usuario.getNombre() : "Usuario " + usuario.getId();
  }

  private double redondear(double valor) {
    return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }
}
