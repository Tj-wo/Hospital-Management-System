package org.pahappa.service;

import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.dao.UserDao;
import org.pahappa.model.Patient;
import org.pahappa.model.User;
import org.pahappa.session.SessionManager;
import org.pahappa.utils.Constants;
import org.pahappa.utils.Role;

import java.util.Scanner;

public class UserService {
    private static final Scanner scanner = new Scanner(System.in);
    private final UserDao userDao = new UserDao();
    private final PatientService patientService = new PatientService();

    /**
     * Registers a new patient with a user account.
     * Hashes the password before saving.
     */
    public void registerPatient(Patient patient, String password) {
        String username = patient.getEmail(); // Use email as username

        // Validate that the username (email) is not already taken
        if (userDao.findByUsername(username) != null) {
            throw new IllegalArgumentException("An account with the email '" + username + "' already exists.");
        }

        // Hash the password for security
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Create a new User entity
        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setRole(Role.PATIENT);
        user.setPatient(patient);
        patient.setUser(user); // Establish the bidirectional link

        // Save the patient, which will cascade and save the user as well
        patientService.addPatient(patient);
    }

    /**
     * Authenticates a user by username and password.
     * Returns the User object on success, null on failure.
     */
    public User login(String username, String password) {
        User user = userDao.findByUsername(username);
        // Check if user exists and if the provided password matches the hashed one
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            return user;
        }
        return null; // Return null if authentication fails
    }

    /**
     * Logs out the current user by clearing the session.
     */
    public void logout() {
        if (SessionManager.getCurrentUser() != null) {
            System.out.println("\nLogging out " + SessionManager.getCurrentUser().getUsername() + "...");
            SessionManager.logout();
        }
    }

    /**
     * Interactive flow for user login.
     */
    public void loginInteractive() {
        System.out.println("\n===== LOGIN =====");
        System.out.print("Enter username (your email): ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        User user = login(username, password);
        if (user != null) {
            SessionManager.setCurrentUser(user);
            System.out.println("Login successful!");
        } else {
            System.out.println("Login failed. Invalid username or password.");
        }
    }

    /**
     * Interactive flow for patient registration.
     */
    public void registerPatientInteractive() {
        try {
            System.out.println("\n===== PATIENT REGISTRATION =====");

            // Use PatientService to gather patient details first
            Patient patient = patientService.gatherPatientDetails();
            if (patient == null) {
                System.out.println("Registration failed due to invalid patient data.");
                return; // Exit if patient details were invalid
            }

            // The username will be the email. Just ask for the password.
            System.out.println("\n--- Create Your Account ---");
            System.out.println("Your username will be your email: " + patient.getEmail());
            String password = getRequiredInput("Choose a password: ", Constants.ERROR_REQUIRED_FIELD);

            // Finalize registration
            registerPatient(patient, password);
            System.out.println("\nRegistration successful! You can now log in with your email and password.");

        } catch (Exception e) {
            System.err.println("\nError during registration: " + e.getMessage());
        }
    }

    // Helper method for getting required user input
    private String getRequiredInput(String prompt, String errorMessage) {
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println(errorMessage);
            }
        } while (input.isEmpty());
        return input;
    }
}