package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.HogarSinIntegrantesException;
import com.tallerwebi.dominio.excepcion.IntegranteConSaldoPendienteException;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.excepcion.UsuarioYaDadoDeBajaException;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ServicioBajaIntegranteImpl implements ServicioBajaIntegrante {

  private static final int CUOTA = 0;
  private static final int HORAS = 1;
  private static final double MARGEN_SALDO = 0.01;

  private final RepositorioUsuario repositorioUsuario;
  private final RepositorioGasto repositorioGasto;
  private final ServicioLiquidacion servicioLiquidacion;
  private final ServicioCalculosGasto servicioCalculosGasto;
  private final ServicioHogar servicioHogar;
  private final ServicioCreditoTarea servicioCreditoTarea;
  private final HttpSession session; // <-- Inyectamos la sesión para obtener el hogarId actual

  @Autowired
  public ServicioBajaIntegranteImpl(
    RepositorioUsuario repositorioUsuario,
    RepositorioGasto repositorioGasto,
    ServicioLiquidacion servicioLiquidacion,
    ServicioCalculosGasto servicioCalculosGasto,
    ServicioHogar servicioHogar,
    ServicioCreditoTarea servicioCreditoTarea,
    HttpSession session
  ) {
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioGasto = repositorioGasto;
    this.servicioLiquidacion = servicioLiquidacion;
    this.servicioCalculosGasto = servicioCalculosGasto;
    this.servicioHogar = servicioHogar;
    this.servicioCreditoTarea = servicioCreditoTarea;
    this.session = session;
  }

  // Método auxiliar para obtener el hogarId actual de la sesión
  private Long obtenerHogarIdActual() {
    Long hogarId = (Long) session.getAttribute("HOGAR_ID");
    if (hogarId == null) {
      // Fallback por seguridad o si el hogar viene directo del servicioHogar
      Hogar hogarActivo = servicioHogar.obtenerHogar();
      if (hogarActivo != null) {
        hogarId = hogarActivo.getId();
      }
    }
    return hogarId;
  }

  @Override
  public List<Usuario> obtenerIntegrantesActivos() {
    return repositorioUsuario.obtenerIntegrantesDelHogar(obtenerHogarIdActual());
  }

  @Override
  public SimulacionBaja simularBaja(Long usuarioId) {
    Long hogarId = obtenerHogarIdActual();
    Usuario saliente = validarSaliente(usuarioId, hogarId);
    List<Usuario> integrantes = repositorioUsuario.obtenerIntegrantesDelHogar(hogarId);
    List<Usuario> restantes = sinElSaliente(integrantes, saliente);
    List<Gasto> gastos = repositorioGasto.obtenerTodosPorHogar(hogarId);

    Map<String, Double> balances = calcularBalances(integrantes, gastos);
    String nombreSaliente = nombreDe(saliente);
    Double balanceFinal = redondear(balances.getOrDefault(nombreSaliente, 0.0));
    List<TransferenciaSugerida> liquidacionFinal = transferenciasDe(
      nombreSaliente,
      servicioLiquidacion.calcularLiquidacionOptima(balances)
    );

    double totalFijos = totalGastosFijosDelMes(gastos);
    Hogar hogar = servicioHogar.obtenerHogar();
    boolean cuotasCalculables = parametrosValidos(hogar);
    List<FilaCuota> filas = armarFilasCuota(
      integrantes,
      restantes,
      totalFijos,
      hogar,
      cuotasCalculables
    );

    return new SimulacionBaja(
      saliente,
      balanceFinal,
      liquidacionFinal,
      filas,
      redondear(totalFijos),
      cuotasCalculables
    );
  }

  @Override
  public void confirmarBaja(Long usuarioId) {
    Long hogarId = obtenerHogarIdActual();
    Usuario saliente = validarSaliente(usuarioId, hogarId);
    List<Usuario> integrantes = repositorioUsuario.obtenerIntegrantesDelHogar(hogarId);
    SimulacionBaja simulacion = simularBaja(usuarioId);
    if (simulacion.isSaldoPendiente()) {
      throw new IntegranteConSaldoPendienteException();
    }
    List<Usuario> restantes = sinElSaliente(integrantes, saliente);

    saliente.setFechaBaja(LocalDateTime.now());
    repositorioUsuario.modificar(saliente);

    Hogar hogar = servicioHogar.obtenerHogar();
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

  private boolean parametrosValidos(Hogar hogar) {
    return (hogar != null && hogar.getPisoMinimoHogar() != null && hogar.getPisoMinimoHogar() > 0);
  }

  private Map<String, Double> calcularBalances(List<Usuario> integrantes, List<Gasto> gastos) {
    Map<Long, Double> consumo = new HashMap<>();
    Map<Long, Double> pagado = new HashMap<>();
    for (Usuario usuario : integrantes) {
      consumo.put(usuario.getId(), 0.0);
      pagado.put(usuario.getId(), 0.0);
    }

    for (Gasto gasto : gastos) {
      repartirGasto(gasto, integrantes, consumo, pagado);
    }

    Map<String, Double> balances = new LinkedHashMap<>();
    for (Usuario usuario : integrantes) {
      Long id = usuario.getId();
      balances.put(nombreDe(usuario), pagado.get(id) + creditoDe(usuario) - consumo.get(id));
    }
    return balances;
  }

  private void repartirGasto(
    Gasto gasto,
    List<Usuario> integrantes,
    Map<Long, Double> consumo,
    Map<Long, Double> pagado
  ) {
    if (gasto.getMonto() == null) {
      return;
    }
    List<Usuario> participantes = participantesDe(gasto, integrantes);
    double parte = gasto.getMonto() / participantes.size();
    for (Usuario usuario : participantes) {
      consumo.merge(usuario.getId(), parte, Double::sum);
    }
    Usuario pagador = gasto.getUsuarioPagador();
    if (pagador != null && pagado.containsKey(pagador.getId())) {
      pagado.merge(pagador.getId(), gasto.getMonto(), Double::sum);
    }
  }

  private List<Usuario> participantesDe(Gasto gasto, List<Usuario> integrantes) {
    List<Usuario> participantes = new ArrayList<>();
    for (Usuario usuario : integrantes) {
      if (usuario.participaEn(gasto.getFecha())) {
        participantes.add(usuario);
      }
    }
    if (participantes.isEmpty()) {
      return integrantes;
    }
    return participantes;
  }

  private List<TransferenciaSugerida> transferenciasDe(
    String nombre,
    List<TransferenciaSugerida> todas
  ) {
    List<TransferenciaSugerida> resultado = new ArrayList<>();
    for (TransferenciaSugerida transferencia : todas) {
      if (nombre.equals(transferencia.getEmisor()) || nombre.equals(transferencia.getReceptor())) {
        resultado.add(transferencia);
      }
    }
    return resultado;
  }

  private double creditoDe(Usuario usuario) {
    if (servicioCreditoTarea == null || usuario.getId() == null) {
      return 0.0;
    }
    Double credito = servicioCreditoTarea.obtenerCreditoAcumuladoPorUsuario(usuario.getId());
    return credito != null ? credito : 0.0;
  }

  private double totalGastosFijosDelMes(List<Gasto> gastos) {
    YearMonth mesActual = YearMonth.now();
    double total = 0.0;
    for (Gasto gasto : gastos) {
      boolean esFijo = gasto.getTipoGasto() == TipoGasto.FIJO;
      boolean esDelMes =
        gasto.getFecha() == null || YearMonth.from(gasto.getFecha()).equals(mesActual);
      if (esFijo && esDelMes && gasto.getMonto() != null) {
        total += gasto.getMonto();
      }
    }
    return total;
  }

  private List<FilaCuota> armarFilasCuota(
    List<Usuario> integrantes,
    List<Usuario> restantes,
    double totalFijos,
    Hogar hogar,
    boolean cuotasCalculables
  ) {
    double parteFijaAntes = redondear(totalFijos / integrantes.size());
    double parteFijaDespues = redondear(totalFijos / restantes.size());

    Map<Long, Double[]> antes = new HashMap<>();
    Map<Long, Double[]> despues = new HashMap<>();
    if (cuotasCalculables) {
      antes = calcularCuotasSobreCopias(integrantes, hogar);
      despues = calcularCuotasSobreCopias(restantes, hogar);
    }

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
      Double ingreso = original.getIngresoMensual() != null ? original.getIngresoMensual() : 0.0;
      Usuario copia = new Usuario(original.getNombre(), ingreso);
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
      Double horas = copia.getHorasTareaARealizar() != null ? copia.getHorasTareaARealizar() : 0.0;
      resultado.put(copia.getId(), new Double[] { copia.getCuotaMensual(), horas });
    }
    return resultado;
  }

  private Double valorDe(Map<Long, Double[]> valores, Long id, int posicion) {
    Double[] fila = valores.get(id);
    return fila == null ? null : fila[posicion];
  }

  private List<Usuario> sinElSaliente(List<Usuario> integrantes, Usuario saliente) {
    List<Usuario> restantes = new ArrayList<>();
    for (Usuario usuario : integrantes) {
      if (!usuario.getId().equals(saliente.getId())) {
        restantes.add(usuario);
      }
    }
    return restantes;
  }

  private String nombreDe(Usuario usuario) {
    return usuario.getNombre() != null ? usuario.getNombre() : "Usuario " + usuario.getId();
  }

  private double redondear(double valor) {
    return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }
}
