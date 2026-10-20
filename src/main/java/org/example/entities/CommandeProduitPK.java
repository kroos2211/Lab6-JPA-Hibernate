package org.example.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CommandeProduitPK implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "commande_id") private Long commandeId;
    @Column(name = "produit_id") private Long produitId;
    public CommandeProduitPK() { }
    public CommandeProduitPK(Long commandeId, Long produitId) {
        this.commandeId = commandeId; this.produitId = produitId;
    }
    public Long getCommandeId() { return commandeId; }
    public Long getProduitId() { return produitId; }
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CommandeProduitPK)) return false;
        CommandeProduitPK that = (CommandeProduitPK) other;
        return Objects.equals(commandeId, that.commandeId) && Objects.equals(produitId, that.produitId);
    }
    @Override public int hashCode() { return Objects.hash(commandeId, produitId); }
    @Override public String toString() { return "(" + commandeId + ", " + produitId + ")"; }
}
