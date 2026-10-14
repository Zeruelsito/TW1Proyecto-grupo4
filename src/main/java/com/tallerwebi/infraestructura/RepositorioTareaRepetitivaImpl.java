package com.tallerwebi.infraestructura;

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
    String query = "select count(t) from TareaRepetitiva t where t.usuarioAsignado = :usuario";
    return sessionFactory
      .getCurrentSession()
      .createQuery(query, Long.class)
      .setParameter("usuario", usuario)
      .getSingleResult()
      .intValue();
  }
}
