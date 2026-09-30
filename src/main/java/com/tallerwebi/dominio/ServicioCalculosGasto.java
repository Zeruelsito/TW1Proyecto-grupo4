package com.tallerwebi.dominio;

//import com.hogar.gastos.dto.GastoDTO;
//import com.hogar.gastos.dto.GastoForm;
//import com.hogar.gastos.dto.HorasForm;
//import com.hogar.gastos.dto.IntegranteDTO;

import java.util.List;

public interface ServicioCalculosGasto {
  Double calculoCapacidadRelativaDePago(Usuario usuario, Double valorPisoMinimoSeguro);
  void calcularPorcentajesDeCapacidadMensualDeUsuarios(List<Usuario> usuarios);
  Double getMontoCapacidadTotalDeLosUsuarios();
  void calcularTotalSumaDeCapacidadesDeCadaUsuario(
    List<Usuario> usuarios,
    Double valorPisoMinimoSeguro
  );
  void establecerMontoTotalDeLasSumasDeCapacidades(List<Usuario> usuarios);
  void calcularCuotasMensualesDeUsuarios(
    List<Usuario> usuariosConPorcentajes,
    Double valorPisoMinimoSeguro
  );
  void calcularHorasDeTareasDeUsuarios(List<Usuario> usuariosConPorcentajes, Double valorHoraTarea);
}
