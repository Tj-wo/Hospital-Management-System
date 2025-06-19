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

// Service class to manage patient operations (add, update, delete, view)
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

    // Interactive method to add a patient
    public void addPatientInteractive() {
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

            Patient patient = new Patient();
            patient.setFirstName(firstName);
            patient.setLastName(lastName);
            patient.setEmail(email);
            patient.setDateOfBirth(dateOfBirth);
            addPatient(patient);
            System.out.println("Patient added successfully!");
        } catch (Exception e) {
            System.out.println("Error adding patient: " + e.getMessage());
        }
    }

    // Interactive method to update a patient
    public void updatePatientInteractive() {
        try {
            Long id = getLongInput("Enter Patient ID to update: ");
            Patient patient = getPatient(id);
            System.out.println("Current details:\n" + patient);

            String firstName = getRequiredInput("Enter new First Name [" + patient.getFirstName() + "]: ", patient.getFirstName());
            if (firstName.length() > Constants.MAX_NAME_LENGTH) {
                System.out.println("First name cannot exceed " + Constants.MAX_NAME_LENGTH + " characters");
                return;
            }

            String lastName = getRequiredInput("Enter new Last Name [" + patient.getLastName() + "]: ", patient.getLastName());
            if (lastName.length() > Constants.MAX_NAME_LENGTH) {
                System.out.println("Last name cannot exceed " + Constants.MAX_NAME_LENGTH + " characters");
                return;
            }

            String email = getRequiredInput("Enter new Email [" + patient.getEmail() + "]: ", patient.getEmail());
            if (!isValidEmail(email)) {
                System.out.println(Constants.ERROR_INVALID_EMAIL);
                return;
            }

            String dateOfBirthStr = getRequiredInput("Enter new Date of Birth (" + Constants.DATE_FORMAT + ") [" + new SimpleDateFormat(Constants.DATE_FORMAT).format(patient.getDateOfBirth()) + "]: ", new SimpleDateFormat(Constants.DATE_FORMAT).format(patient.getDateOfBirth()));
            Date dateOfBirth = parseDate(dateOfBirthStr);
            if (dateOfBirth == null) {
                System.out.println(Constants.ERROR_INVALID_DATE);
                return;
            }
            if (dateOfBirth.after(new Date())) {
                System.out.println("Date of birth cannot be in the future");
                return;
            }

            patient.setFirstName(firstName);
            patient.setLastName(lastName);
            patient.setEmail(email);
            patient.setDateOfBirth(dateOfBirth);
            updatePatient(patient);
            System.out.println("Patient updated successfully!");
        } catch (Exception e) {
            System.out.println("Error updating patient: " + e.getMessage());
        }
    }

    // Interactive method to delete a patient
    public void deletePatientInteractive() {
        try {
            Long id = getLongInput("Enter Patient ID to delete: ");
            Patient patient = getPatient(id);
            System.out.println("Patient to delete:\n" + patient);
            System.out.print("Are you sure? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                deletePatient(id);
                System.out.println("Patient deleted successfully!");
            }
        } catch (Exception e) {
            System.out.println("Error deleting patient: " + e.getMessage());
        }
    }

    // Interactive method to view all patients
    public void viewPatients() {
        List<Patient> patients = getAllPatients();
        System.out.println("\n===== PATIENT LIST =====");
        if (patients.isEmpty()) {
            System.out.println("No patients found.");
        } else {
            patients.forEach(System.out::println);
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