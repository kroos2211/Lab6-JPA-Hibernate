package org.example.service;

import org.example.entities.Produit;
import org.example.util.HibernateUtil;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProduitService {
    public List<Produit> findAll() { return named("Produit.findAll", null, null); }
    public List<Produit> findByCategorie(long catId) { return named("Produit.findByCategorie", "catId", catId); }
    public List<Produit> searchByLibelle(String term) {
        return named("Produit.searchByLibelle", "term", Objects.requireNonNull(term).trim());
    }
    private List<Produit> named(String name, String parameter, Object value) {
        return HibernateUtil.withEntityManager(em -> {
            TypedQuery<Produit> query = em.createNamedQuery(name, Produit.class);
            if (parameter != null) query.setParameter(parameter, value);
            return query.getResultList();
        });
    }
    public List<Produit> search(String term, BigDecimal min, BigDecimal max) {
        if (min != null && min.signum() < 0 || max != null && max.signum() < 0)
            throw new IllegalArgumentException("Prix negatif");
        if (min != null && max != null && min.compareTo(max) > 0)
            throw new IllegalArgumentException("Prix minimum superieur au maximum");
        return HibernateUtil.withEntityManager(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Produit> cq = cb.createQuery(Produit.class);
            Root<Produit> root = cq.from(Produit.class);
            List<Predicate> predicates = new ArrayList<>();
            if (term != null && !term.isBlank()) predicates.add(cb.like(cb.lower(root.get("libelle")), "%" + term.trim().toLowerCase(java.util.Locale.ROOT) + "%"));
            if (min != null) predicates.add(cb.greaterThanOrEqualTo(root.get("prix"), min));
            if (max != null) predicates.add(cb.lessThanOrEqualTo(root.get("prix"), max));
            cq.select(root).where(predicates.toArray(new Predicate[0])).orderBy(cb.asc(root.get("id")));
            return em.createQuery(cq).getResultList();
        });
    }
    public List<Produit> findPage(int pageNumber, int pageSize) {
        if (pageNumber < 1 || pageSize < 1) throw new IllegalArgumentException("Page et taille positives requises");
        long offset = (long) (pageNumber - 1) * pageSize;
        if (offset > Integer.MAX_VALUE) throw new IllegalArgumentException("Numero de page trop grand");
        return HibernateUtil.withEntityManager(em -> {
            return em.createQuery("SELECT p FROM Produit p ORDER BY p.id", Produit.class)
                    .setFirstResult((int) offset).setMaxResults(pageSize).getResultList();
        });
    }
    public long count() {
        return HibernateUtil.withEntityManager(em -> {
            return em.createQuery("SELECT COUNT(p) FROM Produit p", Long.class).getSingleResult();
        });
    }
}
