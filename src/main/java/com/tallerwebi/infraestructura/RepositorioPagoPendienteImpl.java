package com.tallerwebi.infraestructura; // O el paquete que uses para repositorios

import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.PagoPendiente;
import com.tallerwebi.dominio.RepositorioPagoPendiente;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioPagoPendienteImpl implements RepositorioPagoPendiente {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioPagoPendienteImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(PagoPendiente pagoPendiente) {
    sessionFactory.getCurrentSession().saveOrUpdate(pagoPendiente);
  }

  @Override
  public List<PagoPendiente> obtenerPendientesPorHogar(Hogar hogar) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("FROM PagoPendiente p WHERE p.hogar = :hogar", PagoPendiente.class)
      .setParameter("hogar", hogar)
      .list();
  }

  @Override
  public PagoPendiente buscarPorDatos(String emisor, String receptor, Double monto, Hogar hogar) {
    try {
      return sessionFactory
        .getCurrentSession()
        .createQuery(
          "FROM PagoPendiente p WHERE p.emisor = :emisor AND p.receptor = :receptor AND p.monto = :monto AND p.hogar = :hogar",
          PagoPendiente.class
        )
        .setParameter("emisor", emisor)
        .setParameter("receptor", receptor)
        .setParameter("monto", monto)
        .setParameter("hogar", hogar)
        .uniqueResult();
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public void eliminar(PagoPendiente pagoPendiente) {
    if (pagoPendiente != null) {
      sessionFactory.getCurrentSession().delete(pagoPendiente);
    }
  }

  @Override
  public PagoPendiente buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(PagoPendiente.class, id);
  }

  @Override
  public void eliminarPorId(Long id) {
    PagoPendiente pendiente = buscarPorId(id);
    if (pendiente != null) {
      sessionFactory.getCurrentSession().delete(pendiente);
    }
  }
}
