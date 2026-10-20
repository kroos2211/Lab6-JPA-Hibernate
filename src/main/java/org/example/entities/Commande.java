package org.example.entities;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity @Table(name = "commandes")
public class Commande {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private LocalDate dateCommande;
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommandeProduit> lignes = new ArrayList<>();
    protected Commande() { }
    public Commande(LocalDate dateCommande) { setDateCommande(dateCommande); }
    public Long getId() { return id; }
    public LocalDate getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDate dateCommande) { this.dateCommande = Objects.requireNonNull(dateCommande); }
    public List<CommandeProduit> getLignes() { return lignes; }
    public CommandeProduit addProduit(Produit produit, int quantite) {
        Objects.requireNonNull(produit, "produit");
        if (produit.getId() == null) throw new IllegalArgumentException("Le produit doit etre persiste avant la commande");
        if (quantite <= 0) throw new IllegalArgumentException("Quantite strictement positive requise");
        if (lignes.stream().anyMatch(l -> l.getProduit() == produit || produit.getId().equals(l.getProduit().getId())))
            throw new IllegalArgumentException("Produit deja present dans la commande");
        CommandeProduit ligne = new CommandeProduit(this, produit, quantite);
        lignes.add(ligne);
        return ligne;
    }
    public void removeProduit(CommandeProduit ligne) {
        if (lignes.remove(ligne)) ligne.setCommande(null);
    }
    @Override public String toString() { return "Commande{id=" + id + ", date=" + dateCommande + "}"; }
}
