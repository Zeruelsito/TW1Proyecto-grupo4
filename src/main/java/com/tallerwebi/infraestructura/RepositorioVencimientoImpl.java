package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioVencimiento;
import com.tallerwebi.dominio.Vencimiento;
import java.time.LocalDate;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioVencimiento")
public class RepositorioVencimientoImpl implements RepositorioVencimiento {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioVencimientoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Vencimiento vencimiento) {
    sessionFactory.getCurrentSession().persist(vencimiento);
  }

  @Override // del mas antiguo al mas nuevo
  public List<Vencimiento> obtenerTodosOrdenadosPorFecha() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Vencimiento order by fechaVencimiento, id", Vencimiento.class)
      .getResultList();
  }

  @Override // a partir de tal fecha, incluyendo la misma
  public List<Vencimiento> obtenerDesde(LocalDate fecha) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from Vencimiento where fechaVencimiento >= :fecha order by fechaVencimiento, id",
        Vencimiento.class
      )
      .setParameter("fecha", fecha)
      .getResultList();
  }
}
