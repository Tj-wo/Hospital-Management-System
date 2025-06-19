package org.pahappa.service;

import org.pahappa.dao.PatientDao;
import org.pahappa.model.Patient;
import org.pahappa.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class PatientService {
    private static final Scanner scanner = new Scanner(System.in);
    private final PatientDao patientDao = new PatientDao();

    // Add a new patient
    public void addPatient(Patient patient) {
        validatePatient(patient);
        patientDao.save(patient);
    }

    // Get a patient by ID
    public Patient getPatient(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
        }
        Patient patient = patientDao.getById(id);
        if (patient == null) {
            throw new IllegalArgumentException("Patient not found with ID: " + id);
        }
        return patient;
    }

    // Get all patients
    public List<Patient> getAllPatients() {
        return patientDao.getAll();
    }

    // Update a patient
    public void updatePatient(Patient patient) {
        if (patient.getId() == null) {
            throw new IllegalArgumentException("Patient ID is required for update");
        }
        validatePatient(patient);
        patientDao.update(patient);
    }

    // Delete a patient by ID
    public void deletePatient(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
        }
        patientDao.delete(id);
    }

    public void viewPatients() {
        System.out.println("\n===== PATIENT LIST =====");
        List<Patient> patients = getAllPatients();
        if (patients.isEmpty()) {
            System.out.println("No patients found.");
        } else {
            System.out.println("ID  | Name             | Email                     | Date of Birth");
            System.out.println("----|------------------|---------------------------|--------------");
            patients.forEach(p -> System.out.printf(
                    "%-3d | %-16s | %-25s | %s%n",
                    p.getId(),
                    truncate(p.getFullName(), 16),
                    truncate(p.getEmail(), 25),
                    formatDate(p.getDateOfBirth())));
        }
    }

    public void addPatientInteractive() {
        try {
            System.out.println("\n===== ADD NEW PATIENT =====");

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

            Patient patient = new Patient();
            patient.setFirstName(firstName);
            patient.setLastName(lastName);
            patient.setEmail(email);
            patient.setDateOfBirth(dateOfBirth);

            addPatient(patient);
            System.out.println("\nPatient added successfully!");
            System.out.println("New Patient ID: " + patient.getId());
        } catch (Exception e) {
            System.out.println("\nError adding patient: " + e.getMessage());
        }
    }

    public void updatePatientInteractive() {
        try {
            System.out.println("\n===== UPDATE PATIENT =====");

            // Show all patients
            viewPatients();

            Long id = getLongInput("\nEnter Patient ID to update: ");
            Patient patient = getPatient(id);

            // Show current details
            System.out.println("\n--- Current Patient Details ---");
            System.out.println("1. First Name: " + patient.getFirstName());
            System.out.println("2. Last Name: " + patient.getLastName());
            System.out.println("3. Email: " + patient.getEmail());
            System.out.println("4. Date of Birth: " + formatDate(patient.getDateOfBirth()));

            // Get updates
            System.out.println("\n--- Update Fields (press Enter to keep current value) ---");
            String firstName = getInputWithDefault("New First Name [" + patient.getFirstName() + "]: ", patient.getFirstName());
            String lastName = getInputWithDefault("New Last Name [" + patient.getLastName() + "]: ", patient.getLastName());
            String email = getInputWithDefault("New Email [" + patient.getEmail() + "]: ", patient.getEmail());
            String dobStr = getInputWithDefault(
                    String.format("New Date of Birth (%s) [%s]: ",
                            Constants.DATE_FORMAT, formatDate(patient.getDateOfBirth())),
                    formatDate(patient.getDateOfBirth()));

            // Apply updates
            if (!firstName.isEmpty()) patient.setFirstName(firstName);
            if (!lastName.isEmpty()) patient.setLastName(lastName);
            if (!email.isEmpty()) patient.setEmail(email);
            if (!dobStr.isEmpty()) {
                Date dob = parseDate(dobStr);
                if (dob != null) patient.setDateOfBirth(dob);
            }

            updatePatient(patient);
            System.out.println("\nPatient updated successfully!");
        } catch (Exception e) {
            System.out.println("\nError updating patient: " + e.getMessage());
        }
    }

    public void deletePatientInteractive() {
        try {
            System.out.println("\n===== DELETE PATIENT =====");

            // Show all patients
            viewPatients();

            Long id = getLongInput("\nEnter Patient ID to delete: ");

            // Confirm deletion
            System.out.print("\nAre you sure you want to delete this patient? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                deletePatient(id);
                System.out.println("\nPatient deleted successfully!");
            } else {
                System.out.println("\nDeletion cancelled.");
            }
        } catch (Exception e) {
            System.out.println("\nError deleting patient: " + e.getMessage());
        }
    }

    // Validate patient data
    private void validatePatient(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null");
        }
        if (patient.getFirstName() == null || patient.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (First Name)");
        }
        if (patient.getLastName() == null || patient.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Last Name)");
        }
        if (patient.getEmail() == null || patient.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Email)");
        }
        if (patient.getDateOfBirth() == null) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Date of Birth)");
        }

        if (patient.getFirstName().length() > Constants.MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("First name cannot exceed " + Constants.MAX_NAME_LENGTH + " characters");
        }
        if (patient.getLastName().length() > Constants.MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Last name cannot exceed " + Constants.MAX_NAME_LENGTH + " characters");
        }
        if (!isValidEmail(patient.getEmail())) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_EMAIL);
        }
        if (patient.getDateOfBirth().after(new Date())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
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

    // Helper method to format dates consistently
    private String formatDate(Date date) {
        if (date == null) return "N/A";
        return new SimpleDateFormat(Constants.DATE_FORMAT).format(date);
    }

    // Helper method to truncate long strings for display
    private String truncate(String str, int length) {
        if (str == null) return "";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
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

    // Get input with default value (new method)
    private String getInputWithDefault(String prompt, String defaultValue) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
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