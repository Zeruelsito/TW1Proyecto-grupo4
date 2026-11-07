package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Propuesta;
import com.tallerwebi.dominio.RepositorioPropuesta;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioPropuesta")
public class RepositorioPropuestaImpl implements RepositorioPropuesta {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioPropuestaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Propuesta guardar(Propuesta propuesta) {
    sessionFactory.getCurrentSession().persist(propuesta);
    return propuesta;
  }

  @Override
  public Propuesta obtenerPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Propuesta.class, id);
  }

  @Override
  public List<Propuesta> obtenerTodasLasPropuestas() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Propuesta", Propuesta.class)
      .getResultList();
  }

  @Override
  public List<Propuesta> obtenerPropuestasPorHogar(Long hogarId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Propuesta where hogarId = :hogarId order by id desc", Propuesta.class)
      .setParameter("hogarId", hogarId)
      .getResultList();
  }

  @Override
  public List<Propuesta> obtenerPropuestasPorUsuario(Long idUsuario) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Propuesta where idUsuarioCreador = :idUsuario", Propuesta.class)
      .setParameter("idUsuario", idUsuario)
      .getResultList();
  }

  @Override
  public long obtenerTotalIntegrantes(Long hogarId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(u) from Hogar h join h.integrantes u " +
        "where h.id = :hogarId and u.fechaBaja is null",
        Long.class
      )
      .setParameter("hogarId", hogarId)
      .getSingleResult();
  }
}
