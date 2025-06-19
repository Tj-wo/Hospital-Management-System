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

// Service class to manage staff operations (add, update, delete, view)
public class StaffService {
    private static final Scanner scanner = new Scanner(System.in);
    private final StaffDao staffDao = new StaffDao();

    // Add a new staff member
    public void addStaff(Staff staff) {
        validateStaff(staff);
        staffDao.save(staff);
    }

    // Get a staff member by ID
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

    // Get all staff members
    public List<Staff> getAllStaff() {
        return staffDao.getAll();
    }

    // Update a staff member
    public void updateStaff(Staff staff) {
        if (staff.getId() == null) {
            throw new IllegalArgumentException("Staff ID is required for update");
        }
        validateStaff(staff);
        staffDao.update(staff);
    }

    // Delete a staff member by ID
    public void deleteStaff(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
        }
        staffDao.delete(id);
    }

    // Interactive method to add a staff member via console
    public void addStaffInteractive() {
        try {
            String firstName = getRequiredInput("Enter First Name: ", Constants.ERROR_REQUIRED_FIELD);
            if (firstName.length() > Constants.MAX_NAME_LENGTH) {
                System.out.println("First name cannot exceed " + Constants.MAX_NAME_LENGTH + " characters");
                return;
            }

            String lastName = getRequiredInput("Enter Last Name: ", Constants.ERROR_REQUIRED_FIELD);
            if (lastName.length() > Constants.MAX_NAME_LENGTH) {
                System.out.println("Last name cannot exceed " + Constants.MAX_NAME_LENGTH + " characters");
                return;
            }

            String email = getRequiredInput("Enter Email: ", Constants.ERROR_REQUIRED_FIELD);
            if (!isValidEmail(email)) {
                System.out.println(Constants.ERROR_INVALID_EMAIL);
                return;
            }

            String dateOfBirthStr = getRequiredInput("Enter Date of Birth (" + Constants.DATE_FORMAT + "): ", Constants.ERROR_REQUIRED_FIELD);
            Date dateOfBirth = parseDate(dateOfBirthStr);
            if (dateOfBirth == null) {
                System.out.println(Constants.ERROR_INVALID_DATE);
                return;
            }
            if (dateOfBirth.after(new Date())) {
                System.out.println("Date of birth cannot be in the future");
                return;
            }

            System.out.println("Available Roles: " + List.of(Role.values()));
            String roleStr = getRequiredInput("Enter Role (DOCTOR/NURSE/ADMIN): ", Constants.ERROR_REQUIRED_FIELD).toUpperCase();
            Role role;
            try {
                role = Role.valueOf(roleStr);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid role! Must be DOCTOR, NURSE, or ADMIN");
                return;
            }

            String specialty = null;
            if (role == Role.DOCTOR) {
                specialty = getRequiredInput("Enter Specialty: ", Constants.ERROR_REQUIRED_FIELD);
                if (specialty.length() > Constants.MAX_SPECIALTY_LENGTH) {
                    System.out.println("Specialty cannot exceed " + Constants.MAX_SPECIALTY_LENGTH + " characters");
                    return;
                }
            }

            // Updated constructor call to match Staff.java
            Staff staff = new Staff();
            staff.setFirstName(firstName);
            staff.setLastName(lastName);
            staff.setEmail(email);
            staff.setDateOfBirth(dateOfBirth);
            staff.setRole(role);
            staff.setSpecialty(specialty);
            addStaff(staff);
            System.out.println("Staff added successfully!");
        } catch (Exception e) {
            System.out.println("Error adding staff: " + e.getMessage());
        }
    }

    // Interactive method to update a staff member
    public void updateStaffInteractive() {
        try {
            Long id = getLongInput("Enter Staff ID to update: ");
            Staff staff = getStaff(id);
            System.out.println("Current details:\n" + staff);

            String firstName = getRequiredInput("Enter new First Name [" + staff.getFirstName() + "]: ", staff.getFirstName());
            if (firstName.length() > Constants.MAX_NAME_LENGTH) {
                System.out.println("First name cannot exceed " + Constants.MAX_NAME_LENGTH + " characters");
                return;
            }

            String lastName = getRequiredInput("Enter new Last Name [" + staff.getLastName() + "]: ", staff.getLastName());
            if (lastName.length() > Constants.MAX_NAME_LENGTH) {
                System.out.println("Last name cannot exceed " + Constants.MAX_NAME_LENGTH + " characters");
                return;
            }

            String email = getRequiredInput("Enter new Email [" + staff.getEmail() + "]: ", staff.getEmail());
            if (!isValidEmail(email)) {
                System.out.println(Constants.ERROR_INVALID_EMAIL);
                return;
            }

            String dateOfBirthStr = getRequiredInput("Enter new Date of Birth (" + Constants.DATE_FORMAT + ") [" + new SimpleDateFormat(Constants.DATE_FORMAT).format(staff.getDateOfBirth()) + "]: ", new SimpleDateFormat(Constants.DATE_FORMAT).format(staff.getDateOfBirth()));
            Date dateOfBirth = parseDate(dateOfBirthStr);
            if (dateOfBirth == null) {
                System.out.println(Constants.ERROR_INVALID_DATE);
                return;
            }
            if (dateOfBirth.after(new Date())) {
                System.out.println("Date of birth cannot be in the future");
                return;
            }

            System.out.println("Available Roles: " + List.of(Role.values()));
            String roleStr = getRequiredInput("Enter new Role (DOCTOR/NURSE/ADMIN) [" + staff.getRole() + "]: ", staff.getRole().toString()).toUpperCase();
            Role role;
            try {
                role = Role.valueOf(roleStr);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid role! Must be DOCTOR, NURSE, or ADMIN");
                return;
            }

            String specialty = staff.getSpecialty();
            if (role == Role.DOCTOR) {
                specialty = getRequiredInput("Enter new Specialty [" + (staff.getSpecialty() != null ? staff.getSpecialty() : "N/A") + "]: ", staff.getSpecialty() != null ? staff.getSpecialty() : "");
                if (specialty.length() > Constants.MAX_SPECIALTY_LENGTH) {
                    System.out.println("Specialty cannot exceed " + Constants.MAX_SPECIALTY_LENGTH + " characters");
                    return;
                }
            } else {
                specialty = null;
            }

            staff.setFirstName(firstName);
            staff.setLastName(lastName);
            staff.setEmail(email);
            staff.setDateOfBirth(dateOfBirth);
            staff.setRole(role);
            staff.setSpecialty(specialty);
            updateStaff(staff);
            System.out.println("Staff updated successfully!");
        } catch (Exception e) {
            System.out.println("Error updating staff: " + e.getMessage());
        }
    }

    // Interactive method to delete a staff member
    public void deleteStaffInteractive() {
        try {
            Long id = getLongInput("Enter Staff ID to delete: ");
            Staff staff = getStaff(id);
            System.out.println("Staff to delete:\n" + staff);
            System.out.print("Are you sure? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                deleteStaff(id);
                System.out.println("Staff deleted successfully!");
            }
        } catch (Exception e) {
            System.out.println("Error deleting staff: " + e.getMessage());
        }
    }

    // Interactive method to view all staff
    public void viewStaff() {
        List<Staff> staffList = getAllStaff();
        System.out.println("\n===== STAFF LIST =====");
        if (staffList.isEmpty()) {
            System.out.println("No staff found.");
        } else {
            staffList.forEach(System.out::println);
        }
    }

    // Validate staff data
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

    // Validate email format
    private boolean isValidEmail(String email) {
        String emailRegex = Constants.EMAIL_REGEX;
        return Pattern.matches(emailRegex, email);
    }

    // Parse date string
    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
            sdf.setLenient(false);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    // Get required input with error message
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

    // Get a valid Long input
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