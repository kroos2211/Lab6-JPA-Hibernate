package org.example.entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "produits")
@NamedQueries({
    @NamedQuery(name = "Produit.findAll", query = "SELECT p FROM Produit p ORDER BY p.id"),
    @NamedQuery(name = "Produit.findByCategorie", query = "SELECT p FROM Produit p WHERE p.categorie.id = :catId ORDER BY p.id"),
    @NamedQuery(name = "Produit.searchByLibelle", query = "SELECT p FROM Produit p WHERE LOWER(p.libelle) LIKE LOWER(CONCAT('%', :term, '%')) ORDER BY p.id")
})
public class Produit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String libelle;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal prix;
    @Column(nullable = false)
    private LocalDate dateCreation;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    protected Produit() { }
    public Produit(String libelle, BigDecimal prix, LocalDate dateCreation) {
        setLibelle(libelle); setPrix(prix); setDateCreation(dateCreation);
    }
    public Long getId() { return id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) {
        if (libelle == null || libelle.isBlank()) throw new IllegalArgumentException("Libelle obligatoire");
        this.libelle = libelle.trim();
    }
    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) {
        if (prix == null || prix.signum() < 0) throw new IllegalArgumentException("Prix positif ou nul obligatoire");
        this.prix = prix;
    }
    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) {
        if (dateCreation == null) throw new IllegalArgumentException("Date de creation obligatoire");
        this.dateCreation = dateCreation;
    }
    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) {
        if (this.categorie == categorie) return;
        if (this.categorie != null) this.categorie.getProduits().remove(this);
        this.categorie = categorie;
        if (categorie != null && !categorie.getProduits().contains(this)) categorie.getProduits().add(this);
    }
    @Override public String toString() {
        return "Produit{id=" + id + ", libelle='" + libelle + "', prix=" + prix
                + ", categorieId=" + (categorie == null ? null : categorie.getId()) + "}";
    }
}
