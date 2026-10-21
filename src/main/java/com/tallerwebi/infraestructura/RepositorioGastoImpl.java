package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Gasto;
import com.tallerwebi.dominio.RepositorioGasto;
import com.tallerwebi.dominio.TipoGasto;
import com.tallerwebi.dominio.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioGastoImpl implements RepositorioGasto {

  SessionFactory sessionFactory;

  @Autowired
  public RepositorioGastoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Gasto gasto) {
    sessionFactory.getCurrentSession().persist(gasto);
  }

  @Override
  public Gasto buscarPorCodigo(String codigo) {
    String hql = "from Gasto g where g.codigo = :codigo";
    return sessionFactory
      .getCurrentSession()
      .createQuery(hql, Gasto.class)
      .setParameter("codigo", codigo)
      .uniqueResult();
  }

  @Override
  public Gasto buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Gasto.class, id);
  }

  @Override
  public Double obtenerMontoPorCodigo(String codigo) {
    String query = "select g.monto from Gasto g where g.codigo = :codigo";
    return sessionFactory
      .getCurrentSession()
      .createQuery(query, Double.class)
      .setParameter("codigo", codigo)
      .uniqueResult();
  }

  @Override
  public String obtenerNombreDeUsuarioPagadorPorCodigoGasto(String codigo) {
    String query = "select g.usuarioPagador from Gasto g where g.codigo = :codigo";
    return sessionFactory
      .getCurrentSession()
      .createQuery(query, Usuario.class)
      .setParameter("codigo", codigo)
      .uniqueResult()
      .getNombre();
  }

  @Override
  public List<Gasto> obtenerTodos() {
    String hql = "from Gasto";
    return sessionFactory.getCurrentSession().createQuery(hql, Gasto.class).getResultList();
  }

  @Override
  public List<Gasto> obtenerPorTipo(TipoGasto tipo) {
    String hql = "from Gasto g where g.tipoGasto = :tipo";
    return sessionFactory
      .getCurrentSession()
      .createQuery(hql, Gasto.class)
      .setParameter("tipo", tipo)
      .getResultList();
  }

  @Override
  public List<Gasto> obtenerGastosPendientesConVencimiento() {
    String hql =
      "FROM Gasto g WHERE g.pagado = false AND g.fechaVencimiento IS NOT NULL ORDER BY g.fechaVencimiento ASC";
    return sessionFactory.getCurrentSession().createQuery(hql, Gasto.class).getResultList();
  }

  @Override
  public void modificar(Gasto gasto) {
    sessionFactory.getCurrentSession().merge(gasto);
  }

  @Override
  public List<Gasto> obtenerGastosFijosRecurrentes() {
    String hql = "FROM Gasto g WHERE g.tipoGasto = 'FIJO' OR g.tipoGasto = 'RECURRENTE'";
    return sessionFactory.getCurrentSession().createQuery(hql, Gasto.class).getResultList();
  }

  @Override
  public void eliminar(Long id) {
    Gasto gasto = buscarPorId(id);
    if (gasto != null) {
      sessionFactory.getCurrentSession().remove(gasto);
    }
  }

  @Override
  public List<Gasto> obtenerPendientesConVencimientoEntre(
    LocalDateTime desde,
    LocalDateTime hasta
  ) {
    String query =
      "FROM Gasto g WHERE g.pagado = false AND g.fechaVencimiento BETWEEN :desde AND :hasta ORDER BY g.fechaVencimiento ASC";
    return sessionFactory
      .getCurrentSession()
      .createQuery(query, Gasto.class)
      .setParameter("desde", desde)
      .setParameter("hasta", hasta)
      .getResultList();
  }

  @Override
  public List<Gasto> obtenerPendientesVencidosAntesDe(LocalDateTime ahora) {
    String query =
      "FROM Gasto g WHERE g.pagado = false AND g.fechaVencimiento < :ahora ORDER BY g.fechaVencimiento ASC";
    return sessionFactory
      .getCurrentSession()
      .createQuery(query, Gasto.class)
      .setParameter("ahora", ahora)
      .getResultList();
  }
}
