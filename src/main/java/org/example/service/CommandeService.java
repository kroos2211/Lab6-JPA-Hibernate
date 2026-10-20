package org.example.service;

import org.example.dao.GenericDAO;
import org.example.entities.Commande;
import org.example.entities.CommandeProduit;
import org.example.entities.Produit;
import java.util.Objects;

/** Persists an order while resolving product references in the same transaction. */
public class CommandeService {
    private final GenericDAO<Commande, Long> dao = new GenericDAO<>(Commande.class);
    public Commande create(Commande commande) {
        Objects.requireNonNull(commande, "commande");
        return dao.inTransaction(em -> {
            for (CommandeProduit ligne : commande.getLignes()) {
                Long id = ligne.getProduit().getId();
                Produit managed = em.find(Produit.class, id);
                if (managed == null) throw new IllegalArgumentException("Produit introuvable : " + id);
                ligne.setProduit(managed);
            }
            em.persist(commande);
            return commande;
        });
    }
}
