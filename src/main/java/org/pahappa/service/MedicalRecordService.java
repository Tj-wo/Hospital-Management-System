package org.pahappa.service;

import org.pahappa.dao.MedicalRecordDao;
import org.pahappa.model.MedicalRecord;
import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

public class MedicalRecordService {
    private final MedicalRecordDao medicalRecordDao = new MedicalRecordDao();
    private static final Scanner scanner = new Scanner(System.in);
    private final PatientService patientService = new PatientService();
    private final StaffService staffService = new StaffService();

    public void addMedicalRecord(MedicalRecord record) {
        medicalRecordDao.save(record);
    }

    public MedicalRecord getMedicalRecord(Long id) {
        return medicalRecordDao.getById(id);
    }

    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordDao.getAll();
    }

    public List<MedicalRecord> getPatientHistory(Long patientId) {
        return medicalRecordDao.getByPatientId(patientId);
    }

    public void updateMedicalRecord(MedicalRecord record) {
        medicalRecordDao.update(record);
    }

    public void deleteMedicalRecord(Long id) {
        medicalRecordDao.delete(id);
    }

    public void addMedicalRecordInteractive() {
        try {
            List<Patient> patients = patientService.getAllPatients();
            if (patients.isEmpty()) {
                System.out.println("No patients available. Add a patient first!");
                return;
            }
            patients.forEach(p -> System.out.println(p.getId() + ": " + p.getFirstName() + " " + p.getLastName()));
            Long patientId = Long.parseLong(getStringInput("Enter Patient ID: "));

            List<Staff> doctors = staffService.getAllStaff().stream()
                    .filter(s -> "DOCTOR".equalsIgnoreCase(s.getRole()))
                    .toList();
            if (doctors.isEmpty()) {
                System.out.println("No doctors available. Add a doctor first!");
                return;
            }
            doctors.forEach(d -> System.out.println(d.getId() + ": " + d.getFirstName() + " " + d.getLastName()));
            Long doctorId = Long.parseLong(getStringInput("Enter Doctor ID: "));

            String diagnosis = getStringInput("Enter Diagnosis: ");
            if (diagnosis.isEmpty()) {
                System.out.println("Diagnosis is required!");
                return;
            }

            String prescription = getStringInput("Enter Prescription (optional): ");
            String dateStr = getStringInput("Enter Record Date (yyyy-mm-dd): ");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            java.util.Date utilDate = sdf.parse(dateStr);
            if (utilDate.after(new java.util.Date())) {
                System.out.println("Record Date cannot be in the future!");
                return;
            }
            Date recordDate = new Date(utilDate.getTime());

            Patient patient = patientService.getPatient(patientId);
            Staff doctor = staffService.getStaff(doctorId);
            if (patient == null || doctor == null) {
                System.out.println("Invalid Patient or Doctor ID!");
                return;
            }

            MedicalRecord record = new MedicalRecord(patient, doctor, diagnosis, prescription, recordDate);
            addMedicalRecord(record);
            System.out.println("Medical Record added successfully!");
        } catch (ParseException e) {
            System.out.println("Invalid date format! Use yyyy-mm-dd.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format!");
        } catch (Exception e) {
            System.out.println("Error adding medical record: " + e.getMessage());
        }
    }

    public void viewMedicalRecords() {
        System.out.println("Medical Records:");
        getAllMedicalRecords().forEach(System.out::println);
        if (getAllMedicalRecords().isEmpty()) {
            System.out.println("No medical records found.");
        }
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}