package org.pahappa.service;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import javax.enterprise.context.ApplicationScoped;

// Utility class to manage Hibernate SessionFactory
@ApplicationScoped
public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    // Build and configure SessionFactory
    private static SessionFactory buildSessionFactory() {
        try {
            // Create SessionFactory from hibernate.cfg.xml
            return new Configuration().configure().buildSessionFactory();
        } catch (Exception ex) {
            System.err.println("Failed to create SessionFactory: " + ex.getMessage());
            throw new ExceptionInInitializerError(ex);
        }
    }

    // Get the SessionFactory
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    // Shutdown Hibernate
    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}