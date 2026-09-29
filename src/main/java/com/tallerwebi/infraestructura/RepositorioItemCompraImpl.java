package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.ItemCompra;
import com.tallerwebi.dominio.enums.EstadoItemCompra;
import com.tallerwebi.dominio.repository.RepositorioItemCompra;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioItemCompra")
public class RepositorioItemCompraImpl implements RepositorioItemCompra {

    private final SessionFactory sessionFactory;

    @Autowired
    public RepositorioItemCompraImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public ItemCompra guardar(ItemCompra item) {
        sessionFactory.getCurrentSession().persist(item);
        return item;
    }

    @Override
    public ItemCompra obtenerPorId(Long id) {
        return sessionFactory.getCurrentSession().get(ItemCompra.class, id);
    }

    @Override
    public List<ItemCompra> obtenerActivos() {
        return sessionFactory
        .getCurrentSession()
        .createQuery("from ItemCompra where estado <> :archivado order by id", ItemCompra.class)
        .setParameter("archivado", EstadoItemCompra.ARCHIVADO)
        .getResultList();
    }

    @Override
    public List<ItemCompra> obtenerPorEstado(EstadoItemCompra estado) {
        return sessionFactory
        .getCurrentSession()
        .createQuery("from ItemCompra where estado = :estado order by id", ItemCompra.class)
        .setParameter("estado", estado)
        .getResultList();
    }

    @Override
    public void eliminar(ItemCompra item) {
        sessionFactory.getCurrentSession().remove(item);
    }
}
