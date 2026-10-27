package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.MontoInvalidoPisoMinimoException;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service("servicioCalculosGasto")
@Transactional
public class ServicioCalculosGastoImpl implements ServicioCalculosGasto {

  private Double valorPozoTotalDeSumaCapacidadesDeCadaUsuario;

  private void validarPisoMinimo(Double valorPisoMinimoSeguro) {
    if (valorPisoMinimoSeguro == null || valorPisoMinimoSeguro <= 0) {
      throw new MontoInvalidoPisoMinimoException();
    }
  }

  private void limpiarAtributosCalculados(List<Usuario> usuarios) {
    if (usuarios != null) {
      for (Usuario usuario : usuarios) {
        usuario.setCapacidadRelativaPagoMensual(0.0);
        usuario.setPorcentajeSegunCapacidadDePagoMensual(0.0);
        usuario.setHorasTareaARealizar(0.0);
        usuario.setCuotaMensual(0.0);
      }
    }
  }

  @Override
  public Double calculoCapacidadRelativaDePago(Usuario usuario, Double valorPisoMinimoSeguro) {
    validarPisoMinimo(valorPisoMinimoSeguro);
    if (usuario == null) {
      return 0.0;
    }

    Double ingresoMensual = usuario.getIngresoMensual();
    double ingresoSeguro = (ingresoMensual != null) ? ingresoMensual : 0.0;

    double capacidadDelUsuario = ingresoSeguro - valorPisoMinimoSeguro;
    double capacidadFinal = Math.max(capacidadDelUsuario, 0.0);

    usuario.setCapacidadRelativaPagoMensual(capacidadFinal);
    return capacidadFinal;
  }

  @Override
  public void calcularTotalSumaDeCapacidadesDeCadaUsuario(
    List<Usuario> usuarios,
    Double valorPisoMinimoSeguro
  ) {
    validarPisoMinimo(valorPisoMinimoSeguro);
    if (usuarios == null || usuarios.isEmpty()) {
      return;
    }

    limpiarAtributosCalculados(usuarios);

    for (Usuario usuario : usuarios) {
      Double ingresoMensual = usuario.getIngresoMensual();
      double ingresoSeguro = (ingresoMensual != null) ? ingresoMensual : 0.0;

      double capacidadDelUsuario = ingresoSeguro - valorPisoMinimoSeguro;
      double capacidadFinal = Math.max(capacidadDelUsuario, 0.0);

      usuario.setCapacidadRelativaPagoMensual(capacidadFinal);
    }
  }

  @Override
  public void establecerMontoTotalDeLasSumasDeCapacidades(List<Usuario> usuarios) {
    double montoSubtotales = 0.0;
    if (usuarios != null) {
      for (Usuario usuario : usuarios) {
        Double capacidad = usuario.getCapacidadRelativaPagoMensual();
        if (capacidad != null) {
          montoSubtotales += capacidad;
        }
      }
    }
    valorPozoTotalDeSumaCapacidadesDeCadaUsuario = montoSubtotales;
  }

  @Override
  public void calcularPorcentajesDeCapacidadMensualDeUsuarios(List<Usuario> usuarios) {
    if (
      usuarios == null ||
      valorPozoTotalDeSumaCapacidadesDeCadaUsuario == null ||
      valorPozoTotalDeSumaCapacidadesDeCadaUsuario == 0.0
    ) {
      if (usuarios != null) {
        for (Usuario usuario : usuarios) {
          usuario.setPorcentajeSegunCapacidadDePagoMensual(0.0);
        }
      }
      return;
    }

    for (Usuario usuario : usuarios) {
      Double capacidad = usuario.getCapacidadRelativaPagoMensual();
      double capacidadSegura = (capacidad != null) ? capacidad : 0.0;

      double porcentajeParcial =
        (capacidadSegura / valorPozoTotalDeSumaCapacidadesDeCadaUsuario) * 100.0;

      // Redondeamos a 2 decimales para calzar exactamente con las expectativas del test
      double porcentajeRedondeado = BigDecimal
        .valueOf(porcentajeParcial)
        .setScale(2, RoundingMode.HALF_UP)
        .doubleValue();

      usuario.setPorcentajeSegunCapacidadDePagoMensual(porcentajeRedondeado);
    }
  }

  @Override
  public Double getMontoCapacidadTotalDeLosUsuarios() {
    return valorPozoTotalDeSumaCapacidadesDeCadaUsuario;
  }

  @Override
  public void calcularCuotasMensualesDeUsuarios(
    List<Usuario> usuariosConPorcentajes,
    Double montoGastosOcasionales
  ) {
    if (usuariosConPorcentajes == null) {
      return;
    }

    double baseAConvertir = (montoGastosOcasionales != null) ? montoGastosOcasionales : 0.0;

    for (Usuario usuario : usuariosConPorcentajes) {
      Double porcentajeObj = usuario.getPorcentajeSegunCapacidadDePagoMensual();
      double porcentaje = (porcentajeObj != null) ? porcentajeObj : 0.0;

      double calculoCuota = (baseAConvertir * porcentaje) / 100.0;

      usuario.setCuotaMensual(
        BigDecimal.valueOf(calculoCuota).setScale(2, RoundingMode.HALF_DOWN).doubleValue()
      );
    }
  }

  @Override
  public void calcularHorasDeTareasDeUsuarios(
    List<Usuario> usuariosConPorcentajes,
    Double valorHoraTarea
  ) {
    if (!esValidoParaCalcularHoras(usuariosConPorcentajes, valorHoraTarea)) {
      return;
    }

    double valorDeCuotaMaxima = obtenerCuotaMaxima(usuariosConPorcentajes);

    for (Usuario usuario : usuariosConPorcentajes) {
      calcularYAsignarHorasTarea(usuario, valorDeCuotaMaxima, valorHoraTarea);
    }
  }

  private boolean esValidoParaCalcularHoras(List<Usuario> usuarios, Double valorHora) {
    return usuarios != null && !usuarios.isEmpty() && valorHora != null && valorHora > 0;
  }

  private double obtenerCuotaMaxima(List<Usuario> usuarios) {
    double max = 0.0;
    for (Usuario usuario : usuarios) {
      Double cuota = usuario.getCuotaMensual();
      if (cuota != null && cuota > max) {
        max = cuota;
      }
    }
    return max;
  }

  private void calcularYAsignarHorasTarea(Usuario usuario, double cuotaMaxima, double valorHora) {
    Double cuotaObj = usuario.getCuotaMensual();
    double cuotaUsuario = (cuotaObj != null) ? cuotaObj : 0.0;

    if (Double.compare(cuotaMaxima, cuotaUsuario) != 0) {
      double brecha = cuotaMaxima - cuotaUsuario;
      double horaTareaCalculada = brecha / valorHora;
      double horaTareaRedondeada = Math.ceil(horaTareaCalculada * 2.0) / 2.0;
      usuario.setHorasTareaARealizar(horaTareaRedondeada);
    } else {
      usuario.setHorasTareaARealizar(0.0);
    }
  }
}
