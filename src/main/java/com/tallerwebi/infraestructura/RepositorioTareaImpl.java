package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioTarea;
import com.tallerwebi.dominio.Tarea;
import com.tallerwebi.dominio.Usuario;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioTareaImpl implements RepositorioTarea {

  SessionFactory sessionFactory;

  @Autowired
  public RepositorioTareaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Tarea tarea) {
    sessionFactory.getCurrentSession().persist(tarea);
  }

  @Override
  public Tarea buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Tarea.class, id);
  }

  @Override
  public List<Tarea> obtenerTodas() {
    String hql = "from Tarea";
    return sessionFactory.getCurrentSession().createQuery(hql, Tarea.class).getResultList();
  }

  @Override
  public List<Tarea> obtenerPorUsuario(Usuario usuarioRealizador) {
    String hql = "from Tarea t where t.usuarioRealizador = :usuario";
    return sessionFactory
      .getCurrentSession()
      .createQuery(hql, Tarea.class)
      .setParameter("usuario", usuarioRealizador)
      .getResultList();
  }
}
