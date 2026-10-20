package org.example;

import org.example.dao.GenericDAO;
import org.example.entities.*;
import org.example.service.*;
import org.example.util.HibernateUtil;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public final class App {
    private App() { }
    public static void main(String[] args) {
        System.out.println("=== LAB 6 : GESTION COMMERCIALE ===");
        try {
            run();
        } finally {
            HibernateUtil.shutdown();
            System.out.println("=== FIN DU LAB 6 ===");
        }
    }
    private static void run() {
        section("INITIALISATION HIBERNATE");
        HibernateUtil.getSessionFactory();
        System.out.println("Base : " + System.getenv().getOrDefault("APP_DB", "h2"));
        GenericDAO<Categorie, Long> categories = new GenericDAO<>(Categorie.class);
        GenericDAO<Produit, Long> produits = new GenericDAO<>(Produit.class);
        ProduitService recherche = new ProduitService();

        section("CREATION DES CATEGORIES");
        Categorie informatique = categories.create(new Categorie("Informatique"));
        Categorie mobilier = categories.create(new Categorie("Mobilier"));
        Categorie papeterie = categories.create(new Categorie("Papeterie"));
        categories.findAll().forEach(System.out::println);

        section("CREATION DES PRODUITS");
        Produit portable = product(produits, informatique, "Ordinateur portable", "800.00");
        Produit souris = product(produits, informatique, "Souris", "25.00");
        product(produits, informatique, "Clavier", "50.00");
        product(produits, mobilier, "Bureau", "250.00");
        product(produits, mobilier, "Chaise", "120.00");
        product(produits, papeterie, "Cahier", "5.00");
        recherche.findAll().forEach(System.out::println);

        section("OPERATIONS CRUD");
        CrudService<Produit, Long> crud = new CrudService<>(produits);
        Produit temporaire = new Produit("Produit temporaire", new BigDecimal("9.00"), LocalDate.now());
        temporaire.setCategorie(papeterie);
        crud.create(temporaire);
        System.out.println("Create : " + temporaire);
        System.out.println("Read : " + crud.getById(temporaire.getId()));
        temporaire.setPrix(new BigDecimal("10.00"));
        crud.update(temporaire);
        System.out.println("Update : " + crud.getById(temporaire.getId()));
        crud.delete(temporaire);
        System.out.println("Delete : trouve=" + (crud.getById(temporaire.getId()) != null));

        section("NAMED QUERIES");
        System.out.println("findAll : " + recherche.findAll().size());
        System.out.println("findByCategorie Informatique : " + recherche.findByCategorie(informatique.getId()));
        System.out.println("searchByLibelle 'sour' : " + recherche.searchByLibelle("sour"));

        section("RECHERCHE CRITERIA API");
        System.out.println("Nom 'cha' : " + recherche.search("cha", null, null));
        System.out.println("Prix minimum 100 : " + recherche.search(null, new BigDecimal("100"), null));
        System.out.println("Prix maximum 50 : " + recherche.search(null, null, new BigDecimal("50")));
        System.out.println("Combinee 'ch' 10..150 : " + recherche.search("ch", new BigDecimal("10"), new BigDecimal("150")));
        System.out.println("Sans filtre : " + recherche.search(null, null, null).size());

        section("PAGINATION PAGE 1");
        System.out.println("Total=" + recherche.count() + ", page 1=" + recherche.findPage(1, 3));
        section("PAGINATION PAGE 2");
        System.out.println("Total=" + recherche.count() + ", page 2=" + recherche.findPage(2, 3));

        section("HERITAGE JOINED");
        GenericDAO<User, Long> users = new GenericDAO<>(User.class);
        Client client = new Client("Alice", LocalDate.now().minusMonths(2));
        Fournisseur fournisseur = new Fournisseur("Fournitures Atlas", "Casablanca");
        users.create(client);
        users.create(fournisseur);
        System.out.println(client);
        System.out.println(fournisseur);
        System.out.println("Recherche polymorphique : " + users.findAll());

        section("CREATION DES COMMANDES");
        Commande commande = new Commande(YearMonth.now().minusMonths(1).atDay(15));
        commande.addProduit(portable, 2);
        commande.addProduit(souris, 3);
        new CommandeService().create(commande);
        System.out.println(commande);
        section("RELATION N:N ET CLE COMPOSEE");
        commande.getLignes().forEach(l -> System.out.println("Produit=" + l.getProduit().getLibelle()
                + ", quantite=" + l.getQuantite() + ", cle=" + l.getId()));

        Produit orphelin = product(produits, mobilier, "Produit orphelin", "1.00");
        categories.inTransaction(em -> {
            Categorie managed = em.find(Categorie.class, mobilier.getId());
            Produit managedProduit = em.find(Produit.class, orphelin.getId());
            managed.removeProduit(managedProduit);
            return null;
        });
        System.out.println("Suppression orpheline : trouve=" + (produits.find(orphelin.getId()) != null));

        section("TRANSACTIONS ET ROLLBACK");
        int avant = categories.findAll().size();
        try {
            categories.inTransaction(em -> {
                em.persist(new Categorie("Annulation volontaire"));
                em.flush();
                throw new IllegalStateException("Erreur de demonstration");
            });
        } catch (IllegalStateException expected) {
            System.out.println("Rollback : " + expected.getMessage());
        }
        System.out.println("Categories avant/apres rollback : " + avant + "/" + categories.findAll().size());
        EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
        Categorie detached;
        try {
            detached = em.find(Categorie.class, papeterie.getId());
            System.out.println("Managed : " + em.contains(detached));
        } finally { em.close(); }
        System.out.println("Detached apres fermeture : " + !em.isOpen());
        detached.setNom("Papeterie et fournitures");
        categories.update(detached);
        System.out.println("Merge : " + categories.find(papeterie.getId()));

        section("RAPPORT DES VENTES MENSUELLES");
        List<RapportVentesService.LigneRapport> rapport = new RapportVentesService().moisPrecedent();
        rapport.forEach(System.out::println);
    }
    private static Produit product(GenericDAO<Produit, Long> dao, Categorie categorie, String nom, String prix) {
        Produit produit = new Produit(nom, new BigDecimal(prix), LocalDate.now());
        produit.setCategorie(categorie);
        return dao.create(produit);
    }
    private static void section(String nom) { System.out.println("\n=== " + nom + " ==="); }
}
