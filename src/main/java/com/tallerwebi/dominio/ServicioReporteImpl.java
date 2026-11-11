package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.HogarNoEncontradoException;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioReporteImpl implements ServicioReporte {

  private static final String SEPARADOR = ";";
  private static final String SALTO = "\r\n";
  private static final Locale LOCALE_AR = Locale.forLanguageTag("es-AR");
  private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern(
    "dd/MM/yyyy HH:mm"
  );

  private final ServicioRegistroGasto servicioRegistroGasto;
  private final ServicioCreditoTarea servicioCreditoTarea;
  private final ServicioLiquidacion servicioLiquidacion;
  private final ServicioHogar servicioHogar;
  private final HttpSession session;

  @Autowired
  public ServicioReporteImpl(
    ServicioRegistroGasto servicioRegistroGasto,
    ServicioCreditoTarea servicioCreditoTarea,
    ServicioLiquidacion servicioLiquidacion,
    ServicioHogar servicioHogar,
    HttpSession session
  ) {
    this.servicioRegistroGasto = servicioRegistroGasto;
    this.servicioCreditoTarea = servicioCreditoTarea;
    this.servicioLiquidacion = servicioLiquidacion;
    this.servicioHogar = servicioHogar;
    this.session = session;
  }

  private Long obtenerHogarIdActual() {
    Long hogarId = (Long) session.getAttribute("HOGAR_ID");
    if (hogarId == null) {
      Hogar hogarActivo = servicioHogar.obtenerHogar();
      if (hogarActivo != null) {
        hogarId = hogarActivo.getId();
      }
    }
    return hogarId;
  }

  @Override
  public ReporteMensual generarReporte(YearMonth mes) {
    Long hogarId = obtenerHogarIdActual();

    List<Usuario> integrantes = servicioRegistroGasto.obtenerIntegrantes(hogarId);
    List<Gasto> gastos = filtrarGastosDelMes(mes, hogarId);
    List<Tarea> tareas = filtrarTareasDelMes(mes, integrantes);

    Hogar hogar = armarHogarParaLiquidacion(hogarId, integrantes);

    return new ReporteMensual(
      mes,
      gastos,
      tareas,
      armarBalances(integrantes, gastos, tareas),
      servicioLiquidacion.obtenerLiquidacionDelMes(hogar, gastos),
      indexarNombres(integrantes)
    );
  }

  private Hogar armarHogarParaLiquidacion(Long hogarId, List<Usuario> integrantes) {
    Hogar hogarReal = buscarHogarReal(hogarId);

    Hogar copia;
    if (hogarReal != null) {
      copia = new Hogar(hogarReal.getNombreHogar());
      if (hogarReal.getId() != null) {
        copia.setId(hogarReal.getId());
      }
      copia.setPrecioHoraTarea(hogarReal.getPrecioHoraTarea());
      copia.setPisoMinimoHogar(hogarReal.getPisoMinimoHogar());
    } else {
      copia = new Hogar("Hogar Compartido");
    }
    copia.setIntegrantes(new ArrayList<>(integrantes));
    return copia;
  }

  private Hogar buscarHogarReal(Long hogarId) {
    if (hogarId == null) {
      return null;
    }
    try {
      return servicioHogar.seleccionarHogarPorId(hogarId);
    } catch (HogarNoEncontradoException e) {
      return null;
    }
  }

  private List<Gasto> filtrarGastosDelMes(YearMonth mes, Long hogarId) {
    List<Gasto> delMes = new ArrayList<>();
    for (Gasto gasto : servicioRegistroGasto.obtenerTodosLosGastos(hogarId)) {
      if (esDelMes(gasto.getFecha(), mes)) {
        delMes.add(gasto);
      }
    }
    return delMes;
  }

  private List<Tarea> filtrarTareasDelMes(YearMonth mes, List<Usuario> integrantes) {
    Set<Long> idsIntegrantes = new HashSet<>();
    for (Usuario integrante : integrantes) {
      if (integrante.getId() != null) {
        idsIntegrantes.add(integrante.getId());
      }
    }

    List<Tarea> delMes = new ArrayList<>();
    for (Tarea tarea : servicioCreditoTarea.obtenerTodasLasTareas()) {
      boolean esDelHogar = idsIntegrantes.contains(tarea.getUsuarioRealizadorId());
      if (esDelHogar && esDelMes(tarea.getFechaRegistro(), mes)) {
        delMes.add(tarea);
      }
    }
    return delMes;
  }

  private boolean esDelMes(LocalDateTime fecha, YearMonth mes) {
    YearMonth delElemento = fecha == null ? YearMonth.now() : YearMonth.from(fecha);
    return delElemento.equals(mes);
  }

  private List<FilaBalance> armarBalances(
    List<Usuario> integrantes,
    List<Gasto> gastos,
    List<Tarea> tareasDelMes
  ) {
    List<FilaBalance> filas = new ArrayList<>();
    if (integrantes.isEmpty()) {
      return filas;
    }
    double cuota = totalDe(gastos) / integrantes.size();
    for (Usuario integrante : integrantes) {
      filas.add(
        new FilaBalance(
          nombreDe(integrante),
          pagadoPor(integrante, gastos),
          creditoDelMes(integrante, tareasDelMes),
          cuota
        )
      );
    }
    return filas;
  }

  private double creditoDelMes(Usuario integrante, List<Tarea> tareasDelMes) {
    double credito = 0;
    for (Tarea tarea : tareasDelMes) {
      boolean esDelIntegrante =
        integrante.getId() != null && integrante.getId().equals(tarea.getUsuarioRealizadorId());
      boolean realizada =
        EstadoTarea.COMPLETADA.equals(tarea.getEstado()) ||
        EstadoTarea.LIQUIDADA.equals(tarea.getEstado());
      if (esDelIntegrante && realizada && tarea.getValorCredito() != null) {
        credito += tarea.getValorCredito();
      }
    }
    return credito;
  }

  private double totalDe(List<Gasto> gastos) {
    double total = 0;
    for (Gasto gasto : gastos) {
      total += gasto.getMonto();
    }
    return total;
  }

  private double pagadoPor(Usuario integrante, List<Gasto> gastos) {
    double pagado = 0;
    for (Gasto gasto : gastos) {
      if (
        integrante.getId() != null &&
        gasto.getUsuarioPagador() != null &&
        integrante.getId().equals(gasto.getUsuarioPagador().getId())
      ) {
        pagado += gasto.getMonto();
      }
    }
    return pagado;
  }

  private Map<Long, String> indexarNombres(List<Usuario> integrantes) {
    Map<Long, String> nombres = new HashMap<>();
    for (Usuario integrante : integrantes) {
      nombres.put(integrante.getId(), nombreDe(integrante));
    }
    return nombres;
  }

  private String nombreDe(Usuario usuario) {
    return usuario.getNombre() != null ? usuario.getNombre() : "Usuario " + usuario.getId();
  }

  @Override
  public String exportarCsv(ReporteMensual reporte) {
    StringBuilder csv = new StringBuilder();
    csv.append('﻿');
    fila(csv, "Reporte mensual", reporte.getMes().toString());
    agregarGastos(csv, reporte);
    agregarTareas(csv, reporte);
    agregarBalances(csv, reporte);
    agregarTransferencias(csv, reporte);
    return csv.toString();
  }

  private void agregarGastos(StringBuilder csv, ReporteMensual reporte) {
    fila(csv);
    fila(csv, "GASTOS");
    fila(csv, "Fecha", "Descripcion", "Categoria", "Tipo", "Pagador", "Monto");
    for (Gasto gasto : reporte.getGastos()) {
      Long pagadorId = (gasto.getUsuarioPagador() != null)
        ? gasto.getUsuarioPagador().getId()
        : null;

      fila(
        csv,
        formatearFecha(gasto.getFecha()),
        gasto.getDescripcion(),
        gasto.getCategoria(),
        gasto.getTipoGasto() != null ? gasto.getTipoGasto().getDescripcion() : "",
        reporte.nombreDe(pagadorId),
        numero(gasto.getMonto())
      );
    }
    fila(csv, "Total gastos", "", "", "", "", numero(reporte.getTotalGastos()));
  }

  private void agregarTareas(StringBuilder csv, ReporteMensual reporte) {
    fila(csv);
    fila(csv, "TAREAS");
    fila(csv, "Titulo", "Realizador", "Horas", "Credito", "Estado", "Fecha");
    for (Tarea tarea : reporte.getTareas()) {
      fila(
        csv,
        tarea.getTitulo(),
        reporte.nombreDe(tarea.getUsuarioRealizadorId()),
        numero(tarea.getHorasDedicadas()),
        numero(tarea.getValorCredito()),
        tarea.getEstado() != null ? tarea.getEstado().getDescripcion() : "",
        formatearFecha(tarea.getFechaRegistro())
      );
    }
  }

  private void agregarBalances(StringBuilder csv, ReporteMensual reporte) {
    fila(csv);
    fila(csv, "BALANCES POR INTEGRANTE");
    fila(csv, "Integrante", "Pagado", "Credito por tareas", "Cuota equitativa", "Balance neto");
    for (FilaBalance balance : reporte.getBalances()) {
      fila(
        csv,
        balance.getIntegrante(),
        numero(balance.getTotalPagado()),
        numero(balance.getCreditoTareas()),
        numero(balance.getCuotaEquitativa()),
        numero(balance.getBalanceNeto())
      );
    }
  }

  private void agregarTransferencias(StringBuilder csv, ReporteMensual reporte) {
    fila(csv);
    fila(csv, "LIQUIDACION SUGERIDA");
    fila(csv, "Quien paga", "Quien recibe", "Monto");
    for (TransferenciaSugerida transferencia : reporte.getTransferencias()) {
      fila(
        csv,
        transferencia.getEmisor(),
        transferencia.getReceptor(),
        numero(transferencia.getMonto())
      );
    }
  }

  private void fila(StringBuilder csv, String... celdas) {
    for (int i = 0; i < celdas.length; i++) {
      if (i > 0) {
        csv.append(SEPARADOR);
      }
      csv.append(escapar(celdas[i]));
    }
    csv.append(SALTO);
  }

  private String escapar(String valor) {
    if (valor == null) {
      return "";
    }
    boolean necesitaComillas =
      valor.contains(SEPARADOR) || valor.contains("\"") || valor.contains("\n");
    return necesitaComillas ? "\"" + valor.replace("\"", "\"\"") + "\"" : valor;
  }

  private String numero(Double valor) {
    return valor == null ? "" : String.format(LOCALE_AR, "%.2f", valor);
  }

  private String formatearFecha(LocalDateTime fecha) {
    return fecha == null ? "" : fecha.format(FORMATO_FECHA);
  }
}
