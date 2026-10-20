package org.example;

import org.example.dao.GenericDAO;
import org.example.entities.*;
import org.example.service.*;
import org.example.util.HibernateUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.Assert.*;

public class Lab6Test {
    private final GenericDAO<Categorie, Long> categories = new GenericDAO<>(Categorie.class);
    private final GenericDAO<Produit, Long> produits = new GenericDAO<>(Produit.class);
    private final GenericDAO<Commande, Long> commandes = new GenericDAO<>(Commande.class);
    private final GenericDAO<User, Long> users = new GenericDAO<>(User.class);
    private final ProduitService recherche = new ProduitService();

    @Before public void cleanDatabase() {
        categories.inTransaction(em -> {
            for (String table : new String[]{"commande_produit", "commandes", "produits", "categories", "clients", "fournisseurs", "users"})
                em.createNativeQuery("DELETE FROM " + table).executeUpdate();
            return null;
        });
    }
    @AfterClass public static void close() { HibernateUtil.shutdown(); }
    private Categorie cat(String nom) { return categories.create(new Categorie(nom)); }
    private Produit product(Categorie cat, String name, String price) {
        Produit p = new Produit(name, new BigDecimal(price), LocalDate.of(2024, 1, 1));
        p.setCategorie(cat);
        return produits.create(p);
    }

