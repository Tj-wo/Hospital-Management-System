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

public class AdmissionService {
    private static final Scanner scanner = new Scanner(System.in);
    private final AdmissionDao admissionDao = new AdmissionDao();
    private final PatientService patientService = new PatientService();

    public void admitPatient(Admission admission) {
        validateAdmission(admission);
        admissionDao.save(admission);
    }

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

    public List<Admission> getAllAdmissions() {
        return admissionDao.getAll();
    }

    public void updateAdmission(Admission admission) {
        if (admission.getId() == null) {
            throw new IllegalArgumentException("Admission ID is required for update");
        }
        validateAdmission(admission);
        admissionDao.update(admission);
    }

    public void deleteAdmission(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
        }
        admissionDao.delete(id);
    }

    public void viewAdmissions() {
        System.out.println("\n===== ADMISSIONS =====");
        List<Admission> admissions = getAllAdmissions();
        if (admissions.isEmpty()) {
            System.out.println("No admissions found.");
            return;
        }

        System.out.println("ID  | Patient          | Admission Date | Discharge Date | Reason");
        System.out.println("----|------------------|----------------|----------------|-------------------");
        admissions.forEach(a -> System.out.printf(
                "%-3d | %-16s | %-14s | %-14s | %s%n",
                a.getId(),
                truncate(a.getPatient().getFullName(), 16),
                formatDate(a.getAdmissionDate()),
                formatDate(a.getDischargeDate()),
                truncate(a.getReason(), 20)
        ));
    }

    public void admitPatientInteractive() {
        try {
            System.out.println("\n===== ADMIT PATIENT =====");

            // Show available patients
            System.out.println("\n--- Available Patients ---");
            List<Patient> patients = patientService.getAllPatients();
            if (patients.isEmpty()) {
                System.out.println("No patients available. Add a patient first!");
                return;
            }
            patients.forEach(p -> System.out.printf("ID: %d | Name: %s%n", p.getId(), p.getFullName()));

            // Get patient
            Long patientId = getLongInput("\nEnter Patient ID: ");
            Patient patient = patientService.getPatient(patientId);

            // Get admission date
            String admissionDateStr = getRequiredInput(
                    String.format("\nAdmission Date (%s): ", Constants.DATE_FORMAT),
                    Constants.ERROR_REQUIRED_FIELD);
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
            String dischargeDateStr = getOptionalInput(
                    String.format("\nDischarge Date (%s) (optional): ", Constants.DATE_FORMAT));
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
            String reason = getRequiredInput("\nReason: ", Constants.ERROR_REQUIRED_FIELD);
            if (reason.length() > Constants.MAX_REASON_LENGTH) {
                System.out.println(Constants.ERROR_REASON_TOO_LONG);
                return;
            }

            Admission admission = new Admission(patient, admissionDate, dischargeDate, reason);
            admitPatient(admission);
            System.out.println("\nPatient admitted successfully!");
        } catch (Exception e) {
            System.out.println("\nError admitting patient: " + e.getMessage());
        }
    }

    public void updateAdmissionInteractive() {
        try {
            System.out.println("\n===== UPDATE ADMISSION =====");

            // Show all admissions first
            viewAdmissions();

            // Get admission ID to update
            Long id = getLongInput("\nEnter Admission ID to update: ");
            Admission admission = getAdmission(id);

            // Show current details
            System.out.println("\n--- Current Admission Details ---");
            System.out.println("1. Patient: " + admission.getPatient().getFullName());
            System.out.println("2. Admission Date: " + formatDate(admission.getAdmissionDate()));
            System.out.println("3. Discharge Date: " + formatDate(admission.getDischargeDate()));
            System.out.println("4. Reason: " + admission.getReason());

            // Get updates
            System.out.println("\n--- Update Fields (press Enter to skip) ---");

            // Update patient
            System.out.println("\nAvailable Patients:");
            List<Patient> patients = patientService.getAllPatients();
            patients.forEach(p -> System.out.printf("ID: %d | Name: %s%n", p.getId(), p.getFullName()));
            String patientInput = getInputWithDefault(
                    "Enter new Patient ID [" + admission.getPatient().getId() + "]: ",
                    admission.getPatient().getId().toString());
            if (!patientInput.isEmpty()) {
                admission.setPatient(patientService.getPatient(Long.parseLong(patientInput)));
            }

            // Update admission date
            String admissionDateStr = getInputWithDefault(
                    String.format("Enter new Admission Date (%s) [%s]: ",
                            Constants.DATE_FORMAT, formatDate(admission.getAdmissionDate())),
                    formatDate(admission.getAdmissionDate()));
            if (!admissionDateStr.isEmpty()) {
                Date newDate = parseDate(admissionDateStr);
                if (newDate != null) admission.setAdmissionDate(newDate);
            }

            // Update discharge date
            String dischargeDateStr = getInputWithDefault(
                    String.format("Enter new Discharge Date (%s) [%s] (optional): ",
                            Constants.DATE_FORMAT, formatDate(admission.getDischargeDate())),
                    admission.getDischargeDate() != null ? formatDate(admission.getDischargeDate()) : "");
            if (!dischargeDateStr.isEmpty()) {
                Date newDate = parseDate(dischargeDateStr);
                if (newDate != null) admission.setDischargeDate(newDate);
            } else {
                admission.setDischargeDate(null);
            }

            // Update reason
            String reason = getInputWithDefault(
                    "Enter new Reason [" + admission.getReason() + "]: ",
                    admission.getReason());
            if (!reason.isEmpty()) {
                admission.setReason(reason);
            }

            updateAdmission(admission);
            System.out.println("\nAdmission updated successfully!");
        } catch (Exception e) {
            System.out.println("\nError updating admission: " + e.getMessage());
        }
    }

    public void deleteAdmissionInteractive() {
        try {
            System.out.println("\n===== DELETE ADMISSION =====");

            // Show all admissions first
            viewAdmissions();

            // Get admission ID to delete
            Long id = getLongInput("\nEnter Admission ID to delete: ");

            // Confirm deletion
            System.out.print("\nAre you sure you want to delete this admission? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                deleteAdmission(id);
                System.out.println("\nAdmission deleted successfully!");
            } else {
                System.out.println("\nDeletion cancelled.");
            }
        } catch (Exception e) {
            System.out.println("\nError deleting admission: " + e.getMessage());
        }
    }

    private void validateAdmission(Admission admission) {
        // ... (keep existing validation logic)
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

    private String getOptionalInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
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