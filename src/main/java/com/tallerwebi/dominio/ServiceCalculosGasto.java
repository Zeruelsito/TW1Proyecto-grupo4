package com.tallerwebi.dominio;

//import com.hogar.gastos.dto.GastoDTO;
//import com.hogar.gastos.dto.GastoForm;
//import com.hogar.gastos.dto.HorasForm;
//import com.hogar.gastos.dto.IntegranteDTO;

import java.util.List;

public interface ServiceCalculosGasto {
  Double establecerPisoMinimoSeguro(Double valorPisoMinimoSeguro);
  Double establecerValorHora(Double valorHoraConfigurado);
  Double calculoCapacidadRelativaDePago(Usuario usuario);
  void calcularTotalSumaDeCapacidadesDeCadaUsuario(List<Usuario> usuarios);
  List<Usuario> calcularPorcentajesDeCapacidadMensualDeUsuarios(List<Usuario> usuarios);

  Double getMontoCapacidadTotalDeLosUsuarios();

  void establecerMontoTotalDeLasSumasDeCapacidades(List<Usuario> usuarios);
}
