package org.pahappa.service;

import org.pahappa.dao.AdmissionDao;
import org.pahappa.model.Admission;
import org.pahappa.model.Patient;
import org.pahappa.utils.Constants;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

// Service class to manage admission operations (admit, update, delete, view)
public class AdmissionService {
    private static final Scanner scanner = new Scanner(System.in);
    private final AdmissionDao admissionDao = new AdmissionDao();
    private final PatientService patientService = new PatientService();

    // Admit a new patient
    public void admitPatient(Admission admission) {
        validateAdmission(admission); // Check if admission data is valid
        admissionDao.save(admission); // Save to database
    }

    // Get an admission by ID
    public Admission getAdmission(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
        }
        Admission admission = admissionDao.getById(id);
        if (admission == null) {
            throw new IllegalArgumentException(Constants.ERROR_NOT_FOUND);
        }
        return admission;
    }

    // Get all admissions
    public List<Admission> getAllAdmissions() {
        return admissionDao.getAll();
    }

    // Update an existing admission
    public void updateAdmission(Admission admission) {
        if (admission.getId() == null) {
            throw new IllegalArgumentException("Admission ID is required for update");
        }
        validateAdmission(admission);
        admissionDao.update(admission);
    }

    // Delete an admission by ID
    public void deleteAdmission(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
        }
        admissionDao.delete(id);
    }

    // Interactive method to admit a patient via console
    public void admitPatientInteractive() {
        try {
            // Display available patients
            List<Patient> patients = patientService.getAllPatients();
            if (patients.isEmpty()) {
                System.out.println("No patients available. Add a patient first!");
                return;
            }
            System.out.println("\nAvailable Patients:");
            patients.forEach(p -> System.out.printf("%d: %s%n", p.getId(), p.getFullName()));
            Long patientId = getLongInput("Enter Patient ID: ");
            Patient patient = patientService.getPatient(patientId);

            // Get admission date
            String admissionDateStr = getRequiredInput("Enter Admission Date (" + Constants.DATE_FORMAT + "): ", Constants.ERROR_REQUIRED_FIELD);
            Date admissionDate = parseDate(admissionDateStr);
            if (admissionDate == null) {
                System.out.println(Constants.ERROR_INVALID_DATE);
                return;
            }
            if (admissionDate.after(new Date(System.currentTimeMillis()))) {
                System.out.println(Constants.ERROR_FUTURE_ADMISSION_DATE);
                return;
            }

            // Get discharge date (optional)
            String dischargeDateStr = getOptionalInput("Enter Discharge Date (" + Constants.DATE_FORMAT + ") (optional): ");
            Date dischargeDate = null;
            if (!dischargeDateStr.isEmpty()) {
                dischargeDate = parseDate(dischargeDateStr);
                if (dischargeDate == null) {
                    System.out.println(Constants.ERROR_INVALID_DATE);
                    return;
                }
                if (dischargeDate.before(admissionDate)) {
                    System.out.println(Constants.ERROR_DISCHARGE_BEFORE_ADMISSION);
                    return;
                }
            }

            // Get reason
            String reason = getRequiredInput("Enter Reason: ", Constants.ERROR_REQUIRED_FIELD);
            if (reason.length() > Constants.MAX_REASON_LENGTH) {
                System.out.println(Constants.ERROR_REASON_TOO_LONG);
                return;
            }

            Admission admission = new Admission(patient, admissionDate, dischargeDate, reason);
            admitPatient(admission);
            System.out.println("Patient admitted successfully!");
        } catch (Exception e) {
            System.out.println("Error admitting patient: " + e.getMessage());
        }
    }

    // Interactive method to update an admission
    public void updateAdmissionInteractive() {
        try {
            Long id = getLongInput("Enter Admission ID to update: ");
            Admission admission = getAdmission(id);
            System.out.println("Current details:\n" + admission);

            // Update patient
            List<Patient> patients = patientService.getAllPatients();
            if (patients.isEmpty()) {
                System.out.println("No patients available!");
                return;
            }
            System.out.println("\nAvailable Patients:");
            patients.forEach(p -> System.out.printf("%d: %s%n", p.getId(), p.getFullName()));
            Long patientId = getLongInput("Enter new Patient ID [" + admission.getPatient().getId() + "]: ");
            Patient patient = patientService.getPatient(patientId);

            // Update admission date
            String admissionDateStr = getRequiredInput("Enter new Admission Date (" + Constants.DATE_FORMAT + ") [" + admission.getAdmissionDate() + "]: ", admission.getAdmissionDate().toString());
            Date admissionDate = parseDate(admissionDateStr);
            if (admissionDate == null) {
                System.out.println(Constants.ERROR_INVALID_DATE);
                return;
            }
            if (admissionDate.after(new Date(System.currentTimeMillis()))) {
                System.out.println(Constants.ERROR_FUTURE_ADMISSION_DATE);
                return;
            }

            // Update discharge date
            String dischargeDateStr = getOptionalInput("Enter new Discharge Date (" + Constants.DATE_FORMAT + ") [" + (admission.getDischargeDate() != null ? admission.getDischargeDate() : "N/A") + "]: ");
            Date dischargeDate = null;
            if (!dischargeDateStr.isEmpty()) {
                dischargeDate = parseDate(dischargeDateStr);
                if (dischargeDate == null) {
                    System.out.println(Constants.ERROR_INVALID_DATE);
                    return;
                }
                if (dischargeDate.before(admissionDate)) {
                    System.out.println(Constants.ERROR_DISCHARGE_BEFORE_ADMISSION);
                    return;
                }
            }

            // Update reason
            String reason = getRequiredInput("Enter new Reason [" + admission.getReason() + "]: ", admission.getReason());
            if (reason.length() > Constants.MAX_REASON_LENGTH) {
                System.out.println(Constants.ERROR_REASON_TOO_LONG);
                return;
            }

            admission.setPatient(patient);
            admission.setAdmissionDate(admissionDate);
            admission.setDischargeDate(dischargeDate);
            admission.setReason(reason);
            updateAdmission(admission);
            System.out.println("Admission updated successfully!");
        } catch (Exception e) {
            System.out.println("Error updating admission: " + e.getMessage());
        }
    }

    // Interactive method to delete an admission
    public void deleteAdmissionInteractive() {
        try {
            Long id = getLongInput("Enter Admission ID to delete: ");
            Admission admission = getAdmission(id);
            System.out.println("Admission to delete:\n" + admission);
            System.out.print("Are you sure? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                deleteAdmission(id);
                System.out.println("Admission deleted successfully!");
            }
        } catch (Exception e) {
            System.out.println("Error deleting admission: " + e.getMessage());
        }
    }

    // Interactive method to view all admissions
    public void viewAdmissions() {
        List<Admission> admissions = getAllAdmissions();
        System.out.println("\n===== ADMISSIONS =====");
        if (admissions.isEmpty()) {
            System.out.println("No admissions found.");
        } else {
            admissions.forEach(System.out::println);
        }
    }

    // Validate admission data
    private void validateAdmission(Admission admission) {
        if (admission == null) {
            throw new IllegalArgumentException("Admission cannot be null");
        }
        if (admission.getPatient() == null) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Patient)");
        }
        if (admission.getAdmissionDate() == null) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Admission Date)");
        }
        if (admission.getReason() == null || admission.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Reason)");
        }

        if (admission.getReason().length() > Constants.MAX_REASON_LENGTH) {
            throw new IllegalArgumentException(Constants.ERROR_REASON_TOO_LONG);
        }

        if (admission.getAdmissionDate().after(new Date(System.currentTimeMillis()))) {
            throw new IllegalArgumentException(Constants.ERROR_FUTURE_ADMISSION_DATE);
        }

        if (admission.getDischargeDate() != null && admission.getDischargeDate().before(admission.getAdmissionDate())) {
            throw new IllegalArgumentException(Constants.ERROR_DISCHARGE_BEFORE_ADMISSION);
        }
    }

    // Get required input from user
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

    // Get optional input
    private String getOptionalInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
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

    // Parse a date string
    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
            sdf.setLenient(false);
            return new Date(sdf.parse(dateStr).getTime());
        } catch (ParseException e) {
            return null;
        }
    }
}