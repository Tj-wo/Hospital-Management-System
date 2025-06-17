package org.pahappa.service;

import org.pahappa.dao.AdmissionDao;
import org.pahappa.model.Admission;
import org.pahappa.model.Patient;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

public class AdmissionService {
    private final AdmissionDao admissionDao = new AdmissionDao();
    private static final Scanner scanner = new Scanner(System.in);
    private final PatientService patientService = new PatientService();

    public void admitPatient(Admission admission) {
        admissionDao.save(admission);
    }

    public Admission getAdmission(Long id) {
        return admissionDao.getById(id);
    }

    public List<Admission> getAllAdmissions() {
        return admissionDao.getAll();
    }

    public List<Admission> getPatientAdmissions(Long patientId) {
        return admissionDao.getByPatientId(patientId);
    }

    public void dischargePatient(Long id, Date dischargeDate) {
        Admission admission = getAdmission(id);
        if (admission != null) {
            admission.setDischargeDate(dischargeDate);
            admissionDao.update(admission);
        }
    }

    public void deleteAdmission(Long id) {
        admissionDao.delete(id);
    }

    public void addAdmissionInteractive() {
        try {
            List<Patient> patients = patientService.getAllPatients();
            if (patients.isEmpty()) {
                System.out.println("No patients available. Add a patient first!");
                return;
            }
            patients.forEach(p -> System.out.println(p.getId() + ": " + p.getFirstName() + " " + p.getLastName()));
            Long patientId = Long.parseLong(getStringInput("Enter Patient ID: "));

            String dateStr = getStringInput("Enter Admission Date (yyyy-mm-dd): ");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            java.util.Date utilDate = sdf.parse(dateStr);
            if (utilDate.after(new java.util.Date())) {
                System.out.println("Admission Date cannot be in the future!");
                return;
            }
            Date admissionDate = new Date(utilDate.getTime());

            String reason = getStringInput("Enter Reason: ");
            if (reason.isEmpty()) {
                System.out.println("Reason is required!");
                return;
            }

            Patient patient = patientService.getPatient(patientId);
            if (patient == null) {
                System.out.println("Invalid Patient ID!");
                return;
            }

            Admission admission = new Admission(patient, admissionDate, reason);
            admitPatient(admission);
            System.out.println("Admission added successfully!");
        } catch (ParseException e) {
            System.out.println("Invalid date format! Use yyyy-mm-dd.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format!");
        } catch (Exception e) {
            System.out.println("Error adding admission: " + e.getMessage());
        }
    }

    public void viewAdmissions() {
        System.out.println("Admissions:");
        getAllAdmissions().forEach(System.out::println);
        if (getAllAdmissions().isEmpty()) {
            System.out.println("No admissions found.");
        }
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}