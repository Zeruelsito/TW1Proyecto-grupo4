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
  private static Double valorPozoTotalDeSumaCapacidadesDeCadaUsuario;
  private static Double valorFijoIndispensableDeHogar;

  @Override
  public void establecerValorFijoIndispensableDeHogar(Double valorPisoMinimoSeguro){
    ServiceCalculosGastoImpl.valorFijoIndispensableDeHogar = valorPisoMinimoSeguro;
  }

  @Override
  public void establecerPisoMinimoSeguro(Double valorPisoMinimoSeguro) {
    ServiceCalculosGastoImpl.valorPisoMinimoSeguro = valorPisoMinimoSeguro;

  }

  @Override
  public void establecerValorHora(Double valorHoraConfigurado) {
    ServiceCalculosGastoImpl.valorHoraConfigurado = valorHoraConfigurado;
  }

  @Override
  public Double calculoCapacidadRelativaDePago(Usuario usuario) {
    if (valorPisoMinimoSeguro == null || valorPisoMinimoSeguro <= 0) {
      throw new MontoInvalidoPisoMinimoException();
    }
    Double capacidadDelUsuario = usuario.getIngresoMensual() - valorPisoMinimoSeguro;
    if (capacidadDelUsuario >= 0) {
      return capacidadDelUsuario;
    }
    return 0.0;
  }

  @Override
  public void calcularTotalSumaDeCapacidadesDeCadaUsuario(List<Usuario> usuarios) {
    for (Usuario usuario : usuarios) {
      double capacidadDePago = calculoCapacidadRelativaDePago(usuario);
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
  public Double getValorPisoMinimo() {
    return valorPisoMinimoSeguro;
  }

  @Override
  public Double getValorHora(){
    return valorHoraConfigurado;
  }

  @Override
  public Double getValorFijoIndispensableDeHogar(){
    return valorPisoMinimoSeguro;
  }

  @Override
  public void calcularCuotasMensualesDeUsuarios(List<Usuario> usuariosConPorcentajes) {
    for (Usuario usuario : usuariosConPorcentajes) {
      Double calculoCuota = (getMontoCapacidadTotalDeLosUsuarios()*usuario.getPorcentajeSegunCapacidadDePagoMensual())/100;
      Double pisoMinimo = this.getValorPisoMinimo();
      if (calculoCuota < pisoMinimo){
        usuario.setCuotaMensual(pisoMinimo);
      }else {
        usuario.setCuotaMensual(calculoCuota);
      }
    }
  }

}
