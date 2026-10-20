package org.example.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import javax.persistence.EntityManager;
import java.util.function.Function;

/** Unique Hibernate configuration shared by DAO, services and the application. */
public final class HibernateUtil {
    private static SessionFactory sessionFactory;

    private HibernateUtil() { }

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            String db = System.getenv().getOrDefault("APP_DB", "h2");
            try {
                Configuration config = new Configuration().configure("hibernate.cfg.xml");
                if ("h2".equalsIgnoreCase(db)) {
                    config.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
                    config.setProperty("hibernate.connection.url", "jdbc:h2:mem:gestion_commerciale;DB_CLOSE_DELAY=-1");
                    config.setProperty("hibernate.connection.username", "sa");
                    config.setProperty("hibernate.connection.password", "");
                    config.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
                    config.setProperty("hibernate.hbm2ddl.auto", "create-drop");
                } else if ("mysql".equalsIgnoreCase(db)) {
                    config.setProperty("hibernate.connection.username", requiredEnv("DB_USER"));
                    config.setProperty("hibernate.connection.password", requiredEnv("DB_PASSWORD"));
                } else {
                    throw new IllegalArgumentException("APP_DB doit etre h2 ou mysql : " + db);
                }
                sessionFactory = config.buildSessionFactory();
            } catch (RuntimeException e) {
                throw new IllegalStateException("Initialisation Hibernate impossible (APP_DB=" + db + ")", e);
            }
        }
        return sessionFactory;
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalStateException("Variable requise : " + name);
        return value;
    }

    public static <R> R withEntityManager(Function<EntityManager, R> work) {
        EntityManager em = getSessionFactory().createEntityManager();
        try { return work.apply(em); }
        finally { em.close(); }
    }

    public static synchronized void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
}
