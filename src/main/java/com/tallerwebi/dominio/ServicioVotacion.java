package com.tallerwebi.dominio;

import com.tallerwebi.dominio.EstadoPropuesta;
import com.tallerwebi.dominio.Propuesta;
import com.tallerwebi.dominio.RepositorioPropuesta;
import com.tallerwebi.dominio.RepositorioVoto;
import com.tallerwebi.dominio.Voto;
import com.tallerwebi.dominio.excepcion.PropuestaNoExiste;
import com.tallerwebi.dominio.excepcion.UsuarioYaVoto;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service("servicioVotacion")
@Transactional
public class ServicioVotacion {

  private static final double PORCENTAJE_MINIMO = 1.0;
  private static final double PORCENTAJE_MAXIMO = 100.0;
  private static final long VOTOS_MINIMOS = 1;

  private final RepositorioVoto repositorioVoto;
  private final RepositorioPropuesta repositorioPropuesta;

  public ServicioVotacion(
    RepositorioVoto repositorioVoto,
    RepositorioPropuesta repositorioPropuesta
  ) {
    this.repositorioVoto = repositorioVoto;
    this.repositorioPropuesta = repositorioPropuesta;
  }

  public List<Propuesta> obtenerTodasLasPropuestas() {
    List<Propuesta> propuestas = repositorioPropuesta.obtenerTodasLasPropuestas();
    long totalIntegrantes = repositorioPropuesta.obtenerTotalIntegrantes();
    for (Propuesta p : propuestas) {
      actualizarEstado(p, totalIntegrantes);
    }
    return propuestas;
  }

  public void guardarPropuesta(Propuesta propuesta) {
    validarPropuesta(propuesta);
    propuesta.setEstado(EstadoPropuesta.PENDIENTE);
    repositorioPropuesta.guardar(propuesta);
  }

  public void votar(Long idUsuario, Long idPropuesta, boolean esAfirmativo, String comentario) {
    Propuesta propuesta = repositorioPropuesta.obtenerPorId(idPropuesta);
    if (propuesta == null) {
      throw new PropuestaNoExiste("La propuesta no existe");
    }
    if (!propuesta.estaPendiente() || propuesta.estaVencida(LocalDate.now())) {
      throw new PropuestaNoExiste("La propuesta no está pendiente de votación");
    }
    if (repositorioVoto.existeVoto(idUsuario, idPropuesta)) {
      throw new UsuarioYaVoto("El usuario ya ha votado en esta propuesta");
    }
    repositorioVoto.guardar(new Voto(idUsuario, idPropuesta, esAfirmativo, comentario));
    actualizarEstado(propuesta, repositorioPropuesta.obtenerTotalIntegrantes());
  }

  private void actualizarEstado(Propuesta propuesta, long totalIntegrantes) {
    if (propuesta.getEstado() != EstadoPropuesta.PENDIENTE) {
      return;
    }
    long aFavor = repositorioVoto.obtenerVotosAfirmativosPorPropuesta(propuesta.getId());
    long enContra = repositorioVoto.obtenerVotosNegativosPorPropuesta(propuesta.getId());
    long requeridos = (long) Math.ceil(
      (totalIntegrantes * propuesta.getPorcentajeQuorum()) / 100.0
    );

    if (aFavor >= requeridos) {
      propuesta.setEstado(EstadoPropuesta.APROBADA);
    } else if (enContra > totalIntegrantes - requeridos) {
      propuesta.setEstado(EstadoPropuesta.RECHAZADA);
    } else if (propuesta.estaVencida(LocalDate.now())) {
      propuesta.setEstado(EstadoPropuesta.CANCELADA);
    }
  }

  private void validarPropuesta(Propuesta propuesta) {
    validarTexto(propuesta.getTitulo(), "titulo");
    validarTexto(propuesta.getDescripcion(), "descripcion");
    validarQuorum(propuesta.getPorcentajeQuorum());
    validarFechaLimite(propuesta);
  }

  private void validarTexto(String texto, String campo) {
    if (texto == null || texto.trim().isEmpty()) {
      throw new PropuestaNoExiste("El campo " + campo + " no puede estar vacío");
    }
  }

  private void validarQuorum(double porcentajeQuorum) {
    if (porcentajeQuorum < PORCENTAJE_MINIMO || porcentajeQuorum > PORCENTAJE_MAXIMO) {
      throw new PropuestaNoExiste(
        "El porcentaje de quorum debe estar entre " +
        PORCENTAJE_MINIMO +
        "% y " +
        PORCENTAJE_MAXIMO +
        "%"
      );
    }
  }

  private void validarFechaLimite(Propuesta propuesta) {
    if (
      propuesta.getFechaLimite() == null ||
      propuesta.getFechaLimite().isBefore(LocalDateTime.now().toLocalDate())
    ) {
      throw new PropuestaNoExiste("La fecha límite debe ser una fecha futura");
    }
  }
}
