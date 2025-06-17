package org.pahappa.service;

import org.pahappa.dao.PatientDao;
import org.pahappa.model.Patient;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

public class PatientService {
    private final PatientDao patientDao = new PatientDao();
    private static final Scanner scanner = new Scanner(System.in);

    public void registerPatient(Patient patient) {
        patientDao.save(patient);
    }

    public Patient getPatient(Long id) {
        return patientDao.getById(id);
    }

    public List<Patient> getAllPatients() {
        return patientDao.getAll();
    }

    public void updatePatient(Patient patient) {
        patientDao.update(patient);
    }

    public void deletePatient(Long id) {
        patientDao.delete(id);
    }

    public void testConnection() {
        List<Patient> patients = getAllPatients();
        System.out.println("Connected to DB. Found " + patients.size() + " patients.");
        patients.forEach(System.out::println);
    }

    public void addPatientInteractive() {
        try {
            String firstName = getStringInput("Enter First Name: ");
            if (firstName.isEmpty()) {
                System.out.println("First Name is required!");
                return;
            }

            String lastName = getStringInput("Enter Last Name: ");
            if (lastName.isEmpty()) {
                System.out.println("Last Name is required!");
                return;
            }

            String dateStr = getStringInput("Enter Date of Birth (yyyy-mm-dd): ");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            java.util.Date utilDate = sdf.parse(dateStr);
            if (utilDate.after(new java.util.Date())) {
                System.out.println("Date of Birth cannot be in the future!");
                return;
            }
            Date dateOfBirth = new Date(utilDate.getTime());

            String email = getStringInput("Enter Email: ");
            if (email.isEmpty()) {
                System.out.println("Email is required!");
                return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                System.out.println("Invalid email format!");
                return;
            }

            String phone = getStringInput("Enter Phone (optional): ");
            String address = getStringInput("Enter Address (optional): ");

            Patient patient = new Patient(firstName, lastName, dateOfBirth, email, phone, address);
            registerPatient(patient);
            System.out.println("Patient added successfully!");
        } catch (ParseException e) {
            System.out.println("Invalid date format! Use yyyy-mm-dd.");
        }
    }

    public void viewPatients() {
        System.out.println("Patients:");
        getAllPatients().forEach(System.out::println);
        if (getAllPatients().isEmpty()) {
            System.out.println("No patients found.");
        }
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}