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
  public Hogar buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Hogar.class, id);
  }

  @Override
  public List<Hogar> obtenerTodos() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Hogar order by id", Hogar.class)
      .getResultList();
  }
}
