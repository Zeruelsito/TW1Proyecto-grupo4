package com.tallerwebi.dominio;

//import com.hogar.gastos.dto.GastoDTO;
//import com.hogar.gastos.dto.GastoForm;
//import com.hogar.gastos.dto.HorasForm;
//import com.hogar.gastos.dto.IntegranteDTO;

import java.util.List;

public interface ServiceCalculosGasto {

  void establecerValorFijoIndispensableDeHogar(Double valorPisoMinimoSeguro);
  void establecerPisoMinimoSeguro(Double valorPisoMinimoSeguro);
  void establecerValorHora(Double valorHoraConfigurado);
  Double calculoCapacidadRelativaDePago(Usuario usuario);
  void calcularTotalSumaDeCapacidadesDeCadaUsuario(List<Usuario> usuarios);
  void calcularPorcentajesDeCapacidadMensualDeUsuarios(List<Usuario> usuarios);
  Double getMontoCapacidadTotalDeLosUsuarios();
  void establecerMontoTotalDeLasSumasDeCapacidades(List<Usuario> usuarios);
  Double getValorPisoMinimo();
  Double getValorHora();
  Double getValorFijoIndispensableDeHogar();

  void calcularCuotasMensualesDeUsuarios(List<Usuario> usuariosConPorcentajes);
}
