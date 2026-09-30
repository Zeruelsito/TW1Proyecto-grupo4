package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Voto;
import com.tallerwebi.dominio.RepositorioVoto;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
 
@Repository("repositorioVoto")
public class RepositorioVotoImpl implements RepositorioVoto {
 
  private static final String ID_PROPUESTA = "idPropuesta";
 
  private SessionFactory sessionFactory;
 
  public RepositorioVotoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }
 
  @Override
  public Voto guardar(Voto voto) {
    sessionFactory.getCurrentSession().persist(voto);
    return voto;
  }
 
  @Override
  public Voto obtenerPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Voto.class, id);
  }
 
  @Override
  public List<Voto> obtenerVotosPorPropuesta(Long id) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Voto where idPropuesta = :idPropuesta", Voto.class)
      .setParameter(ID_PROPUESTA, id)
      .getResultList();
  }
 
  @Override
  public List<Voto> obtenerTodosLosVotos() {
    return sessionFactory.getCurrentSession().createQuery("from Voto", Voto.class).getResultList();
  }
 
  @Override
  public List<Voto> obtenerVotosPorUsuario(Long idUsuario) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Voto where idUsuario = :idUsuario", Voto.class)
      .setParameter("idUsuario", idUsuario)
      .getResultList();
  }
 
  @Override
  public boolean existeVoto(Long idUsuario, Long idPropuesta) {
    Long cantidad = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(v) from Voto v where v.idUsuario = :idUsuario and v.idPropuesta = :idPropuesta",
        Long.class
      )
      .setParameter("idUsuario", idUsuario)
      .setParameter(ID_PROPUESTA, idPropuesta)
      .getSingleResult();
    return cantidad > 0;
  }
 
  @Override
  public Long obtenerVotosAfirmativosPorPropuesta(Long idPropuesta) {
    List<Voto> votosAfirmativos = sessionFactory
      .getCurrentSession()
      .createQuery("from Voto where idPropuesta = :idPropuesta and esAfirmativo = true", Voto.class)
      .setParameter(ID_PROPUESTA, idPropuesta)
      .getResultList();
    return (long) votosAfirmativos.size();
  }
 
  @Override
  public Long obtenerVotosNegativosPorPropuesta(Long idPropuesta) {
    List<Voto> votosNegativos = sessionFactory
      .getCurrentSession()
      .createQuery(
        "from Voto where idPropuesta = :idPropuesta and esAfirmativo = false",
        Voto.class
      )
      .setParameter(ID_PROPUESTA, idPropuesta)
      .getResultList();
    return (long) votosNegativos.size();
  }
}
