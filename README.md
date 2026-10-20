# Lab 6 – Gestion commerciale avec JPA et Hibernate

## Objectif

Ce TP gère des catégories, produits, commandes et utilisateurs avec JPA et Hibernate. Il montre les relations entre entités, les transactions et les requêtes de recherche.

## Technologies utilisées

Java 17, Maven 3.9, JPA 2.2, Hibernate 5.6, MySQL 8, H2 et JUnit 4.

## Travail réalisé

- Catégories, produits, DAO générique et service CRUD.
- Named Queries, Criteria API et pagination.
- Héritage JOINED, commandes N:N avec quantité et clé composée.
- Rapport des ventes du mois précédent par catégorie, calculé en base.
- Servlet `/rapport-ventes` compilé dans le WAR.

## Exécution

Avec le JDK 17 actif, à la racine du projet :

```powershell
mvn clean compile
mvn clean test
mvn clean package
mvn exec:java '-Dexec.mainClass=org.example.App'
```

L'exécution vérifiée utilise **H2 en mémoire** (`APP_DB=h2` par défaut). MySQL n'était pas accessible sur `localhost:3306`. Pour l'utiliser, créer la base `gestion_commerciale`, puis définir `APP_DB=mysql`, `DB_USER` et `DB_PASSWORD` dans l'environnement ; aucun identifiant n'est enregistré dans le dépôt. Le servlet n'a pas été déployé : les installations Tomcat détectées utilisent Jakarta Servlet, tandis que ce TP utilise `javax.servlet`.

## Résultats

| Vérification | Résultat réel |
|---|---|
| Compilation | BUILD SUCCESS |
| Tests JUnit | 33 réussis, 0 échec, 0 erreur |
| Application | BUILD SUCCESS ; démonstration complète |
| Packaging | BUILD SUCCESS ; WAR généré |
| Rapport mensuel | Septembre 2026 : Informatique, 1 675.0000 |

Les sorties complètes sont dans [`resultats/`](resultats/). Les images ci-dessous sont **des extraits de ces journaux réels, pas des captures du terminal intégré**.

## Captures d'écran

1. Compilation : ![Extrait réel de la compilation](captures/01-compilation.png)
2. Tests : ![Extrait réel des tests JUnit](captures/02-tests-junit.png)
3. Catégories et produits : ![Extrait réel des catégories et produits](captures/03-categories-produits.png)
4. CRUD : ![Extrait réel des opérations CRUD](captures/04-crud.png)
5. Named Queries : ![Extrait réel des Named Queries](captures/05-named-queries.png)
6. Criteria API : ![Extrait réel des recherches Criteria](captures/06-criteria-api.png)
7. Pagination : ![Extrait réel des pages de produits](captures/07-pagination.png)
8. Héritage JOINED : ![Extrait réel des utilisateurs](captures/08-heritage-joined.png)
9. Commandes : ![Extrait réel des lignes de commande](captures/09-commandes-relations.png)
10. Rapport : ![Extrait réel du rapport et du build](captures/10-rapport-build.png)

## Conclusion

Ce lab m'a permis de pratiquer les relations JPA, les requêtes et les transactions. Les tests H2 vérifient les principales fonctionnalités sans serveur MySQL.
