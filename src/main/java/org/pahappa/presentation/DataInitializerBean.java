package org.pahappa.presentation;

import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.dao.UserDao;
import org.pahappa.model.User;
import org.pahappa.utils.Role;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;

/**
 * A CDI bean that runs once when the application starts up.
 * It observes the initialization of the ApplicationScoped context.
 */
@ApplicationScoped
public class DataInitializerBean {

    public void onStartup(@Observes @Initialized(ApplicationScoped.class) Object useless) {
        System.out.println("******************************************");
        System.out.println("***** APPLICATION STARTUP - SEEDING DATA *****");
        System.out.println("******************************************");

        UserDao userDao = new UserDao();
        String adminUsername = "admin@email.com";
        User existingAdmin = userDao.findByUsername(adminUsername);

        if (existingAdmin == null) {
            System.out.println("Default admin user not found. Creating a new one.");

            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            String hashedPassword = BCrypt.hashpw("admin123", BCrypt.gensalt());
            adminUser.setPassword(hashedPassword);
            adminUser.setRole(Role.ADMIN);

            userDao.save(adminUser);
            System.out.println("Default admin created: admin@email.com / admin123");
        } else {
            System.out.println("Default admin user already exists.");
        }
    }
}