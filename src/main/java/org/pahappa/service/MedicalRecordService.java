package org.pahappa.service;

import org.pahappa.dao.MedicalRecordDao;
import org.pahappa.model.MedicalRecord;
import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
import org.pahappa.utils.Constants;
import org.pahappa.utils.Role;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

// Service class to manage medical record operations
public class MedicalRecordService {
    private static final Scanner scanner = new Scanner(System.in);
    private final MedicalRecordDao medicalRecordDao = new MedicalRecordDao();
    private final PatientService patientService = new PatientService();
    private final StaffService staffService = new StaffService();

    // Add a new medical record
    public void addMedicalRecord(MedicalRecord medicalRecord) {
        validateMedicalRecord(medicalRecord);
        medicalRecordDao.save(medicalRecord);
    }

    // Get a medical record by ID
    public MedicalRecord getMedicalRecord(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
        }
        MedicalRecord medicalRecord = medicalRecordDao.getById(id);
        if (medicalRecord == null) {
            throw new IllegalArgumentException("Medical record not found with ID: " + id);
        }
        return medicalRecord;
    }

    // Get all medical records
    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordDao.getAll();
    }

    // Get medical records by patient ID
    public List<MedicalRecord> getMedicalRecordsByPatientId(Long patientId) {
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
        }
        return medicalRecordDao.getByPatientId(patientId);
    }

    // Update a medical record
    public void updateMedicalRecord(MedicalRecord medicalRecord) {
        if (medicalRecord.getId() == null) {
            throw new IllegalArgumentException("Medical record ID is required for update");
        }
        validateMedicalRecord(medicalRecord);
        medicalRecordDao.update(medicalRecord);
    }

    // Delete a medical record by ID
    public void deleteMedicalRecord(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
        }
        medicalRecordDao.delete(id);
    }

    // Interactive method to add a medical record
    public void addMedicalRecordInteractive() {
        try {
            // Select patient
            List<Patient> patients = patientService.getAllPatients();
            if (patients.isEmpty()) {
                System.out.println("No patients available. Add a patient first!");
                return;
            }
            System.out.println("\nAvailable Patients:");
            patients.forEach(p -> System.out.printf("%d: %s%n", p.getId(), p.getFullName()));
            Long patientId = getLongInput("Enter Patient ID: ");
            Patient patient = patientService.getPatient(patientId);

            // Select doctor
            List<Staff> doctors = staffService.getAllStaff().stream()
                    .filter(s -> s.getRole() == Role.DOCTOR)
                    .toList();
            if (doctors.isEmpty()) {
                System.out.println("No doctors available. Add a doctor first!");
                return;
            }
            System.out.println("\nAvailable Doctors:");
            doctors.forEach(d -> System.out.printf("%d: %s %s (%s)%n", d.getId(), d.getFirstName(), d.getLastName(), d.getSpecialty()));
            Long doctorId = getLongInput("Enter Doctor ID: ");
            Staff doctor = staffService.getStaff(doctorId);
            if (doctor.getRole() != Role.DOCTOR) {
                System.out.println("Selected staff is not a doctor!");
                return;
            }

            // Get record date
            String recordDateStr = getRequiredInput("Enter Record Date (" + Constants.DATE_FORMAT + "): ", Constants.ERROR_REQUIRED_FIELD);
            Date recordDate = parseDate(recordDateStr);
            if (recordDate == null) {
                System.out.println(Constants.ERROR_INVALID_DATE);
                return;
            }
            if (recordDate.after(new Date(System.currentTimeMillis()))) {
                System.out.println("Record date cannot be in the future");
                return;
            }

            // Get diagnosis
            String diagnosis = getRequiredInput("Enter Diagnosis: ", Constants.ERROR_REQUIRED_FIELD);
            if (diagnosis.length() > Constants.MAX_DIAGNOSIS_LENGTH) {
                System.out.println("Diagnosis cannot exceed " + Constants.MAX_DIAGNOSIS_LENGTH + " characters");
                return;
            }

            // Get treatment
            String treatment = getRequiredInput("Enter Treatment: ", Constants.ERROR_REQUIRED_FIELD);
            if (treatment.length() > Constants.MAX_TREATMENT_LENGTH) {
                System.out.println("Treatment cannot exceed " + Constants.MAX_TREATMENT_LENGTH + " characters");
                return;
            }

            MedicalRecord medicalRecord = new MedicalRecord();
            medicalRecord.setPatient(patient);
            medicalRecord.setDoctor(doctor);
            medicalRecord.setRecordDate(recordDate);
            medicalRecord.setDiagnosis(diagnosis);
            medicalRecord.setTreatment(treatment);
            addMedicalRecord(medicalRecord);
            System.out.println("Medical record added successfully!");
        } catch (Exception e) {
            System.out.println("Error adding medical record: " + e.getMessage());
        }
    }

    // Interactive method to update a medical record
    public void updateMedicalRecordInteractive() {
        try {
            Long id = getLongInput("Enter Medical Record ID to update: ");
            MedicalRecord medicalRecord = getMedicalRecord(id);
            System.out.println("Current details:\n" + medicalRecord);

            // Update patient
            List<Patient> patients = patientService.getAllPatients();
            if (patients.isEmpty()) {
                System.out.println("No patients available!");
                return;
            }
            System.out.println("\nAvailable Patients:");
            patients.forEach(p -> System.out.printf("%d: %s%n", p.getId(), p.getFullName()));
            Long patientId = getLongInput("Enter new Patient ID [" + medicalRecord.getPatient().getId() + "]: ");
            Patient patient = patientService.getPatient(patientId);

            // Update doctor
            List<Staff> doctors = staffService.getAllStaff().stream()
                    .filter(s -> s.getRole() == Role.DOCTOR)
                    .toList();
            if (doctors.isEmpty()) {
                System.out.println("No doctors available!");
                return;
            }
            System.out.println("\nAvailable Doctors:");
            doctors.forEach(d -> System.out.printf("%d: %s %s (%s)%n", d.getId(), d.getFirstName(), d.getLastName(), d.getSpecialty()));
            Long doctorId = getLongInput("Enter new Doctor ID [" + medicalRecord.getDoctor().getId() + "]: ");
            Staff doctor = staffService.getStaff(doctorId);
            if (doctor.getRole() != Role.DOCTOR) {
                System.out.println("Selected staff is not a doctor!");
                return;
            }

            // Update record date
            String recordDateStr = getRequiredInput("Enter new Record Date (" + Constants.DATE_FORMAT + ") [" + medicalRecord.getRecordDate() + "]: ", medicalRecord.getRecordDate().toString());
            Date recordDate = parseDate(recordDateStr);
            if (recordDate == null) {
                System.out.println(Constants.ERROR_INVALID_DATE);
                return;
            }
            if (recordDate.after(new Date(System.currentTimeMillis()))) {
                System.out.println("Record date cannot be in the future");
                return;
            }

            // Update diagnosis
            String diagnosis = getRequiredInput("Enter new Diagnosis [" + medicalRecord.getDiagnosis() + "]: ", medicalRecord.getDiagnosis());
            if (diagnosis.length() > Constants.MAX_DIAGNOSIS_LENGTH) {
                System.out.println("Diagnosis cannot exceed " + Constants.MAX_DIAGNOSIS_LENGTH + " characters");
                return;
            }

            // Update treatment
            String treatment = getRequiredInput("Enter new Treatment [" + medicalRecord.getTreatment() + "]: ", medicalRecord.getTreatment());
            if (treatment.length() > Constants.MAX_TREATMENT_LENGTH) {
                System.out.println("Treatment cannot exceed " + Constants.MAX_TREATMENT_LENGTH + " characters");
                return;
            }

            medicalRecord.setPatient(patient);
            medicalRecord.setDoctor(doctor);
            medicalRecord.setRecordDate(recordDate);
            medicalRecord.setDiagnosis(diagnosis);
            medicalRecord.setTreatment(treatment);
            updateMedicalRecord(medicalRecord);
            System.out.println("Medical record updated successfully!");
        } catch (Exception e) {
            System.out.println("Error updating medical record: " + e.getMessage());
        }
    }

    // Interactive method to delete a medical record
    public void deleteMedicalRecordInteractive() {
        try {
            Long id = getLongInput("Enter Medical Record ID to delete: ");
            MedicalRecord medicalRecord = getMedicalRecord(id);
            System.out.println("Medical record to delete:\n" + medicalRecord);
            System.out.print("Are you sure? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                deleteMedicalRecord(id);
                System.out.println("Medical record deleted successfully!");
            }
        } catch (Exception e) {
            System.out.println("Error deleting medical record: " + e.getMessage());
        }
    }

    // Interactive method to view all medical records
    public void viewMedicalRecords() {
        List<MedicalRecord> medicalRecords = getAllMedicalRecords();
        System.out.println("\n===== MEDICAL RECORDS =====");
        if (medicalRecords.isEmpty()) {
            System.out.println("No medical records found.");
        } else {
            medicalRecords.forEach(System.out::println);
        }
    }

    // Validate medical record data
    private void validateMedicalRecord(MedicalRecord medicalRecord) {
        if (medicalRecord == null) {
            throw new IllegalArgumentException("Medical record cannot be null");
        }
        if (medicalRecord.getPatient() == null) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Patient)");
        }
        if (medicalRecord.getDoctor() == null) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Doctor)");
        }
        if (medicalRecord.getRecordDate() == null) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Record Date)");
        }
        if (medicalRecord.getDiagnosis() == null || medicalRecord.getDiagnosis().trim().isEmpty()) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Diagnosis)");
        }
        if (medicalRecord.getTreatment() == null || medicalRecord.getTreatment().trim().isEmpty()) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Treatment)");
        }

        if (medicalRecord.getDoctor().getRole() != Role.DOCTOR) {
            throw new IllegalArgumentException("Doctor must have role DOCTOR");
        }
        if (medicalRecord.getDiagnosis().length() > Constants.MAX_DIAGNOSIS_LENGTH) {
            throw new IllegalArgumentException("Diagnosis cannot exceed " + Constants.MAX_DIAGNOSIS_LENGTH + " characters");
        }
        if (medicalRecord.getTreatment().length() > Constants.MAX_TREATMENT_LENGTH) {
            throw new IllegalArgumentException("Treatment cannot exceed " + Constants.MAX_TREATMENT_LENGTH + " characters");
        }
        if (medicalRecord.getRecordDate().after(new Date(System.currentTimeMillis()))) {
            throw new IllegalArgumentException("Record date cannot be in the future");
        }
    }

    // Parse date string
    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
            sdf.setLenient(false);
            return new Date(sdf.parse(dateStr).getTime());
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