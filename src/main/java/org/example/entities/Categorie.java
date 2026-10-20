package org.example.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "categories", uniqueConstraints = @UniqueConstraint(columnNames = "nom"))
public class Categorie {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String nom;
    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Produit> produits = new ArrayList<>();

    protected Categorie() { }
    public Categorie(String nom) { setNom(nom); }
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) {
        if (nom == null || nom.isBlank()) throw new IllegalArgumentException("Nom de categorie obligatoire");
        this.nom = nom.trim();
    }
    public List<Produit> getProduits() { return produits; }
    public void addProduit(Produit produit) {
        Objects.requireNonNull(produit, "produit");
        produit.setCategorie(this);
    }
    public void removeProduit(Produit produit) {
        if (produit != null && produit.getCategorie() == this) produit.setCategorie(null);
    }
    @Override public String toString() { return "Categorie{id=" + id + ", nom='" + nom + "'}"; }
}
