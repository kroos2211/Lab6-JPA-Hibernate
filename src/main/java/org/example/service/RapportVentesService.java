package org.example.service;

import org.example.entities.*;
import org.example.util.HibernateUtil;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

public class RapportVentesService {
    public static final class LigneRapport {
        private final String categorie;
        private final YearMonth mois;
        private final BigDecimal chiffreAffaires;
        public LigneRapport(String categorie, YearMonth mois, BigDecimal chiffreAffaires) {
            this.categorie = categorie; this.mois = mois; this.chiffreAffaires = chiffreAffaires;
        }
        public String getCategorie() { return categorie; }
        public YearMonth getMois() { return mois; }
        public BigDecimal getChiffreAffaires() { return chiffreAffaires; }
        @Override public String toString() { return mois + " | " + categorie + " | " + chiffreAffaires; }
    }
    public List<LigneRapport> moisPrecedent() { return pourMois(YearMonth.now().minusMonths(1)); }
    public List<LigneRapport> pourMois(YearMonth mois) {
        if (mois == null) throw new IllegalArgumentException("Mois obligatoire");
        LocalDate debut = mois.atDay(1);
        LocalDate fin = mois.plusMonths(1).atDay(1);
        return HibernateUtil.withEntityManager(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Tuple> cq = cb.createTupleQuery();
            Root<CommandeProduit> ligne = cq.from(CommandeProduit.class);
            Join<CommandeProduit, Commande> commande = ligne.join("commande");
            Join<CommandeProduit, Produit> produit = ligne.join("produit");
            Join<Produit, Categorie> categorie = produit.join("categorie");
            Expression<BigDecimal> montant = cb.prod(produit.<BigDecimal>get("prix"), ligne.<Integer>get("quantite").as(BigDecimal.class));
            cq.multiselect(categorie.get("nom").alias("categorie"), cb.sum(montant).alias("total"));
            cq.where(cb.greaterThanOrEqualTo(commande.get("dateCommande"), debut), cb.lessThan(commande.get("dateCommande"), fin));
            cq.groupBy(categorie.get("id"), categorie.get("nom"));
            cq.orderBy(cb.asc(categorie.get("nom")));
            return em.createQuery(cq).getResultList().stream()
                    .map(row -> new LigneRapport(row.get("categorie", String.class), mois, row.get("total", BigDecimal.class)))
                    .collect(Collectors.toList());
        });
    }
}
