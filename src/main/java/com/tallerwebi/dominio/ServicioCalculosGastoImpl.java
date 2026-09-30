package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.MontoInvalidoPisoMinimoException;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service("serviceGasto")
@Transactional
public class ServicioCalculosGastoImpl implements ServicioCalculosGasto {

  private Double valorPozoTotalDeSumaCapacidadesDeCadaUsuario;

  @Override
  public Double calculoCapacidadRelativaDePago(Usuario usuario, Double valorPisoMinimoSeguro) {
    verificaPisoMInimo(valorPisoMinimoSeguro);
    Double capacidadDelUsuario = usuario.getIngresoMensual() - valorPisoMinimoSeguro;
    if (capacidadDelUsuario >= 0) {
      return capacidadDelUsuario;
    }
    return 0.0;
  }

  private static void verificaPisoMInimo(Double valorPisoMinimoSeguro) {
    if (valorPisoMinimoSeguro <= 0) {
      throw new MontoInvalidoPisoMinimoException();
    }
  }

  @Override
  public void calcularTotalSumaDeCapacidadesDeCadaUsuario(
    List<Usuario> usuarios,
    Double valorPisoMinimoSeguro
  ) {
    verificaPisoMInimo(valorPisoMinimoSeguro);
    for (Usuario usuario : usuarios) {
      double capacidadDePago = calculoCapacidadRelativaDePago(usuario, valorPisoMinimoSeguro);
      usuario.setCapacidadRelativaPagoMensual(capacidadDePago);
    }
  }

  @Override
  public void establecerMontoTotalDeLasSumasDeCapacidades(List<Usuario> usuarios) {
    Double montoSubtotales = 0.0;
    for (Usuario usuario : usuarios) {
      montoSubtotales += usuario.getCapacidadRelativaPagoMensual();
    }
    valorPozoTotalDeSumaCapacidadesDeCadaUsuario = montoSubtotales;
  }

  @Override
  public void calcularPorcentajesDeCapacidadMensualDeUsuarios(List<Usuario> usuarios) {
    for (Usuario usuario : usuarios) {
      if (
        valorPozoTotalDeSumaCapacidadesDeCadaUsuario == null ||
        valorPozoTotalDeSumaCapacidadesDeCadaUsuario == 0.0
      ) {
        usuario.setPorcentajeSegunCapacidadDePagoMensual(0.0);
      }

      double porcentajeParcial =
        usuario.getCapacidadRelativaPagoMensual() / valorPozoTotalDeSumaCapacidadesDeCadaUsuario;
      usuario.setPorcentajeSegunCapacidadDePagoMensual(porcentajeParcial * 100d);
    }
  }

  @Override
  public Double getMontoCapacidadTotalDeLosUsuarios() {
    return valorPozoTotalDeSumaCapacidadesDeCadaUsuario;
  }

  @Override
  public void calcularCuotasMensualesDeUsuarios(
    List<Usuario> usuariosConPorcentajes,
    Double valorPisoMinimoSeguro
  ) {
    verificaPisoMInimo(valorPisoMinimoSeguro);
    for (Usuario usuario : usuariosConPorcentajes) {
      double calculoCuota =
        (getMontoCapacidadTotalDeLosUsuarios() *
          usuario.getPorcentajeSegunCapacidadDePagoMensual()) /
        100;

      if (calculoCuota <= valorPisoMinimoSeguro) {
        usuario.setCuotaMensual(valorPisoMinimoSeguro);
      } else {
        usuario.setCuotaMensual(
          BigDecimal.valueOf(calculoCuota).setScale(2, RoundingMode.HALF_DOWN).doubleValue()
        );
      }
    }
  }

  @Override
  public void calcularHorasDeTareasDeUsuarios(
    List<Usuario> usuariosConPorcentajes,
    Double valorHoraTarea
  ) {
    Double valorDeCuotaMaxima = 0d;
    for (Usuario usuario : usuariosConPorcentajes) {
      if (valorDeCuotaMaxima < usuario.getCuotaMensual()) {
        valorDeCuotaMaxima = usuario.getCuotaMensual();
      }
    }
    for (Usuario usuario : usuariosConPorcentajes) {
      if (!(valorDeCuotaMaxima.equals(usuario.getCuotaMensual()))) {
        Double brechaEntreUsuarioYCuotaMaxima = valorDeCuotaMaxima - usuario.getCuotaMensual();
        Double horaTareaCalculada = brechaEntreUsuarioYCuotaMaxima / valorHoraTarea;
        // Redondea hacia arriba al 0.5 más cercano (ej: 0.1 -> 0.5, 0.6 -> 1.0)
        Double horaTareaRedondeada = Math.ceil(horaTareaCalculada * 2.0) / 2.0;
        usuario.setHorasTareaARealizar(horaTareaRedondeada);
      } else {
        usuario.setHorasTareaARealizar(0d);
      }
    }
  }
}
