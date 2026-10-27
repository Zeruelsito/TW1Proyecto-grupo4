package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Hogar;
import com.tallerwebi.dominio.RepositorioHogar;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioHogar")
public class RepositorioHogarImpl implements RepositorioHogar {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioHogarImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Hogar hogar) {
    sessionFactory.getCurrentSession().persist(hogar);
  }

  @Override
  public void actualizar(Hogar hogar) {
    sessionFactory.getCurrentSession().merge(hogar);
  }

  @Override
  public Hogar buscarPorId(Long id) {
    String hql =
      "SELECT DISTINCT h FROM Hogar h LEFT JOIN FETCH h.integrantes WHERE h.id = :hogarId";
    return sessionFactory
      .getCurrentSession()
      .createQuery(hql, Hogar.class)
      .setParameter("hogarId", id)
      .getSingleResultOrNull();
  }

  @Override
  public List<Hogar> obtenerHogaresDeUsuario(Long usuarioId) {
    String hql = "SELECT h FROM Hogar h JOIN h.integrantes u WHERE u.id = :usuarioId";
    return sessionFactory
      .getCurrentSession() // O entityManager si usan JPA puro
      .createQuery(hql, Hogar.class)
      .setParameter("usuarioId", usuarioId)
      .getResultList();
  }

  @Override
  public List<Hogar> obtenerTodos() {
    String hql = "FROM Hogar";
    return sessionFactory
      .getCurrentSession() // O entityManager si usan JPA puro
      .createQuery(hql, Hogar.class)
      .getResultList();
  }
}
