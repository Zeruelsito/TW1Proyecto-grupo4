package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.RepositorioHogar;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioHogarImpl implements RepositorioHogar {

  SessionFactory sessionFactory;

  @Autowired
  public RepositorioHogarImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Hogar obtenerHogarPorNombre(String nombre) {
    String query = "from Hogar h where h.nombreHogar = :nombre";
    return sessionFactory
      .getCurrentSession()
      .createQuery(query, Hogar.class)
      .setParameter("nombre", nombre)
      .uniqueResult();
  }

  @Override
  public List<Hogar> obtenerHogaresDeUsuario(String email) {
    String query = "FROM Hogar h WHERE h.usuarioCreador.email = :email";
    return sessionFactory
      .getCurrentSession()
      .createQuery(query, Hogar.class)
      .setParameter("email", email)
      .getResultList();
  }

  @Override
  public List<Hogar> obtenerHogares() {
    String query = "FROM Hogar h";
    return sessionFactory.getCurrentSession().createQuery(query, Hogar.class).getResultList();
  }

  @Override
  public Hogar obtenerHogarPorId(Long hogarId) {
    String hql = "FROM Hogar h WHERE h.id = :id";

    return sessionFactory
      .getCurrentSession()
      .createQuery(hql, Hogar.class)
      .setParameter("id", hogarId)
      .uniqueResult(); // Retorna un solo objeto o null si no existe
  }

  @Override
  public void guardar(Hogar hogar) {
    sessionFactory.getCurrentSession().persist(hogar);
  }
}
