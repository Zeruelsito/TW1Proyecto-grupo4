package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.PropuestaNoExiste;
import com.tallerwebi.dominio.excepcion.SinPermisoParaCancelarException;
import com.tallerwebi.dominio.excepcion.UsuarioYaVoto;
import com.tallerwebi.dominio.excepcion.VotacionCerradaException;
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

  private final RepositorioVoto repositorioVoto;
  private final RepositorioPropuesta repositorioPropuesta;
  private final RepositorioUsuario repositorioUsuario;

  public ServicioVotacion(
    RepositorioVoto repositorioVoto,
    RepositorioPropuesta repositorioPropuesta,
    RepositorioUsuario repositorioUsuario
  ) {
    this.repositorioVoto = repositorioVoto;
    this.repositorioPropuesta = repositorioPropuesta;
    this.repositorioUsuario = repositorioUsuario;
  }

  public List<Propuesta> obtenerPropuestasDelHogar(Long hogarId) {
    List<Propuesta> propuestas = repositorioPropuesta.obtenerPropuestasPorHogar(hogarId);
    long totalIntegrantes = repositorioPropuesta.obtenerTotalIntegrantes(hogarId);
    for (Propuesta p : propuestas) {
      actualizarEstado(p, totalIntegrantes);
      cargarDetalleDeVotos(p, totalIntegrantes);
    }
    return propuestas;
  }

  public void guardarPropuesta(Propuesta propuesta) {
    validarPropuesta(propuesta);
    propuesta.setEstado(EstadoPropuesta.PENDIENTE);
    repositorioPropuesta.guardar(propuesta);
  }

  public void votar(
    Long idUsuario,
    Long hogarId,
    Long idPropuesta,
    boolean esAfirmativo,
    String comentario
  ) {
    Propuesta propuesta = repositorioPropuesta.obtenerPorId(idPropuesta);
    if (propuesta == null || !propuesta.perteneceAlHogar(hogarId)) {
      throw new PropuestaNoExiste("La propuesta no existe");
    }
    if (!propuesta.estaPendiente() || propuesta.estaVencida(LocalDate.now())) {
      throw new VotacionCerradaException("La votación de esta propuesta ya está cerrada");
    }
    if (repositorioVoto.existeVoto(idUsuario, idPropuesta)) {
      throw new UsuarioYaVoto("El usuario ya ha votado en esta propuesta");
    }
    repositorioVoto.guardar(new Voto(idUsuario, idPropuesta, esAfirmativo, comentario));
    actualizarEstado(propuesta, repositorioPropuesta.obtenerTotalIntegrantes(hogarId));
  }

  public void cancelarPropuesta(Long idUsuario, Long hogarId, Long idPropuesta, boolean esAdmin) {
    Propuesta propuesta = repositorioPropuesta.obtenerPorId(idPropuesta);
    if (propuesta == null || !propuesta.perteneceAlHogar(hogarId)) {
      throw new PropuestaNoExiste("La propuesta no existe");
    }
    if (!esAdmin && !propuesta.fueCreadaPor(idUsuario)) {
      throw new SinPermisoParaCancelarException(
        "Solo quien creó la propuesta o un administrador puede cancelarla"
      );
    }
    if (!propuesta.estaPendiente()) {
      throw new VotacionCerradaException("Solo se pueden cancelar propuestas pendientes");
    }
    propuesta.setEstado(EstadoPropuesta.CANCELADA);
  }

  long calcularVotosRequeridos(long totalIntegrantes, double porcentajeQuorum) {
    long requeridos = (long) Math.ceil((totalIntegrantes * porcentajeQuorum) / 100.0);
    return Math.max(1, requeridos);
  }

  private void actualizarEstado(Propuesta propuesta, long totalIntegrantes) {
    if (propuesta.getEstado() != EstadoPropuesta.PENDIENTE || totalIntegrantes <= 0) {
      return;
    }
    long aFavor = repositorioVoto.obtenerVotosAfirmativosPorPropuesta(propuesta.getId());
    long enContra = repositorioVoto.obtenerVotosNegativosPorPropuesta(propuesta.getId());
    long requeridos = calcularVotosRequeridos(totalIntegrantes, propuesta.getPorcentajeQuorum());
    long sinVotar = totalIntegrantes - aFavor - enContra;

    if (aFavor >= requeridos) {
      propuesta.setEstado(EstadoPropuesta.APROBADA);
    } else if (aFavor + sinVotar < requeridos) {
      propuesta.setEstado(EstadoPropuesta.RECHAZADA);
    } else if (propuesta.estaVencida(LocalDate.now())) {
      propuesta.setEstado(EstadoPropuesta.CANCELADA);
    }
  }

  private void validarPropuesta(Propuesta propuesta) {
    if (propuesta.getHogarId() == null) {
      throw new PropuestaNoExiste("La propuesta debe pertenecer a un hogar");
    }
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

  private void cargarDetalleDeVotos(Propuesta propuesta, long totalIntegrantes) {
    List<Voto> votos = repositorioVoto.obtenerVotosPorPropuesta(propuesta.getId());
    List<DetalleVoto> detalles = new ArrayList<>();
    for (Voto voto : votos) {
      Usuario usuario = repositorioUsuario.buscarPorId(voto.getIdUsuario());
      String nombre = usuario != null ? usuario.getNombre() : "Integrante";
      detalles.add(new DetalleVoto(nombre, voto.isEsAfirmativo(), voto.getComentario()));
    }
    propuesta.setVotos(detalles);
    propuesta.setFaltanVotar(Math.max(0, totalIntegrantes - votos.size()));
  }
}
