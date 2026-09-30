package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ReporteMensual;
import com.tallerwebi.dominio.ServicioReporte;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorReporte {

    private static final String ROL_ADMIN = "ADMIN";

    private final ServicioReporte servicioReporte;

    @Autowired
    public ControladorReporte(ServicioReporte servicioReporte) {
        this.servicioReporte = servicioReporte;
    }

    @GetMapping("/reporte")
    public ModelAndView verReporte(@RequestParam(value = "mes", required = false) String mes,HttpServletRequest request) {
        String redireccion = redireccionSiNoEsAdmin(request);
        if (redireccion != null) {
            return new ModelAndView(redireccion);
        }
        YearMonth periodo = interpretarMes(mes);
        ModelAndView mav = new ModelAndView("reporte");
        mav.addObject("reporte", servicioReporte.generarReporte(periodo));
        mav.addObject("mes", periodo.toString());
        return mav;
    }

    @GetMapping("/reporte/exportar")
    public ResponseEntity<byte[]> exportarCsv(@RequestParam(value = "mes", required = false) String mes, HttpServletRequest request) {
        String redireccion = redireccionSiNoEsAdmin(request);
        if (redireccion != null) {
            return ResponseEntity
                .status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, request.getContextPath() + "/login")
                .build();
        }
        YearMonth periodo = interpretarMes(mes);
        ReporteMensual reporte = servicioReporte.generarReporte(periodo);
        byte[] contenido = servicioReporte.exportarCsv(reporte).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"reporte-" + periodo + ".csv\""
            )
            .body(contenido);
    }

    private String redireccionSiNoEsAdmin(HttpServletRequest request) {
        Object rol = request.getSession().getAttribute("ROL");
        if (rol == null) {
            return "redirect:/login";
        }
        return ROL_ADMIN.equals(rol) ? null : "redirect:/home";
    }

    private YearMonth interpretarMes(String mes) {
        if (mes == null || mes.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(mes);
        } catch (DateTimeParseException e) {
            return YearMonth.now();
        }
    }
}
