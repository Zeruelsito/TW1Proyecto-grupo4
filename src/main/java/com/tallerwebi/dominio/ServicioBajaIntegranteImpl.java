package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.HogarSinIntegrantesException;
import com.tallerwebi.dominio.excepcion.IntegranteConSaldoPendienteException;
import com.tallerwebi.dominio.excepcion.PagoNoCorrespondeALiquidacionException;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.excepcion.UsuarioYaDadoDeBajaException;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ServicioBajaIntegranteImpl implements ServicioBajaIntegrante {

  private static final String CLAVE_HOGAR_ID = "HOGAR_ID";
  private static final String CLAVE_HOGAR_ACTIVO = "hogarActivoEnSession";
  private static final double MONTO_CERO = 0.0;
  private static final double MARGEN_MONTO = 0.01;

  private final RepositorioUsuario repositorioUsuario;
  private final RepositorioGasto repositorioGasto;
  private final RepositorioHogar repositorioHogar;
  private final ServicioCalculoBaja servicioCalculoBaja;
  private final ServicioHogar servicioHogar;
  private final ServicioPagoRealizado servicioPagoRealizado;
  private final ServicioPagoPendiente servicioPagoPendiente;
  private final HttpSession session;

  @Autowired
  public ServicioBajaIntegranteImpl(
    RepositorioUsuario repositorioUsuario,
    RepositorioGasto repositorioGasto,
    RepositorioHogar repositorioHogar,
    ServicioCalculoBaja servicioCalculoBaja,
    ServicioHogar servicioHogar,
    ServicioPagoRealizado servicioPagoRealizado,
    ServicioPagoPendiente servicioPagoPendiente,
    HttpSession session
  ) {
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioGasto = repositorioGasto;
    this.repositorioHogar = repositorioHogar;
    this.servicioCalculoBaja = servicioCalculoBaja;
    this.servicioHogar = servicioHogar;
    this.servicioPagoRealizado = servicioPagoRealizado;
    this.servicioPagoPendiente = servicioPagoPendiente;
    this.session = session;
  }

  private Long obtenerHogarIdActual() {
    Long hogarId = (Long) session.getAttribute(CLAVE_HOGAR_ID);
    if (hogarId != null) {
      return hogarId;
    }
    Hogar hogarActivo = servicioHogar.obtenerHogar();
    return hogarActivo != null ? hogarActivo.getId() : null;
  }

  @Override
  public List<Usuario> obtenerIntegrantesActivos() {
    return repositorioUsuario.obtenerIntegrantesDelHogar(obtenerHogarIdActual());
  }

  @Override
  public SimulacionBaja simularBaja(Long usuarioId) {
    Long hogarId = obtenerHogarIdActual();
    Hogar hogar = resolverHogar(hogarId);
    Usuario saliente = validarSaliente(usuarioId, hogarId);
    List<Usuario> integrantes = repositorioUsuario.obtenerIntegrantesDelHogar(hogarId);
    List<Usuario> restantes = sinElSaliente(integrantes, saliente);
    List<Gasto> gastos = repositorioGasto.obtenerTodosPorHogar(hogarId);

    double[] deudaHorasInfo = servicioCalculoBaja.calcularDeudaHorasTarea(saliente, hogar, gastos);
    double deudaHorasPesos = redondear(deudaHorasInfo[0]);
    double horasAdeudadas = redondear(deudaHorasInfo[1]);

    Map<String, Double> balances = servicioCalculoBaja.calcularBalancesConBaja(
      integrantes,
      restantes,
      saliente,
      gastos,
      deudaHorasPesos,
      hogarId
    );

    String nombreSaliente = nombreDe(saliente);
    Double balanceFinal = redondear(balances.getOrDefault(nombreSaliente, 0.0));
    List<TransferenciaSugerida> liquidacionFinal = calcularLiquidacionSimulacion(
      saliente,
      nombreSaliente,
      restantes,
      balances,
      deudaHorasPesos,
      hogarId
    );

    Set<String> clavesPendientes = obtenerClavesPendientes(hogar);
    double totalFijos = servicioCalculoBaja.totalGastosFijosDelMes(gastos);
    boolean cuotasCalculables = servicioCalculoBaja.parametrosValidos(hogar);
    List<FilaCuota> filas = servicioCalculoBaja.armarFilasCuota(
      integrantes,
      restantes,
      totalFijos,
      hogar,
      cuotasCalculables
    );

    return new SimulacionBaja(
      saliente,
      balanceFinal,
      deudaHorasPesos,
      horasAdeudadas,
      liquidacionFinal,
      clavesPendientes,
      filas,
      redondear(totalFijos),
      cuotasCalculables
    );
  }

  @Override
  public void confirmarBaja(Long usuarioId) {
    Long hogarId = obtenerHogarIdActual();
    Hogar hogar = resolverHogar(hogarId);
    Usuario saliente = validarSaliente(usuarioId, hogarId);
    List<Usuario> integrantes = repositorioUsuario.obtenerIntegrantesDelHogar(hogarId);
    SimulacionBaja simulacion = simularBaja(usuarioId);

    if (simulacion.isSaldoPendiente()) {
      throw new IntegranteConSaldoPendienteException();
    }

    if (simulacion.isDebeCobrar()) {
      limpiarDeudasAFavorDelSaliente(simulacion, hogar, integrantes);
    }

    List<Usuario> restantes = sinElSaliente(integrantes, saliente);
    quitarIntegranteDeHogar(hogar, saliente);
    servicioCalculoBaja.recalcularYGuardarCuotasRestantes(restantes, hogar);

    Long usuarioLogueadoId = obtenerUsuarioLogueadoId();
    if (usuarioLogueadoId != null && usuarioLogueadoId.equals(saliente.getId())) {
      actualizarSesionPostBaja(saliente.getId(), hogarId);
    }
  }

  @Override
  public void registrarPago(Long usuarioId, Usuario emisor, Usuario receptor, Double monto) {
    SimulacionBaja simulacion = simularBaja(usuarioId);
    if (!simulacion.tieneTransferenciaPendiente(emisor, receptor, monto)) {
      throw new PagoNoCorrespondeALiquidacionException();
    }
    Hogar hogar = resolverHogar(obtenerHogarIdActual());
    PagoRealizado pago = new PagoRealizado(emisor, receptor, monto, LocalDateTime.now(), hogar);
    servicioPagoRealizado.registrarPagoExitoso(pago);
  }

  private List<TransferenciaSugerida> calcularLiquidacionSimulacion(
    Usuario saliente,
    String nombreSaliente,
    List<Usuario> restantes,
    Map<String, Double> balancesConPagos,
    double deudaHorasPesos,
    Long hogarId
  ) {
    List<PagoRealizado> pagos = servicioPagoRealizado != null && hogarId != null
      ? servicioPagoRealizado.obtenerHistorialPagos(hogarId)
      : Collections.emptyList();

    Map<String, Double> balancesSinPagos = revertirPagosEnBalances(balancesConPagos, pagos);
    List<TransferenciaSugerida> base = servicioCalculoBaja.liquidacionDelSaliente(
      nombreSaliente,
      balancesSinPagos
    );

    Map<String, Double> montoHaciaRestante = new HashMap<>();
    Map<String, String> emailPorRestante = new HashMap<>();
    Map<String, Long> idPorRestante = new HashMap<>();

    for (Usuario r : restantes) {
      String nombreR = nombreDe(r);
      montoHaciaRestante.put(nombreR, MONTO_CERO);
      emailPorRestante.put(nombreR, r.getEmail());
      idPorRestante.put(nombreR, r.getId());
    }

    for (TransferenciaSugerida t : base) {
      if (nombreSaliente.equals(t.getEmisor())) {
        emailPorRestante.putIfAbsent(t.getReceptor(), t.getEmailEmisor());
        idPorRestante.putIfAbsent(t.getReceptor(), t.getReceptorId());
        montoHaciaRestante.merge(
          t.getReceptor(),
          t.getMonto() != null ? t.getMonto() : MONTO_CERO,
          Double::sum
        );
      }
    }

    if (deudaHorasPesos > MARGEN_MONTO && !restantes.isEmpty()) {
      double parteCasaPorRestante = deudaHorasPesos / restantes.size();
      for (Usuario r : restantes) {
        montoHaciaRestante.merge(nombreDe(r), parteCasaPorRestante, Double::sum);
      }
    }

    List<TransferenciaSugerida> resultado = new ArrayList<>();
    for (Map.Entry<String, Double> entry : montoHaciaRestante.entrySet()) {
      String nombreReceptor = entry.getKey();
      double bruto = entry.getValue();
      double yaPagado = sumarPagosDirectos(pagos, nombreSaliente, nombreReceptor);
      double pendiente = redondear(bruto - yaPagado);
      if (pendiente > MARGEN_MONTO) {
        resultado.add(
          new TransferenciaSugerida(
            saliente.getId(),
            nombreSaliente,
            saliente.getEmail(),
            idPorRestante.get(nombreReceptor),
            nombreReceptor,
            pendiente
          )
        );
      }
    }
    return resultado;
  }

  private Map<String, Double> revertirPagosEnBalances(
    Map<String, Double> balancesConPagos,
    List<PagoRealizado> pagos
  ) {
    Map<String, Double> copia = new HashMap<>(balancesConPagos);
    for (PagoRealizado pago : pagos) {
      if (pago.getMonto() != null && pago.getEmisor() != null && pago.getReceptor() != null) {
        copia.computeIfPresent(pago.getEmisor().getNombre(), (k, v) -> v - pago.getMonto());
        copia.computeIfPresent(pago.getReceptor().getNombre(), (k, v) -> v + pago.getMonto());
      }
    }
    return copia;
  }

  private double sumarPagosDirectos(
    List<PagoRealizado> pagos,
    String emisorNombre,
    String receptorNombre
  ) {
    double total = MONTO_CERO;
    for (PagoRealizado pago : pagos) {
      if (
        pago.getMonto() != null &&
        pago.getEmisor() != null &&
        pago.getReceptor() != null &&
        emisorNombre != null &&
        emisorNombre.equals(pago.getEmisor().getNombre()) &&
        receptorNombre != null &&
        receptorNombre.equals(pago.getReceptor().getNombre())
      ) {
        total += pago.getMonto();
      }
    }
    return total;
  }

  private void quitarIntegranteDeHogar(Hogar hogar, Usuario saliente) {
    if (hogar == null || hogar.getIntegrantes() == null) {
      return;
    }
    hogar.getIntegrantes().removeIf(u -> u.getId().equals(saliente.getId()));
    if (repositorioHogar != null) {
      repositorioHogar.actualizar(hogar);
    }
  }

  private Hogar resolverHogar(Long hogarId) {
    if (hogarId != null && servicioHogar != null) {
      Hogar porId = servicioHogar.seleccionarHogarPorId(hogarId);
      if (porId != null) {
        return porId;
      }
    }
    return servicioHogar != null ? servicioHogar.obtenerHogar() : null;
  }

  private Long obtenerUsuarioLogueadoId() {
    for (String clave : List.of("ID", "usuarioId", "USUARIO_ID")) {
      Object valor = session.getAttribute(clave);
      if (valor instanceof Long id) {
        return id;
      }
    }
    return null;
  }

  private Set<String> obtenerClavesPendientes(Hogar hogar) {
    Set<String> clavesPendientes = new HashSet<>();
    if (hogar == null || servicioPagoPendiente == null) {
      return clavesPendientes;
    }
    List<PagoPendiente> pendientes = servicioPagoPendiente.obtenerPendientes(hogar);
    if (pendientes == null) {
      return clavesPendientes;
    }
    for (PagoPendiente p : pendientes) {
      if (p.getEmisor() != null && p.getReceptor() != null && p.getMonto() != null) {
        clavesPendientes.add(
          p.getEmisor().getNombre() + "_" + p.getReceptor().getNombre() + "_" + p.getMonto()
        );
      }
    }
    return clavesPendientes;
  }

  private void limpiarDeudasAFavorDelSaliente(
    SimulacionBaja simulacion,
    Hogar hogar,
    List<Usuario> integrantes
  ) {
    if (hogar == null) {
      return;
    }
    String nombreSaliente = nombreDe(simulacion.getUsuarioSaliente());
    for (TransferenciaSugerida t : simulacion.getLiquidacionFinal()) {
      if (!nombreSaliente.equals(t.getReceptor())) {
        continue;
      }
      Usuario deudor = integrantes
        .stream()
        .filter(u -> nombreDe(u).equals(t.getEmisor()))
        .findFirst()
        .orElse(null);
      if (deudor != null) {
        PagoRealizado condonacion = new PagoRealizado(
          deudor,
          simulacion.getUsuarioSaliente(),
          t.getMonto(),
          LocalDateTime.now(),
          hogar
        );
        servicioPagoRealizado.registrarPagoExitoso(condonacion);
      }
    }
  }

  private void actualizarSesionPostBaja(Long usuarioId, Long hogarAbandonadoId) {
    List<Hogar> otrosHogares = new ArrayList<>(servicioHogar.obtenerHogares(usuarioId));
    otrosHogares.removeIf(h -> h.getId() != null && h.getId().equals(hogarAbandonadoId));

    if (!otrosHogares.isEmpty()) {
      Hogar siguienteHogar = servicioHogar.seleccionarHogarPorId(otrosHogares.get(0).getId());
      session.setAttribute(CLAVE_HOGAR_ID, siguienteHogar.getId());
      session.setAttribute(CLAVE_HOGAR_ACTIVO, siguienteHogar);
    } else {
      session.removeAttribute(CLAVE_HOGAR_ID);
      session.removeAttribute(CLAVE_HOGAR_ACTIVO);
    }
  }

  private Usuario validarSaliente(Long usuarioId, Long hogarId) {
    Usuario saliente = usuarioId == null ? null : repositorioUsuario.buscarPorId(usuarioId);
    if (saliente == null) {
      throw new UsuarioNoEncontrado();
    }
    if (saliente.estaDeBaja()) {
      throw new UsuarioYaDadoDeBajaException();
    }
    if (sinElSaliente(repositorioUsuario.obtenerIntegrantesDelHogar(hogarId), saliente).isEmpty()) {
      throw new HogarSinIntegrantesException();
    }
    return saliente;
  }

  private List<Usuario> sinElSaliente(List<Usuario> integrantes, Usuario saliente) {
    List<Usuario> restantes = new ArrayList<>(integrantes);
    restantes.removeIf(usuario -> usuario.getId().equals(saliente.getId()));
    return restantes;
  }

  private String nombreDe(Usuario usuario) {
    return usuario.getNombre() != null ? usuario.getNombre() : "Usuario " + usuario.getId();
  }

  private double redondear(double valor) {
    return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }
}