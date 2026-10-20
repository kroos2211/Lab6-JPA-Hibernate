package org.example.dao;

import org.example.util.HibernateUtil;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class GenericDAO<T, ID extends Serializable> {
    private final Class<T> entityClass;
    public GenericDAO(Class<T> entityClass) { this.entityClass = Objects.requireNonNull(entityClass); }

    public T create(T entity) {
        Objects.requireNonNull(entity, "entity");
        return inTransaction(em -> { em.persist(entity); return entity; });
    }
    public T find(ID id) {
        Objects.requireNonNull(id, "id");
        return HibernateUtil.withEntityManager(em -> em.find(entityClass, id));
    }
    public List<T> findAll() {
        return HibernateUtil.withEntityManager(em -> {
            CriteriaQuery<T> query = em.getCriteriaBuilder().createQuery(entityClass);
            Root<T> root = query.from(entityClass);
            query.select(root);
            return em.createQuery(query).getResultList();
        });
    }
    public T update(T entity) {
        Objects.requireNonNull(entity, "entity");
        return inTransaction(em -> em.merge(entity));
    }
    public void delete(T entity) {
        Objects.requireNonNull(entity, "entity");
        inTransaction(em -> { em.remove(em.contains(entity) ? entity : em.merge(entity)); return null; });
    }
    public <R> R inTransaction(Function<EntityManager, R> work) {
        Objects.requireNonNull(work, "work");
        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            R result = work.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
