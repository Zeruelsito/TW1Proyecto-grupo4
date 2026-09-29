package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.MontoInvalidoPisoMinimoException;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service("serviceGasto")
@Transactional
public class ServiceCalculosGastoImpl implements ServiceCalculosGasto {

  private static Double valorHoraConfigurado;
  private static Double valorPisoMinimoSeguro;
  private static Double valorMontoTotalSumaDeCapacidadesDeCadaUsuario;

  @Override
  public Double establecerPisoMinimoSeguro(Double valorPisoMinimoSeguro) {
    ServiceCalculosGastoImpl.valorPisoMinimoSeguro = valorPisoMinimoSeguro;
    return valorPisoMinimoSeguro;
  }

  @Override
  public Double establecerValorHora(Double valorHoraConfigurado) {
    ServiceCalculosGastoImpl.valorHoraConfigurado = valorHoraConfigurado;
    return valorHoraConfigurado;
  }

  @Override
  public Double calculoCapacidadRelativaDePago(Usuario usuario) {
    if (valorPisoMinimoSeguro == null || valorPisoMinimoSeguro <= 0) {
      throw new MontoInvalidoPisoMinimoException();
    }
    return usuario.getIngresoMensual() - valorPisoMinimoSeguro;
  }

  @Override
  public void calcularTotalSumaDeCapacidadesDeCadaUsuario(List<Usuario> usuarios) {
    for (Usuario usuario : usuarios) {
      usuario.setCapacidadRelativaPagoMensual(calculoCapacidadRelativaDePago(usuario));
    }
  }

  @Override
  public void establecerMontoTotalDeLasSumasDeCapacidades(List<Usuario> usuarios) {
    Double montoSubtotales = 0.0;
    for (Usuario usuario : usuarios) {
      montoSubtotales += usuario.getCapacidadRelativaPagoMensual();
    }
    valorMontoTotalSumaDeCapacidadesDeCadaUsuario = montoSubtotales;
  }

  @Override
  public List<Usuario> calcularPorcentajesDeCapacidadMensualDeUsuarios(List<Usuario> usuarios) {
    for (Usuario usuario : usuarios) {
      Double porcentajeParcial =
        usuario.getCapacidadRelativaPagoMensual() / valorMontoTotalSumaDeCapacidadesDeCadaUsuario;
      usuario.setPorcentajeSegunCapacidadDePagoMensual(porcentajeParcial * 100d);
    }
    return usuarios;
  }

  @Override
  public Double getMontoCapacidadTotalDeLosUsuarios() {
    return valorMontoTotalSumaDeCapacidadesDeCadaUsuario;
  }
}
