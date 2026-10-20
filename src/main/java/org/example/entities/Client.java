package org.example.entities;

import javax.persistence.*;
import java.time.LocalDate;

@Entity @Table(name = "clients")
@PrimaryKeyJoinColumn(name = "id")
public class Client extends User {
    @Column(nullable = false)
    private LocalDate dateInscription;
    protected Client() { }
    public Client(String nom, LocalDate dateInscription) { super(nom); setDateInscription(dateInscription); }
    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate dateInscription) {
        if (dateInscription == null) throw new IllegalArgumentException("Date d'inscription obligatoire");
        this.dateInscription = dateInscription;
    }
    @Override public String toString() { return "Client{id=" + getId() + ", nom='" + getNom() + "'}"; }
}
