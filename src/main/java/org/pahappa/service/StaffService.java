package org.pahappa.service;

import org.pahappa.dao.StaffDao;
import org.pahappa.model.Staff;
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

    public void addStaff(Staff staff) {
        validateStaff(staff);
        staffDao.save(staff);
    }

    public Staff getStaff(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
        }
        Staff staff = staffDao.getById(id);
        if (staff == null) {
            throw new IllegalArgumentException("Staff not found with ID: " + id);
        }
        return staff;
    }

    public List<Staff> getAllStaff() {
        return staffDao.getAll();
    }

    public void updateStaff(Staff staff) {
        if (staff.getId() == null) {
            throw new IllegalArgumentException("Staff ID is required for update");
        }
        validateStaff(staff);
        staffDao.update(staff);
    }

    public void deleteStaff(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
        }
        staffDao.delete(id);
    }

    public void viewStaff() {
        System.out.println("\n===== STAFF LIST =====");
        List<Staff> staffList = getAllStaff();
        if (staffList.isEmpty()) {
            System.out.println("No staff members found.");
            return;
        }

        System.out.println("ID  | Name             | Role    | Specialty        | Email");
        System.out.println("----|------------------|---------|------------------|-------------------");
        staffList.forEach(s -> System.out.printf(
                "%-3d | %-16s | %-7s | %-16s | %s%n",
                s.getId(),
                truncate(s.getFullName(), 16),
                s.getRole(),
                truncate(s.getSpecialty() != null ? s.getSpecialty() : "N/A", 16),
                truncate(s.getEmail(), 20)
        ));
    }

    public void addStaffInteractive() {
        try {
            System.out.println("\n===== ADD NEW STAFF MEMBER =====");

            // Get basic info
            String firstName = getRequiredInput("First Name: ", Constants.ERROR_REQUIRED_FIELD);
            if (firstName.length() > Constants.MAX_NAME_LENGTH) {
                System.out.println("First name cannot exceed " + Constants.MAX_NAME_LENGTH + " characters");
                return;
            }

            String lastName = getRequiredInput("Last Name: ", Constants.ERROR_REQUIRED_FIELD);
            if (lastName.length() > Constants.MAX_NAME_LENGTH) {
                System.out.println("Last name cannot exceed " + Constants.MAX_NAME_LENGTH + " characters");
                return;
            }

            String email = getRequiredInput("Email: ", Constants.ERROR_REQUIRED_FIELD);
            if (!isValidEmail(email)) {
                System.out.println(Constants.ERROR_INVALID_EMAIL);
                return;
            }

            // Get date of birth
            String dateOfBirthStr = getRequiredInput(
                    String.format("Date of Birth (%s): ", Constants.DATE_FORMAT),
                    Constants.ERROR_REQUIRED_FIELD);
            Date dateOfBirth = parseDate(dateOfBirthStr);
            if (dateOfBirth == null) {
                System.out.println(Constants.ERROR_INVALID_DATE);
                return;
            }
            if (dateOfBirth.after(new Date())) {
                System.out.println("Date of birth cannot be in the future");
                return;
            }

            // Get role
            System.out.println("\nAvailable Roles: " + List.of(Role.values()));
            String roleStr = getRequiredInput("Role (DOCTOR/NURSE/ADMIN): ", Constants.ERROR_REQUIRED_FIELD).toUpperCase();
            Role role;
            try {
                role = Role.valueOf(roleStr);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid role! Must be DOCTOR, NURSE, or ADMIN");
                return;
            }

            // Get specialty if doctor
            String specialty = null;
            if (role == Role.DOCTOR) {
                specialty = getRequiredInput("Specialty: ", Constants.ERROR_REQUIRED_FIELD);
                if (specialty.length() > Constants.MAX_SPECIALTY_LENGTH) {
                    System.out.println("Specialty cannot exceed " + Constants.MAX_SPECIALTY_LENGTH + " characters");
                    return;
                }
            }

            // Create and save staff
            Staff staff = new Staff();
            staff.setFirstName(firstName);
            staff.setLastName(lastName);
            staff.setEmail(email);
            staff.setDateOfBirth(dateOfBirth);
            staff.setRole(role);
            staff.setSpecialty(specialty);

            addStaff(staff);
            System.out.println("\nStaff member added successfully!");
            System.out.println("New Staff ID: " + staff.getId());
        } catch (Exception e) {
            System.out.println("\nError adding staff: " + e.getMessage());
        }
    }

    public void updateStaffInteractive() {
        try {
            System.out.println("\n===== UPDATE STAFF MEMBER =====");

            // Show all staff first
            viewStaff();

            // Get staff ID to update
            Long id = getLongInput("\nEnter Staff ID to update: ");
            Staff staff = getStaff(id);

            // Show current details
            System.out.println("\n--- Current Staff Details ---");
            System.out.println("1. First Name: " + staff.getFirstName());
            System.out.println("2. Last Name: " + staff.getLastName());
            System.out.println("3. Email: " + staff.getEmail());
            System.out.println("4. Date of Birth: " + formatDate(staff.getDateOfBirth()));
            System.out.println("5. Role: " + staff.getRole());
            System.out.println("6. Specialty: " + (staff.getSpecialty() != null ? staff.getSpecialty() : "N/A"));

            // Get updates
            System.out.println("\n--- Update Fields (press Enter to skip) ---");
            String firstName = getInputWithDefault("New First Name [" + staff.getFirstName() + "]: ", staff.getFirstName());
            String lastName = getInputWithDefault("New Last Name [" + staff.getLastName() + "]: ", staff.getLastName());
            String email = getInputWithDefault("New Email [" + staff.getEmail() + "]: ", staff.getEmail());
            String dobStr = getInputWithDefault(
                    String.format("New Date of Birth (%s) [%s]: ",
                            Constants.DATE_FORMAT, formatDate(staff.getDateOfBirth())),
                    formatDate(staff.getDateOfBirth()));

            // Role update
            System.out.println("\nAvailable Roles: " + List.of(Role.values()));
            String roleStr = getInputWithDefault(
                    "New Role (DOCTOR/NURSE/ADMIN) [" + staff.getRole() + "]: ",
                    staff.getRole().toString()).toUpperCase();

            // Specialty update
            String specialty = staff.getSpecialty();
            if (roleStr.equalsIgnoreCase("DOCTOR") ||
                    (!roleStr.isEmpty() && Role.valueOf(roleStr) == Role.DOCTOR)) {
                specialty = getInputWithDefault(
                        "New Specialty [" + (staff.getSpecialty() != null ? staff.getSpecialty() : "N/A") + "]: ",
                        staff.getSpecialty() != null ? staff.getSpecialty() : "");
            }

            // Apply updates
            if (!firstName.isEmpty()) staff.setFirstName(firstName);
            if (!lastName.isEmpty()) staff.setLastName(lastName);
            if (!email.isEmpty()) staff.setEmail(email);
            if (!dobStr.isEmpty()) {
                Date dob = parseDate(dobStr);
                if (dob != null) staff.setDateOfBirth(dob);
            }
            if (!roleStr.isEmpty()) {
                try {
                    staff.setRole(Role.valueOf(roleStr));
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid role! Changes not saved.");
                    return;
                }
            }
            if (specialty != null && !specialty.isEmpty()) {
                staff.setSpecialty(specialty);
            }

            updateStaff(staff);
            System.out.println("\nStaff member updated successfully!");
        } catch (Exception e) {
            System.out.println("\nError updating staff: " + e.getMessage());
        }
    }

    public void deleteStaffInteractive() {
        try {
            System.out.println("\n===== DELETE STAFF MEMBER =====");

            // Show all staff first
            viewStaff();

            // Get staff ID to delete
            Long id = getLongInput("\nEnter Staff ID to delete: ");

            // Confirm deletion
            System.out.print("\nAre you sure you want to delete this staff member? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                deleteStaff(id);
                System.out.println("\nStaff member deleted successfully!");
            } else {
                System.out.println("\nDeletion cancelled.");
            }
        } catch (Exception e) {
            System.out.println("\nError deleting staff: " + e.getMessage());
        }
    }

    private void validateStaff(Staff staff) {
        if (staff == null) {
            throw new IllegalArgumentException("Staff cannot be null");
        }
        if (staff.getFirstName() == null || staff.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (First Name)");
        }
        if (staff.getLastName() == null || staff.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Last Name)");
        }
        if (staff.getEmail() == null || staff.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Email)");
        }
        if (staff.getDateOfBirth() == null) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Date of Birth)");
        }
        if (staff.getRole() == null) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Role)");
        }

        if (staff.getFirstName().length() > Constants.MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("First name cannot exceed " + Constants.MAX_NAME_LENGTH + " characters");
        }
        if (staff.getLastName().length() > Constants.MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Last name cannot exceed " + Constants.MAX_NAME_LENGTH + " characters");
        }
        if (!isValidEmail(staff.getEmail())) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_EMAIL);
        }
        if (staff.getDateOfBirth().after(new Date())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }
        if (staff.getRole() == Role.DOCTOR && (staff.getSpecialty() == null || staff.getSpecialty().trim().isEmpty())) {
            throw new IllegalArgumentException("Specialty is required for doctors");
        }
        if (staff.getRole() != Role.DOCTOR && staff.getSpecialty() != null) {
            throw new IllegalArgumentException("Specialty should only be set for doctors");
        }
        if (staff.getSpecialty() != null && staff.getSpecialty().length() > Constants.MAX_SPECIALTY_LENGTH) {
            throw new IllegalArgumentException("Specialty cannot exceed " + Constants.MAX_SPECIALTY_LENGTH + " characters");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = Constants.EMAIL_REGEX;
        return Pattern.matches(emailRegex, email);
    }

    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
            sdf.setLenient(false);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    private String formatDate(Date date) {
        if (date == null) return "N/A";
        return new SimpleDateFormat(Constants.DATE_FORMAT).format(date);
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
            if (input.isEmpty()) {
                System.out.println(errorMessage);
            }
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
                System.out.println("Invalid ID format! Please enter a number.");
            }
        }
    }
}