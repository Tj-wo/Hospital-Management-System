package org.pahappa.main;

import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.dao.UserDao;
import org.pahappa.model.User;
import org.pahappa.utils.Role;

/**
 * This class is responsible for initializing the database with essential data
 * on application startup, such as creating a default admin user if one doesn't exist.
 */
public class DataInitializer {

    public static void initialize() {
        UserDao userDao = new UserDao();
        String adminUsername = "admin@email.com";

        // Check if the specific admin user exists by username
        User existingAdmin = userDao.findByUsername(adminUsername);

        if (existingAdmin == null) {
            System.out.println("++++++++++++++ DEBUG ++++++++++++++");
            System.out.println("Default admin user '" + adminUsername + "' not found. Creating a new one.");

            // Create a new User object for the admin
            User adminUser = new User();
            adminUser.setUsername(adminUsername);

            // Generate and hash the default password
            String plainPassword = "admin123";
            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
            adminUser.setPassword(hashedPassword);

            adminUser.setRole(Role.ADMIN);

            // --- VERIFICATION ---
            System.out.println("Plain password: " + plainPassword);
            System.out.println("Generated HASH: " + hashedPassword);
            System.out.println("Hash length: " + hashedPassword.length());
            System.out.println("Is hash valid for check? " + BCrypt.checkpw(plainPassword, hashedPassword));
            System.out.println("Saving this user object: " + adminUser);
            System.out.println("+++++++++++++++++++++++++++++++++++++");

            // Save the newly created admin user
            try {
                userDao.save(adminUser);
                System.out.println("Default admin user created successfully in the database.");
                System.out.println("------------------------------------");
                System.out.println("Login with:");
                System.out.println("  Username: " + adminUsername);
                System.out.println("  Password: " + plainPassword);
                System.out.println("------------------------------------");
            } catch (Exception e) {
                System.err.println("!!!!!!!!!!!!!! FAILED TO SAVE ADMIN USER !!!!!!!!!!!!!!");
                e.printStackTrace();
                System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }

        } else {
            System.out.println("Default admin user already exists. Skipping creation.");
        }
    }
}