package org.pahappa.service;

import org.pahappa.dao.PatientDao;
import org.pahappa.model.Patient;
import org.pahappa.session.SessionManager;
import org.pahappa.utils.Constants;
import org.pahappa.utils.Role;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class PatientService {
    private static final Scanner scanner = new Scanner(System.in);
    private final PatientDao patientDao = new PatientDao();

    // Internal method called by authorized services (UserService, PatientService)
    public void addPatient(Patient patient) {
        validatePatient(patient);
        patientDao.save(patient);
    }

    /**
     * Reusable interactive method to gather and validate patient details.
     * Returns a transient Patient object or null if validation fails.
     */
    public Patient gatherPatientDetails() {
        try {
            String firstName = getRequiredInput("First Name: ", Constants.ERROR_REQUIRED_FIELD);
            String lastName = getRequiredInput("Last Name: ", Constants.ERROR_REQUIRED_FIELD);
            String email = getRequiredInput("Email: ", Constants.ERROR_REQUIRED_FIELD);
            String dateOfBirthStr = getRequiredInput(
                    String.format("Date of Birth (%s): ", Constants.DATE_FORMAT),
                    Constants.ERROR_REQUIRED_FIELD);

            Patient patient = new Patient();
            patient.setFirstName(firstName);
            patient.setLastName(lastName);
            patient.setEmail(email);

            Date dob = parseDate(dateOfBirthStr);
            if (dob == null || dob.after(new Date())) {
                System.out.println("Invalid date of birth. It cannot be in the future.");
                return null;
            }
            patient.setDateOfBirth(dob);

            validatePatient(patient);
            return patient;
        } catch (Exception e) {
            System.err.println("Error gathering patient details: " + e.getMessage());
            return null;
        }
    }

    /**
     * Interactive flow for an Admin or Receptionist to add a patient record
     * without creating a user account.
     */
    public void addPatientInteractive() {
        Role currentUserRole = SessionManager.getCurrentUser().getRole();
        if (currentUserRole != Role.ADMIN && currentUserRole != Role.RECEPTIONIST) {
            System.out.println("Access Denied: Only Admins or Receptionists can perform this action.");
            return;
        }

        System.out.println("\n===== ADD NEW PATIENT RECORD (by " + currentUserRole + ") =====");
        Patient patient = gatherPatientDetails();
        if (patient != null) {
            patientDao.save(patient); // Directly save the patient record
            System.out.println("\nPatient record added successfully! New Patient ID: " + patient.getId());
            System.out.println("Note: This patient must register separately to create an online account.");
        }
    }

    public Patient getPatient(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
        Patient patient = patientDao.getById(id);
        if (patient == null) throw new IllegalArgumentException("Patient not found with ID: " + id);
        return patient;
    }

    public List<Patient> getAllPatients() {
        return patientDao.getAll();
    }

    public void updatePatient(Patient patient) {
        // Permissions are checked in the interactive method before calling this
        if (patient.getId() == null) throw new IllegalArgumentException("Patient ID is required for update");
        validatePatient(patient);
        patientDao.update(patient);
    }

    public void deletePatient(Long id) {
        // Permissions are checked in the interactive method before calling this
        if (id == null || id <= 0) throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
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

    /**
     * Interactive flow for updating a patient's details.
     * Restricted to Admins and Receptionists.
     */
    public void updatePatientInteractive() {
        Role currentUserRole = SessionManager.getCurrentUser().getRole();
        if (currentUserRole != Role.ADMIN && currentUserRole != Role.RECEPTIONIST) {
            System.out.println("Access Denied.");
            return;
        }
        try {
            viewPatients();
            Long id = getLongInput("\nEnter Patient ID to update: ");
            Patient patient = getPatient(id);

            System.out.println("\n--- Current Patient Details ---");
            System.out.println("1. First Name: " + patient.getFirstName());
            System.out.println("2. Last Name: " + patient.getLastName());
            System.out.println("3. Email: " + patient.getEmail());
            System.out.println("4. Date of Birth: " + formatDate(patient.getDateOfBirth()));

            System.out.println("\n--- Update Fields (press Enter to keep current value) ---");
            String firstName = getInputWithDefault("New First Name [" + patient.getFirstName() + "]: ", patient.getFirstName());
            String lastName = getInputWithDefault("New Last Name [" + patient.getLastName() + "]: ", patient.getLastName());
            String email = getInputWithDefault("New Email [" + patient.getEmail() + "]: ", patient.getEmail());
            String dobStr = getInputWithDefault(
                    String.format("New Date of Birth (%s) [%s]: ", Constants.DATE_FORMAT, formatDate(patient.getDateOfBirth())),
                    formatDate(patient.getDateOfBirth()));

            patient.setFirstName(firstName);
            patient.setLastName(lastName);
            patient.setEmail(email);
            if (!dobStr.equals(formatDate(patient.getDateOfBirth()))) {
                Date newDob = parseDate(dobStr);
                if (newDob != null) patient.setDateOfBirth(newDob);
            }

            updatePatient(patient);
            System.out.println("\nPatient updated successfully!");
        } catch (Exception e) {
            System.err.println("\nError updating patient: " + e.getMessage());
        }
    }

    /**
     * Interactive flow for deleting a patient.
     * Restricted to Admins due to its destructive nature.
     */
    public void deletePatientInteractive() {
        if (SessionManager.getCurrentUser().getRole() != Role.ADMIN) {
            System.out.println("Access Denied: Only Admins can delete patient records.");
            return;
        }
        try {
            viewPatients();
            Long id = getLongInput("\nEnter Patient ID to delete: ");

            System.out.print("\nWARNING: This will permanently delete the patient and all associated records (appointments, etc).\nAre you absolutely sure? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                deletePatient(id);
                System.out.println("\nPatient deleted successfully!");
            } else {
                System.out.println("\nDeletion cancelled.");
            }
        } catch (Exception e) {
            System.err.println("\nError deleting patient: " + e.getMessage());
        }
    }

    // --- Helper and Validation Methods ---
    private void validatePatient(Patient patient) {
        if (patient == null) throw new IllegalArgumentException("Patient cannot be null");
        if (patient.getFirstName() == null || patient.getFirstName().trim().isEmpty()) throw new IllegalArgumentException("First Name is required");
        if (patient.getLastName() == null || patient.getLastName().trim().isEmpty()) throw new IllegalArgumentException("Last Name is required");
        if (patient.getEmail() == null || !isValidEmail(patient.getEmail())) throw new IllegalArgumentException("A valid Email is required");
        if (patient.getDateOfBirth() == null) throw new IllegalArgumentException("Date of Birth is required");
    }
    private boolean isValidEmail(String email) { return Pattern.matches(Constants.EMAIL_REGEX, email); }
    private Date parseDate(String dateStr) {
        try {
            return new SimpleDateFormat(Constants.DATE_FORMAT).parse(dateStr);
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