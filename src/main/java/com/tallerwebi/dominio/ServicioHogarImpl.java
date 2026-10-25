package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.HogarNoEncontradoException;
import com.tallerwebi.dominio.excepcion.ParametroInvalidoException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ServicioHogarImpl implements ServicioHogar {

  private final List<Hogar> hogares = new ArrayList<>();
  private Hogar hogar = new Hogar("Hogar 1"); //le saque el final

  @Autowired
  private RepositorioHogar repositorioHogar;

  @Override
  public void actualizarParametros(Double precioHoraTarea, Double pisoMinimoHogar) {
    if (
      precioHoraTarea == null ||
      pisoMinimoHogar == null ||
      precioHoraTarea < 0 ||
      pisoMinimoHogar < 0
    ) {
      throw new ParametroInvalidoException();
    }

    if (!precioHoraTarea.equals(hogar.getPrecioHoraTarea())) { //si ve q el valor q le pasaron es diferente al de ahora, guarda el valor viejo y setea el nuevo, en teoria deberian poder repetirse operaciones pero me percate tarde de este error.
      hogar
        .getHistorial()
        .add(new CambioParametro("phoraTarea", hogar.getPrecioHoraTarea(), precioHoraTarea));
      hogar.setPrecioHoraTarea(precioHoraTarea);
    }

    if (!pisoMinimoHogar.equals(hogar.getPisoMinimoHogar())) {
      hogar
        .getHistorial()
        .add(
          new CambioParametro("Piso mínimo del hogar", hogar.getPisoMinimoHogar(), pisoMinimoHogar)
        );
      hogar.setPisoMinimoHogar(pisoMinimoHogar);
    }
  }

  @Override
  public Hogar obtenerHogar() {
    return hogar;
  }

  @Override
  public void registrarHogar(Hogar hogar) {
    hogares.add(hogar);
  }

  @Override
  public void seleccionarHogar(Long hogarId) {
    for (Hogar hogarSeleccionado : hogares) {
      if (hogarSeleccionado.getId().equals(hogarId)) {
        hogar = hogarSeleccionado;
        return;
      }
    }
    throw new HogarNoEncontradoException();
  }

  @Override
  public List<CambioParametro> obtenerHistorial() {
    List<CambioParametro> historial = new ArrayList<>(hogar.getHistorial());
    Collections.reverse(historial);
    return historial;
  }

  @Override
  public List<Hogar> obtenerHogares(Long usuarioId) {
    List<Hogar> hogares = repositorioHogar.obtenerHogaresDeUsuario(usuarioId);
    for (Hogar hogar : hogares) {
      org.hibernate.Hibernate.initialize(hogar.getIntegrantes());
    }
    return hogares;
  }

  @Override
  public void crearHogar(Hogar hogar) {
    repositorioHogar.guardar(hogar);
  }

  @Override
  public boolean unirseAHogar(String nombreHogar, String pin, Usuario usuario) {
    List<Hogar> todosLosHogares = repositorioHogar.obtenerTodos();
    for (Hogar h : todosLosHogares) {
      if (
        h.getNombreHogar() != null &&
        h.getNombreHogar().equalsIgnoreCase(nombreHogar) &&
        pin.equals(h.getPin())
      ) {
        boolean yaEsIntegrante = h
          .getIntegrantes()
          .stream()
          .anyMatch(u -> u.getId().equals(usuario.getId()));
        if (!yaEsIntegrante) {
          h.getIntegrantes().add(usuario);
          repositorioHogar.actualizar(h);
        }
        return true;
      }
    }
    return false;
  }
}
