<<<<<<< Updated upstream
=======
package org.pahappa.com.service;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration; // Changed from Configuration

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            return new AnnotationConfiguration().configure().buildSessionFactory(); // Use AnnotationConfiguration
        } catch (Exception ex) {
            System.err.println("SessionFactory creation failed: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
<<<<<<< Updated upstream
}
>>>>>>> Stashed changes
=======
}
>>>>>>> Stashed changes
