package com.tallerwebi.dominio;

import java.time.YearMonth;

public interface ServicioReporte {
  ReporteMensual generarReporte(YearMonth mes);
  String exportarCsv(ReporteMensual reporte);
}
