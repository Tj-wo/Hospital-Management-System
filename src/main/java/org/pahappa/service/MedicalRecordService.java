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

public class MedicalRecordService {
    private static final Scanner scanner = new Scanner(System.in);
    private final MedicalRecordDao medicalRecordDao = new MedicalRecordDao();
    private final PatientService patientService = new PatientService();
    private final StaffService staffService = new StaffService();

    public void addMedicalRecord(MedicalRecord medicalRecord) {
        validateMedicalRecord(medicalRecord);
        medicalRecordDao.save(medicalRecord);
    }

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

    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordDao.getAll();
    }

    public List<MedicalRecord> getMedicalRecordsByPatientId(Long patientId) {
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
        }
        return medicalRecordDao.getByPatientId(patientId);
    }

    public void updateMedicalRecord(MedicalRecord medicalRecord) {
        if (medicalRecord.getId() == null) {
            throw new IllegalArgumentException("Medical record ID is required for update");
        }
        validateMedicalRecord(medicalRecord);
        medicalRecordDao.update(medicalRecord);
    }

    public void deleteMedicalRecord(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
        }
        medicalRecordDao.delete(id);
    }

    public void viewMedicalRecords() {
        System.out.println("\n===== MEDICAL RECORDS =====");
        List<MedicalRecord> records = getAllMedicalRecords();
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

    public void addMedicalRecordInteractive() {
        try {
            System.out.println("\n===== ADD NEW MEDICAL RECORD =====");

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

            // Show available doctors
            System.out.println("\n--- Available Doctors ---");
            List<Staff> doctors = staffService.getAllStaff().stream()
                    .filter(s -> s.getRole() == Role.DOCTOR)
                    .toList();
            if (doctors.isEmpty()) {
                System.out.println("No doctors available. Add a doctor first!");
                return;
            }
            doctors.forEach(d -> System.out.printf("ID: %d | Name: %s | Specialty: %s%n",
                    d.getId(), d.getFullName(), d.getSpecialty()));

            // Get doctor
            Long doctorId = getLongInput("\nEnter Doctor ID: ");
            Staff doctor = staffService.getStaff(doctorId);
            if (doctor.getRole() != Role.DOCTOR) {
                System.out.println("Selected staff is not a doctor!");
                return;
            }

            // Get record date
            String recordDateStr = getRequiredInput(
                    String.format("\nRecord Date (%s): ", Constants.DATE_FORMAT),
                    Constants.ERROR_REQUIRED_FIELD);
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
            String diagnosis = getRequiredInput("\nDiagnosis: ", Constants.ERROR_REQUIRED_FIELD);
            if (diagnosis.length() > Constants.MAX_DIAGNOSIS_LENGTH) {
                System.out.println("Diagnosis cannot exceed " + Constants.MAX_DIAGNOSIS_LENGTH + " characters");
                return;
            }

            // Get treatment
            String treatment = getRequiredInput("\nTreatment: ", Constants.ERROR_REQUIRED_FIELD);
            if (treatment.length() > Constants.MAX_TREATMENT_LENGTH) {
                System.out.println("Treatment cannot exceed " + Constants.MAX_TREATMENT_LENGTH + " characters");
                return;
            }

            // Create and save record
            MedicalRecord record = new MedicalRecord();
            record.setPatient(patient);
            record.setDoctor(doctor);
            record.setRecordDate(recordDate);
            record.setDiagnosis(diagnosis);
            record.setTreatment(treatment);

            addMedicalRecord(record);
            System.out.println("\nMedical record added successfully!");
            System.out.println("New Record ID: " + record.getId());
        } catch (Exception e) {
            System.out.println("\nError adding medical record: " + e.getMessage());
        }
    }

    public void updateMedicalRecordInteractive() {
        try {
            System.out.println("\n===== UPDATE MEDICAL RECORD =====");

            // Show all records first
            viewMedicalRecords();

            // Get record ID to update
            Long id = getLongInput("\nEnter Medical Record ID to update: ");
            MedicalRecord record = getMedicalRecord(id);

            // Show current details
            System.out.println("\n--- Current Medical Record Details ---");
            System.out.println("1. Patient: " + record.getPatient().getFullName());
            System.out.println("2. Doctor: " + record.getDoctor().getFullName());
            System.out.println("3. Record Date: " + formatDate(record.getRecordDate()));
            System.out.println("4. Diagnosis: " + record.getDiagnosis());
            System.out.println("5. Treatment: " + record.getTreatment());

            // Get updates
            System.out.println("\n--- Update Fields (press Enter to skip) ---");


            // Update doctor
            System.out.println("\nAvailable Doctors:");
            List<Staff> doctors = staffService.getAllStaff().stream()
                    .filter(s -> s.getRole() == Role.DOCTOR)
                    .toList();
            doctors.forEach(d -> System.out.printf("ID: %d | Name: %s | Specialty: %s%n",
                    d.getId(), d.getFullName(), d.getSpecialty()));
            String doctorInput = getInputWithDefault(
                    "Enter Doctor ID : ",
                    record.getDoctor().getId().toString());
            if (!doctorInput.isEmpty()) {
                Staff doctor = staffService.getStaff(Long.parseLong(doctorInput));
                if (doctor.getRole() != Role.DOCTOR) {
                    System.out.println("Selected staff is not a doctor!");
                    return;
                }
                record.setDoctor(doctor);
            }

            // Update record date
            String dateInput = getInputWithDefault(
                    String.format("Enter new Record Date (%s) [%s]: ",
                            Constants.DATE_FORMAT, formatDate(record.getRecordDate())),
                    formatDate(record.getRecordDate()));
            if (!dateInput.isEmpty()) {
                Date newDate = parseDate(dateInput);
                if (newDate != null) record.setRecordDate(newDate);
            }

            // Update diagnosis
            String diagnosis = getInputWithDefault(
                    "Enter new Diagnosis [" + record.getDiagnosis() + "]: ",
                    record.getDiagnosis());
            if (!diagnosis.isEmpty()) {
                record.setDiagnosis(diagnosis);
            }

            // Update treatment
            String treatment = getInputWithDefault(
                    "Enter new Treatment [" + record.getTreatment() + "]: ",
                    record.getTreatment());
            if (!treatment.isEmpty()) {
                record.setTreatment(treatment);
            }

            updateMedicalRecord(record);
            System.out.println("\nMedical record updated successfully!");
        } catch (Exception e) {
            System.out.println("\nError updating medical record: " + e.getMessage());
        }
    }

    public void deleteMedicalRecordInteractive() {
        try {
            System.out.println("\n===== DELETE MEDICAL RECORD =====");

            // Show all records first
            viewMedicalRecords();

            // Get record ID to delete
            Long id = getLongInput("\nEnter Medical Record ID to delete: ");

            // Confirm deletion
            System.out.print("\nAre you sure you want to delete this medical record? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                deleteMedicalRecord(id);
                System.out.println("\nMedical record deleted successfully!");
            } else {
                System.out.println("\nDeletion cancelled.");
            }
        } catch (Exception e) {
            System.out.println("\nError deleting medical record: " + e.getMessage());
        }
    }

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