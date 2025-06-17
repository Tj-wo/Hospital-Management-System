package org.pahappa.main;

import org.pahappa.com.model.Patient;
import org.pahappa.com.service.PatientService;
import java.sql.Date;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hospital Management System starting...");
        PatientService patientService = new PatientService();
        try {
            // Add a test patient
            Patient patient = new Patient("John", "Doe", new Date(System.currentTimeMillis()).toLocalDate(), "john.doe@example.com", "1234567890", "123 Main St");
            patientService.registerPatient(patient);
            patientService.testConnection(); // Check again
        } catch (Exception e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Application finished.");
        System.exit(0);
    }
}