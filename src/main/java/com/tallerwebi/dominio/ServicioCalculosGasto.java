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

  //maca
  Double calcularCuotaMensualPorUsuario(
    Usuario usuario,
    Double montoGastosOcasionales,
    Double valorPisoMinimoSeguro,
    List<Usuario> integrantes
  );

  Double calcularPorcentajeDeAporteDeGastosPorUsuario(
    Usuario usuario,
    List<Usuario> integrantes,
    Double valorPisoMinimoSeguro
  );

  Double calcularValorDeHorasDeTareaPorUsuario(
    Usuario usuario,
    Double valorHoraTarea,
    Double montoGastosOcasionales,
    Double valorPisoMinimoSeguro,
    List<Usuario> integrantes
  );
}
