package com.tallerwebi.dominio;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

  @Autowired
  public ServicioReporteImpl(
    ServicioRegistroGasto servicioRegistroGasto,
    ServicioCreditoTarea servicioCreditoTarea,
    ServicioLiquidacion servicioLiquidacion
  ) {
    this.servicioRegistroGasto = servicioRegistroGasto;
    this.servicioCreditoTarea = servicioCreditoTarea;
    this.servicioLiquidacion = servicioLiquidacion;
  }

  @Override
  public ReporteMensual generarReporte(YearMonth mes) {
    List<Usuario> integrantes = servicioRegistroGasto.obtenerIntegrantes();
    List<Gasto> gastos = filtrarGastosDelMes(mes);
    List<Tarea> tareas = filtrarTareasDelMes(mes);

    Hogar hogar = new Hogar("Hogar Compartido");
    hogar.setIntegrantes(integrantes);

    return new ReporteMensual(
      mes,
      gastos,
      tareas,
      armarBalances(integrantes, gastos),
      servicioLiquidacion.obtenerLiquidacionDelMes(hogar, gastos),
      indexarNombres(integrantes)
    );
  }

  private List<Gasto> filtrarGastosDelMes(YearMonth mes) {
    List<Gasto> delMes = new ArrayList<>();
    for (Gasto gasto : servicioRegistroGasto.obtenerTodosLosGastos()) {
      if (esDelMes(gasto.getFecha(), mes)) {
        delMes.add(gasto);
      }
    }
    return delMes;
  }

  private List<Tarea> filtrarTareasDelMes(YearMonth mes) {
    List<Tarea> delMes = new ArrayList<>();
    for (Tarea tarea : servicioCreditoTarea.obtenerTodasLasTareas()) {
      if (esDelMes(tarea.getFechaRegistro(), mes)) {
        delMes.add(tarea);
      }
    }
    return delMes;
  }

  private boolean esDelMes(LocalDateTime fecha, YearMonth mes) {
    YearMonth delElemento = fecha == null ? YearMonth.now() : YearMonth.from(fecha);
    return delElemento.equals(mes);
  }

  private List<FilaBalance> armarBalances(List<Usuario> integrantes, List<Gasto> gastos) {
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
          servicioCreditoTarea.obtenerCreditoAcumuladoPorUsuario(integrante.getId()),
          cuota
        )
      );
    }
    return filas;
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
      if (integrante.getId() != null && integrante.getId().equals(gasto.getPagadorId())) {
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
      fila(
        csv,
        formatearFecha(gasto.getFecha()),
        gasto.getDescripcion(),
        gasto.getCategoria(),
        gasto.getTipoGasto() != null ? gasto.getTipoGasto().getDescripcion() : "",
        reporte.nombreDe(gasto.getPagadorId()),
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
