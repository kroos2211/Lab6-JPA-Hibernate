package org.example.entities;

import javax.persistence.*;
import java.util.Objects;

@Entity @Table(name = "commande_produit")
public class CommandeProduit {
    @EmbeddedId private CommandeProduitPK id = new CommandeProduitPK();
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("commandeId") @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("produitId") @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;
    @Column(nullable = false) private int quantite;
    protected CommandeProduit() { }
    public CommandeProduit(Commande commande, Produit produit, int quantite) {
        this.commande = commande; this.produit = produit; setQuantite(quantite);
    }
    public CommandeProduitPK getId() { return id; }
    public Commande getCommande() { return commande; }
    void setCommande(Commande commande) { this.commande = commande; }
    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = Objects.requireNonNull(produit); }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) {
        if (quantite <= 0) throw new IllegalArgumentException("Quantite strictement positive requise");
        this.quantite = quantite;
    }
    @Override public String toString() { return "Ligne{id=" + id + ", quantite=" + quantite + "}"; }
}
