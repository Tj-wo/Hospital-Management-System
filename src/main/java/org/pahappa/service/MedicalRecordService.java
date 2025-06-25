package org.pahappa.service;

import org.pahappa.dao.AdmissionDao;
import org.pahappa.dao.MedicalRecordDao;
import org.pahappa.model.*;
import org.pahappa.session.SessionManager;
import org.pahappa.utils.Constants;
import org.pahappa.utils.Role;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

public class MedicalRecordService {
    private static final Scanner scanner = new Scanner(System.in);
    private final MedicalRecordDao medicalRecordDao = new MedicalRecordDao();
    private final PatientService patientService = new PatientService();
    private final StaffService staffService = new StaffService();
    private final AdmissionDao admissionDao = new AdmissionDao(); // Needed for nurse permission check

    /**
     * Adds a new medical record. Restricted to DOCTORS.
     */
    public void addMedicalRecord(MedicalRecord medicalRecord) {
        if (SessionManager.getCurrentUser().getRole() != Role.DOCTOR) {
            throw new SecurityException("Access Denied: Only Doctors can create medical records.");
        }
        validateMedicalRecord(medicalRecord);
        medicalRecordDao.save(medicalRecord);
    }

    /**
     * Updates a medical record. Doctors can update any. Nurses can update for their assigned, active patients.
     */
    public void updateMedicalRecord(MedicalRecord medicalRecord) {
        User currentUser = SessionManager.getCurrentUser();
        Role role = currentUser.getRole();

        if (role == Role.DOCTOR) {
            // Doctors have full access to update
        } else if (role == Role.NURSE) {
            // Check if the nurse is assigned to this patient's active admission
            boolean isAssigned = admissionDao.findByPatientId(medicalRecord.getPatient().getId()).stream()
                    .anyMatch(a -> a.getNurse() != null && a.getNurse().getId().equals(currentUser.getStaff().getId()) && a.getDischargeDate() == null);
            if (!isAssigned) {
                throw new SecurityException("Access Denied: You can only update records for your currently assigned patients.");
            }
        } else {
            throw new SecurityException("Access Denied: You do not have permission to update medical records.");
        }

        validateMedicalRecord(medicalRecord);
        medicalRecordDao.update(medicalRecord);
    }

    /**
     * Displays medical records based on the user's role.
     */
    public void viewMedicalRecords() {
        User currentUser = SessionManager.getCurrentUser();
        List<MedicalRecord> records;
        System.out.println("\n===== MEDICAL RECORDS =====");

        switch (currentUser.getRole()) {
            case PATIENT:
                records = medicalRecordDao.getByPatientId(currentUser.getPatient().getId());
                System.out.println("--- Showing Your Medical History ---");
                break;
            case DOCTOR:
                records = medicalRecordDao.getAll(); // Doctors can see all records for context
                System.out.println("--- Showing All Medical Records ---");
                break;
            case NURSE:
                System.out.println("Note: Nurses can view all records for context, but can only update for assigned patients.");
                records = medicalRecordDao.getAll();
                break;
            case ADMIN:
                records = medicalRecordDao.getAll();
                break;
            default:
                System.out.println("You do not have permission to view medical records.");
                return;
        }

        if (records.isEmpty()) {
            System.out.println("No medical records found.");
            return;
        }

        System.out.println("ID  | Patient          | Doctor           | Date       | Diagnosis");
        System.out.println("----|------------------|------------------|------------|-------------------");
        records.forEach(r -> System.out.printf(
                "%-3d | %-16s | %-16s | %-10s | %s%n",
                r.getId(),
                truncate(r.getPatient().getFullName(), 16),
                truncate(r.getDoctor().getFullName(), 16),
                formatDate(r.getRecordDate()),
                truncate(r.getDiagnosis(), 20)
        ));
    }

