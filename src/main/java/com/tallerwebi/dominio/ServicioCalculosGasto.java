package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioCalculosGasto {
  Double calculoCapacidadRelativaDePago(Usuario usuario, Double valorPisoMinimoSeguro);

  void calcularTotalSumaDeCapacidadesDeCadaUsuario(
    List<Usuario> usuarios,
    Double valorPisoMinimoSeguro
  );

  void establecerMontoTotalDeLasSumasDeCapacidades(List<Usuario> usuarios);

  void calcularPorcentajesDeCapacidadMensualDeUsuarios(List<Usuario> usuarios);

  Double getMontoCapacidadTotalDeLosUsuarios();

  void calcularCuotasMensualesDeUsuarios(
    List<Usuario> usuariosConPorcentajes,
    Double montoGastosOcasionales
  );

  void calcularHorasDeTareasDeUsuarios(List<Usuario> usuariosConPorcentajes, Double valorHoraTarea);
}
