package org.pahappa.service;

import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.dao.StaffDao;
import org.pahappa.dao.UserDao;
import org.pahappa.model.Staff;
import org.pahappa.model.User;
import org.pahappa.session.SessionManager;
import org.pahappa.utils.Constants;
import org.pahappa.utils.Role;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class StaffService {
    private static final Scanner scanner = new Scanner(System.in);
    private final StaffDao staffDao = new StaffDao();
    private final UserDao userDao = new UserDao();

    /**
     * Adds a new staff member and creates a corresponding user account.
     * The staff member's email is used as their username.
     * This action is restricted to ADMIN users.
     */
    public void addStaff(Staff staff, String password) {
        if (SessionManager.getCurrentUser() == null || SessionManager.getCurrentUser().getRole() != Role.ADMIN) {
            throw new SecurityException("Access Denied: Only ADMINs can add new staff.");
        }

        validateStaff(staff);

        String username = staff.getEmail(); // Use email as the username
        if (userDao.findByUsername(username) != null) {
            throw new IllegalArgumentException("An account with the email '" + username + "' already exists.");
        }

        // Hash the password for the new user account
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Create the user account
        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setRole(staff.getRole()); // The user's role is determined by the staff's role
        user.setStaff(staff);
        staff.setUser(user); // Set the link from staff to user

        // Save the staff entity, which cascades to the user entity
        staffDao.save(staff);
    }

    /**
     * Interactive flow for an ADMIN to add a new staff member.
     */
    public void addStaffInteractive() {
        if (SessionManager.getCurrentUser().getRole() != Role.ADMIN) {
            System.out.println("Access Denied: Only Admins can add staff.");
            return;
        }

        try {
            System.out.println("\n===== ADD NEW STAFF MEMBER =====");

            String firstName = getRequiredInput("First Name: ", Constants.ERROR_REQUIRED_FIELD);
            String lastName = getRequiredInput("Last Name: ", Constants.ERROR_REQUIRED_FIELD);
            String email = getRequiredInput("Email: ", Constants.ERROR_REQUIRED_FIELD);
            String dateOfBirthStr = getRequiredInput(
                    String.format("Date of Birth (%s): ", Constants.DATE_FORMAT),
                    Constants.ERROR_REQUIRED_FIELD);

            System.out.println("\nAvailable Roles: DOCTOR, NURSE, ADMIN, RECEPTIONIST");
            String roleStr = getRequiredInput("Role: ", Constants.ERROR_REQUIRED_FIELD).toUpperCase();
            Role role = Role.valueOf(roleStr);

            String specialty = null;
            if (role == Role.DOCTOR) {
                specialty = getRequiredInput("Specialty: ", Constants.ERROR_REQUIRED_FIELD);
            }

            System.out.println("\n--- Create User Account for Staff ---");
            System.out.println("The username will be the staff member's email: " + email);
            String password = getRequiredInput("Enter temporary password: ", Constants.ERROR_REQUIRED_FIELD);

            Staff staff = new Staff();
            staff.setFirstName(firstName);
            staff.setLastName(lastName);
            staff.setEmail(email);
            staff.setDateOfBirth(parseDate(dateOfBirthStr));
            staff.setRole(role);
            staff.setSpecialty(specialty);

            addStaff(staff, password);
            System.out.println("\nStaff member and user account added successfully!");

        } catch (Exception e) {
            System.err.println("\nError adding staff: " + e.getMessage());
        }
    }

    public Staff getStaff(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
        Staff staff = staffDao.getById(id);
        if (staff == null) throw new IllegalArgumentException("Staff not found with ID: " + id);
        return staff;
    }

    public List<Staff> getAllStaff() {
        return staffDao.getAll();
    }

    public List<Staff> getStaffByRole(Role role) {
        return staffDao.getAll().stream().filter(s -> s.getRole() == role).toList();
    }

    public void updateStaff(Staff staff) {
        if (SessionManager.getCurrentUser().getRole() != Role.ADMIN) {
            throw new SecurityException("Access Denied: Only ADMINs can update staff.");
        }
        if (staff.getId() == null) throw new IllegalArgumentException("Staff ID is required for update");
        validateStaff(staff);
        staffDao.update(staff);
    }

    public void deleteStaff(Long id) {
        if (SessionManager.getCurrentUser().getRole() != Role.ADMIN) {
            throw new SecurityException("Access Denied: Only ADMINs can delete staff.");
        }
        if (id == null || id <= 0) throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
        // Important: In a real system, add logic to re-assign appointments/admissions before deleting.
        staffDao.delete(id);
    }

    public void viewStaff() {
        System.out.println("\n===== STAFF LIST =====");
        List<Staff> staffList = getAllStaff();
        if (staffList.isEmpty()) {
            System.out.println("No staff members found.");
            return;
        }
        System.out.println("ID  | Name             | Role         | Specialty        | Email");
        System.out.println("----|------------------|--------------|------------------|-------------------");
        staffList.forEach(s -> System.out.printf(
                "%-3d | %-16s | %-12s | %-16s | %s%n",
                s.getId(),
                truncate(s.getFullName(), 16),
                s.getRole(),
                truncate(s.getSpecialty() != null ? s.getSpecialty() : "N/A", 16),
                truncate(s.getEmail(), 20)
        ));
    }

    public void updateStaffInteractive() {
        if (SessionManager.getCurrentUser().getRole() != Role.ADMIN) {
            System.out.println("Access Denied: Only Admins can update staff records.");
            return;
        }
        try {
            viewStaff();
            Long id = getLongInput("\nEnter Staff ID to update: ");
            Staff staff = getStaff(id);

            System.out.println("\n--- Update Fields (press Enter to keep current value) ---");
            String firstName = getInputWithDefault("New First Name [" + staff.getFirstName() + "]: ", staff.getFirstName());
            String lastName = getInputWithDefault("New Last Name [" + staff.getLastName() + "]: ", staff.getLastName());

            // Note: Email/username change is complex and often disallowed. We will skip it here.
            // Role and specialty changes also have cascading effects and are omitted for simplicity.
            System.out.println("Email (username), Role, and Specialty cannot be changed here.");

            staff.setFirstName(firstName);
            staff.setLastName(lastName);

            updateStaff(staff);
            System.out.println("\nStaff updated successfully!");
        } catch (Exception e) {
            System.err.println("\nError updating staff: " + e.getMessage());
        }
    }

    public void deleteStaffInteractive() {
        if (SessionManager.getCurrentUser().getRole() != Role.ADMIN) {
            System.out.println("Access Denied: Only Admins can delete staff records.");
            return;
        }
        try {
            viewStaff();
            Long id = getLongInput("\nEnter Staff ID to delete: ");
            System.out.print("\nWARNING: This will delete the staff member and their user account. This is irreversible.\nAre you sure? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                deleteStaff(id);
                System.out.println("\nStaff member deleted successfully!");
            } else {
                System.out.println("\nDeletion cancelled.");
            }
        } catch (Exception e) {
            System.err.println("\nError deleting staff: " + e.getMessage());
        }
    }

    // --- Helper and Validation Methods ---
    private void validateStaff(Staff staff) {
        if (staff == null) throw new IllegalArgumentException("Staff cannot be null");
        if (staff.getFirstName() == null || staff.getFirstName().trim().isEmpty()) throw new IllegalArgumentException("First Name is required");
        if (staff.getLastName() == null || staff.getLastName().trim().isEmpty()) throw new IllegalArgumentException("Last Name is required");
        if (staff.getEmail() == null || !isValidEmail(staff.getEmail())) throw new IllegalArgumentException("A valid Email is required");
        if (staff.getDateOfBirth() == null) throw new IllegalArgumentException("Date of Birth is required");
        if (staff.getRole() == null) throw new IllegalArgumentException("Role is required");
        if (staff.getRole() == Role.DOCTOR && (staff.getSpecialty() == null || staff.getSpecialty().trim().isEmpty())) {
            throw new IllegalArgumentException("Specialty is required for doctors.");
        }
    }

    private boolean isValidEmail(String email) {
        return Pattern.matches(Constants.EMAIL_REGEX, email);
    }

    private Date parseDate(String dateStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
        sdf.setLenient(false);
        return sdf.parse(dateStr);
    }

    private String truncate(String str, int length) {
        if (str == null) return "";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }

    private String getRequiredInput(String prompt, String errorMessage) {
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) System.out.println(errorMessage);
        } while (input.isEmpty());
        return input;
    }

    private String getInputWithDefault(String prompt, String defaultValue) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }

    private Long getLongInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid ID. Please enter a number.");
            }
        }
    }
}