    /**
     * Interactive flow for a doctor to add a new medical record.
     */
    public void addMedicalRecordInteractive() {
        if (SessionManager.getCurrentUser().getRole() != Role.DOCTOR) {
            System.out.println("Access Denied.");
            return;
        }

        try {
            System.out.println("\n===== ADD NEW MEDICAL RECORD =====");
            patientService.viewPatients();
            Long patientId = getLongInput("\nEnter Patient ID: ");
            Patient patient = patientService.getPatient(patientId);

            // The doctor creating the record is the currently logged-in doctor
            Staff doctor = SessionManager.getCurrentUser().getStaff();
            System.out.println("Recording Doctor: " + doctor.getFullName());

            Date recordDate = new Date(System.currentTimeMillis());
            String diagnosis = getRequiredInput("\nDiagnosis: ", Constants.ERROR_REQUIRED_FIELD);
            String treatment = getRequiredInput("\nTreatment Plan: ", Constants.ERROR_REQUIRED_FIELD);

            MedicalRecord record = new MedicalRecord();
            record.setPatient(patient);
            record.setDoctor(doctor);
            record.setRecordDate(recordDate);
            record.setDiagnosis(diagnosis);
            record.setTreatment(treatment);

            addMedicalRecord(record);
            System.out.println("\nMedical record added successfully!");
        } catch (Exception e) {
            System.err.println("\nError adding medical record: " + e.getMessage());
        }
    }

    /**
     * Interactive flow for updating a medical record.
     */
    public void updateMedicalRecordInteractive() {
        Role role = SessionManager.getCurrentUser().getRole();
        if(role != Role.DOCTOR && role != Role.NURSE){
            System.out.println("Access Denied.");
            return;
        }

        try {
            viewMedicalRecords();
            Long id = getLongInput("\nEnter Medical Record ID to update: ");
            MedicalRecord record = medicalRecordDao.getById(id);
            if(record == null) { System.out.println("Record not found."); return; }

            System.out.println("\n--- Updating Record (press Enter to skip) ---");
            String diagnosis = getInputWithDefault("New Diagnosis [" + record.getDiagnosis() + "]: ", record.getDiagnosis());
            String treatment = getInputWithDefault("New Treatment [" + record.getTreatment() + "]: ", record.getTreatment());

            record.setDiagnosis(diagnosis);
            record.setTreatment(treatment);

            updateMedicalRecord(record);
            System.out.println("\nMedical record updated successfully!");
        } catch (Exception e) {
            System.err.println("\nError updating medical record: " + e.getMessage());
        }
    }

    public void deleteMedicalRecordInteractive(){
        if (SessionManager.getCurrentUser().getRole() != Role.ADMIN) {
            System.out.println("Access Denied: Only Admins can delete medical records.");
            return;
        }
        // Deleting records is a serious action, hence restricted to Admin
        // Logic similar to other delete interactive methods
    }

    // --- Helper and Validation Methods ---
    private void validateMedicalRecord(MedicalRecord record) {
        if (record == null) throw new IllegalArgumentException("Medical record cannot be null");
        if (record.getPatient() == null) throw new IllegalArgumentException("Patient is required");
        if (record.getDoctor() == null) throw new IllegalArgumentException("Doctor is required");
        if (record.getRecordDate() == null) throw new IllegalArgumentException("Record date is required");
        if (record.getDiagnosis() == null || record.getDiagnosis().trim().isEmpty()) throw new IllegalArgumentException("Diagnosis is required");
    }
    private String formatDate(Date date) { return date == null ? "N/A" : new SimpleDateFormat(Constants.DATE_FORMAT).format(date); }
    private String truncate(String str, int len) { return str == null ? "" : (str.length() > len ? str.substring(0, len-3) + "..." : str); }
    private String getRequiredInput(String p, String e) { String i; do { System.out.print(p); i = scanner.nextLine().trim(); if(i.isEmpty()) System.out.println(e); } while (i.isEmpty()); return i; }
    private String getInputWithDefault(String p, String d) { System.out.print(p); String i = scanner.nextLine().trim(); return i.isEmpty() ? d : i; }
    private Long getLongInput(String p) { while (true) { try { System.out.print(p); return Long.parseLong(scanner.nextLine().trim()); } catch (NumberFormatException e) { System.out.println("Invalid ID."); } } }
}