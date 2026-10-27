package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.PagoRealizado;
import com.tallerwebi.dominio.RepositorioPagoRealizado;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioPagoRealizadoImpl implements RepositorioPagoRealizado {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioPagoRealizadoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(PagoRealizado pago) {
    sessionFactory.getCurrentSession().persist(pago);
  }

  @Override
  public List<PagoRealizado> obtenerPorHogarId(Long hogarId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("FROM PagoRealizado p WHERE p.hogar.id = :hogarId", PagoRealizado.class)
      .setParameter("hogarId", hogarId)
      .getResultList();
  }
}