    @Test public void creationCategorie() { assertNotNull(cat("Informatique").getId()); }
    @Test public void creationProduit() { assertNotNull(product(cat("A"), "Souris", "25").getId()); }
    @Test public void associationProduitCategorie() {
        Categorie c = cat("A"); Produit p = product(c, "Souris", "25");
        assertEquals(c.getId(), HibernateUtil.withEntityManager(em -> em.find(Produit.class, p.getId()).getCategorie().getId()));
    }
    @Test public void genericCreate() { assertNotNull(cat("A").getId()); }
    @Test public void genericFind() { Categorie c = cat("A"); assertEquals("A", categories.find(c.getId()).getNom()); }
    @Test public void genericFindAll() { cat("A"); cat("B"); assertEquals(2, categories.findAll().size()); }
    @Test public void genericUpdateDetached() {
        Categorie c = cat("A"); c.setNom("B"); categories.update(c);
        assertEquals("B", categories.find(c.getId()).getNom());
    }
    @Test public void genericDeleteDetached() { Categorie c = cat("A"); categories.delete(c); assertNull(categories.find(c.getId())); }
    @Test public void crudService() {
        CrudService<Categorie, Long> service = new CrudService<>(categories);
        Categorie c = service.create(new Categorie("A"));
        assertEquals(1, service.getAll().size());
        assertEquals("A", service.getById(c.getId()).getNom());
        c.setNom("B"); service.update(c); assertEquals("B", service.getById(c.getId()).getNom());
        service.delete(c); assertNull(service.getById(c.getId()));
    }
    @Test(expected = NullPointerException.class) public void validationEntiteNulle() {
        new CrudService<>(categories).create(null);
    }
    @Test public void namedFindAll() { product(cat("A"), "A", "1"); product(cat("B"), "B", "2"); assertEquals(2, recherche.findAll().size()); }
    @Test public void namedParCategorie() {
        Categorie c = cat("A"); product(c, "Oui", "1"); product(cat("B"), "Non", "1");
        assertEquals("Oui", recherche.findByCategorie(c.getId()).get(0).getLibelle());
        assertEquals(1, recherche.findByCategorie(c.getId()).size());
    }
    @Test public void namedParLibelle() {
        Categorie c = cat("A"); product(c, "Souris", "1"); product(c, "Clavier", "2");
        assertEquals("Souris", recherche.searchByLibelle("SOUR").get(0).getLibelle());
    }
    @Test public void criteriaNom() {
        Categorie c = cat("A"); product(c, "Chaise", "100"); product(c, "Bureau", "200");
        assertEquals("Chaise", recherche.search("chai", null, null).get(0).getLibelle());
    }
    @Test public void criteriaPrixMinimum() {
        Categorie c = cat("A"); product(c, "Petit", "5"); product(c, "Grand", "50");
        assertEquals("Grand", recherche.search(null, new BigDecimal("10"), null).get(0).getLibelle());
    }
    @Test public void criteriaPrixMaximum() {
        Categorie c = cat("A"); product(c, "Petit", "5"); product(c, "Grand", "50");
        assertEquals("Petit", recherche.search(null, null, new BigDecimal("10")).get(0).getLibelle());
    }
    @Test public void criteriaCombines() {
        Categorie c = cat("A"); product(c, "Chaise basse", "20"); product(c, "Chaise haute", "100"); product(c, "Bureau", "50");
        assertEquals("Chaise basse", recherche.search("chaise", new BigDecimal("10"), new BigDecimal("50")).get(0).getLibelle());
    }
    @Test public void criteriaSansFiltre() { Categorie c = cat("A"); product(c, "A", "1"); product(c, "B", "2"); assertEquals(2, recherche.search(null, null, null).size()); }
    @Test(expected = IllegalArgumentException.class) public void criteriaPrixInverses() { recherche.search(null, new BigDecimal("20"), new BigDecimal("10")); }
    @Test public void paginationPremierePage() {
        Categorie c = cat("A"); for (int i = 0; i < 5; i++) product(c, "P" + i, "1");
        assertEquals(3, recherche.findPage(1, 3).size());
    }
    @Test public void paginationDeuxPagesDistinctes() {
        Categorie c = cat("A"); for (int i = 0; i < 6; i++) product(c, "P" + i, "1");
        List<Produit> first = recherche.findPage(1, 3), second = recherche.findPage(2, 3);
        Set<Long> ids = first.stream().map(Produit::getId).collect(Collectors.toSet());
        assertEquals(3, second.size()); assertTrue(second.stream().noneMatch(p -> ids.contains(p.getId())));
    }
    @Test(expected = IllegalArgumentException.class) public void paginationInvalide() { recherche.findPage(0, 3); }
    @Test public void heritageClient() { Client c = new Client("Alice", LocalDate.now()); users.create(c); assertTrue(users.find(c.getId()) instanceof Client); }
    @Test public void heritageFournisseur() { Fournisseur f = new Fournisseur("Atlas", "Rabat"); users.create(f); assertTrue(users.find(f.getId()) instanceof Fournisseur); }
    @Test public void recherchePolymorphique() {
        users.create(new Client("Alice", LocalDate.now())); users.create(new Fournisseur("Atlas", "Rabat"));
        assertEquals(2, users.findAll().size());
    }
    @Test public void creationCommande() {
        Produit p = product(cat("A"), "P", "10"); Commande c = new Commande(LocalDate.now()); c.addProduit(p, 2); new CommandeService().create(c);
        assertNotNull(c.getId()); assertEquals(1, c.getLignes().size());
    }
    @Test public void cleComposee() {
        assertEquals(new CommandeProduitPK(1L, 2L), new CommandeProduitPK(1L, 2L));
        assertEquals(new CommandeProduitPK(1L, 2L).hashCode(), new CommandeProduitPK(1L, 2L).hashCode());
        assertNotEquals(new CommandeProduitPK(1L, 2L), new CommandeProduitPK(1L, 3L));
    }
    @Test public void relationQuantitesEtCles() {
        Categorie cat = cat("A"); Produit p1 = product(cat, "P1", "10"), p2 = product(cat, "P2", "20");
        Commande c = new Commande(LocalDate.now()); c.addProduit(p1, 2); c.addProduit(p2, 3); new CommandeService().create(c);
        HibernateUtil.withEntityManager(em -> {
            List<CommandeProduit> lignes = em.createQuery("SELECT l FROM CommandeProduit l WHERE l.commande.id = :id ORDER BY l.produit.id", CommandeProduit.class)
                    .setParameter("id", c.getId()).getResultList();
            assertEquals(2, lignes.size()); assertEquals(2, lignes.get(0).getQuantite()); assertEquals(3, lignes.get(1).getQuantite());
            assertEquals(c.getId(), lignes.get(0).getId().getCommandeId());
            assertEquals(p1.getId(), lignes.get(0).getId().getProduitId()); return null;
        });
    }
    @Test(expected = IllegalArgumentException.class) public void quantiteInvalide() {
        Produit p = product(cat("A"), "P", "1"); new Commande(LocalDate.now()).addProduit(p, 0);
    }
    @Test public void suppressionOrpheline() {
        Categorie c = cat("A"); Produit p = product(c, "P", "1");
        categories.inTransaction(em -> { em.find(Categorie.class, c.getId()).removeProduit(em.find(Produit.class, p.getId())); return null; });
        assertNull(produits.find(p.getId()));
    }
    @Test public void transactionRollback() {
        try {
            categories.inTransaction(em -> { em.persist(new Categorie("Annulee")); em.flush(); throw new IllegalStateException("rollback"); });
            fail("Exception attendue");
        } catch (IllegalStateException expected) { assertEquals("rollback", expected.getMessage()); }
        assertTrue(categories.findAll().isEmpty());
    }
    @Test public void entiteDetachedMerge() {
        Categorie c = cat("A"); EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        Categorie detached = em.find(Categorie.class, c.getId()); em.close();
        detached.setNom("B"); categories.update(detached);
        assertEquals("B", categories.find(c.getId()).getNom());
    }
    @Test public void rapportMensuelAgrege() {
        Categorie a = cat("A"), b = cat("B");
        Produit p1 = product(a, "P1", "10"), p2 = product(a, "P2", "5"), p3 = product(b, "P3", "7");
        Commande avril = new Commande(LocalDate.of(2024, 4, 15));
        avril.addProduit(p1, 2); avril.addProduit(p2, 3); avril.addProduit(p3, 1); new CommandeService().create(avril);
        Commande mai = new Commande(LocalDate.of(2024, 5, 1)); mai.addProduit(p1, 99); new CommandeService().create(mai);
        List<RapportVentesService.LigneRapport> rapport = new RapportVentesService().pourMois(YearMonth.of(2024, 4));
        assertEquals(2, rapport.size());
        assertEquals(0, new BigDecimal("35").compareTo(rapport.get(0).getChiffreAffaires()));
        assertEquals(0, new BigDecimal("7").compareTo(rapport.get(1).getChiffreAffaires()));
        assertEquals(YearMonth.of(2024, 4), rapport.get(0).getMois());
    }
}
