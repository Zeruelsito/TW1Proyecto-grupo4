package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.AlcanceTarea;
//import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.RepositorioTareaRepetitiva;
import com.tallerwebi.dominio.TareaRepetitiva;
import com.tallerwebi.dominio.Usuario;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioTareaRepetitiva")
public class RepositorioTareaRepetitivaImpl implements RepositorioTareaRepetitiva {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioTareaRepetitivaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(TareaRepetitiva tareaRepetitiva) {
    sessionFactory.getCurrentSession().persist(tareaRepetitiva);
  }

  @Override
  public List<TareaRepetitiva> obtenerTodos() {
    String query = "select t from TareaRepetitiva t order by t.id";
    return sessionFactory
      .getCurrentSession()
      .createQuery(query, TareaRepetitiva.class)
      .getResultList();
  }

  @Override
  public TareaRepetitiva obtenerPorId(Long id) {
    return sessionFactory.getCurrentSession().get(TareaRepetitiva.class, id);
  }

  @Override
  public int contarAsignadasAUsuario(Usuario usuario) {
    String query =
      "select cast(count(t) as integer) from TareaRepetitiva t where t.usuarioAsignado = :usuario";
    return sessionFactory
      .getCurrentSession()
      .createQuery(query, Integer.class)
      .setParameter("usuario", usuario)
      .getSingleResult();
  }

  @Override
  public List<TareaRepetitiva> obtenerPorHogar(Long hogarId) {
    String query = "select t from TareaRepetitiva t where t.hogar.id = :hogarId order by t.id";
    return sessionFactory
      .getCurrentSession()
      .createQuery(query, TareaRepetitiva.class)
      .setParameter("hogarId", hogarId)
      .getResultList();
  }

  @Override
  public List<TareaRepetitiva> obtenerPorHogarYUsuario(Long hogarId, Long usuarioId) {
    String query =
      "from TareaRepetitiva t where t.hogar.id = :hogarId " +
      "and t.activa = true " +
      "and (t.alcance = :hogar or t.propietario.id = :usuarioId)";
    return sessionFactory
      .getCurrentSession()
      .createQuery(query, TareaRepetitiva.class)
      .setParameter("hogarId", hogarId)
      .setParameter("hogar", AlcanceTarea.HOGAR)
      .setParameter("usuarioId", usuarioId)
      .getResultList();
  }

  @Override
  public TareaRepetitiva obtenerPorHogarUsuarioYTitulo(
    Long hogarId,
    Long usuarioId,
    String titulo
  ) {
    String query =
      "from TareaRepetitiva t where t.hogar.id = :hogarId and t.titulo = :titulo " +
      "and t.activa = true " +
      "and (t.alcance = :hogar or t.propietario.id = :usuarioId)";
    return sessionFactory
      .getCurrentSession()
      .createQuery(query, TareaRepetitiva.class)
      .setParameter("hogarId", hogarId)
      .setParameter("titulo", titulo)
      .setParameter("hogar", AlcanceTarea.HOGAR)
      .setParameter("usuarioId", usuarioId)
      .setMaxResults(1)
      .uniqueResult();
  }
}
