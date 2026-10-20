package org.example.entities;

import javax.persistence.*;

@Entity @Table(name = "fournisseurs")
@PrimaryKeyJoinColumn(name = "id")
public class Fournisseur extends User {
    @Column(nullable = false)
    private String adresse;
    protected Fournisseur() { }
    public Fournisseur(String nom, String adresse) { super(nom); setAdresse(adresse); }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) {
        if (adresse == null || adresse.isBlank()) throw new IllegalArgumentException("Adresse obligatoire");
        this.adresse = adresse.trim();
    }
    @Override public String toString() { return "Fournisseur{id=" + getId() + ", nom='" + getNom() + "'}"; }
}
