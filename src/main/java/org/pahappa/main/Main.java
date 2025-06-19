package org.pahappa;

import org.pahappa.service.*;
import java.util.Scanner;

// Main class to run the Hospital Management System console application
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final PatientService patientService = new PatientService();
    private static final StaffService staffService = new StaffService();
    private static final MedicalRecordService medicalRecordService = new MedicalRecordService();
    private static final AppointmentService appointmentService = new AppointmentService();
    private static final AdmissionService admissionService = new AdmissionService();

    public static void main(String[] args) {
        System.out.println("Welcome to the Hospital Management System!");
        while (true) {
            displayMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        managePatients();
                        break;
                    case "2":
                        manageStaff();
                        break;
                    case "3":
                        manageMedicalRecords();
                        break;
                    case "4":
                        manageAppointments();
                        break;
                    case "5":
                        manageAdmissions();
                        break;
                    case "6":
                        System.out.println("Exiting system. Goodbye!");
                        scanner.close();
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice! Please select a number between 1 and 6.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    // Display the main menu
    private static void displayMenu() {
        System.out.println("\n===== HOSPITAL MANAGEMENT SYSTEM =====");
        System.out.println("1. Manage Patients");
        System.out.println("2. Manage Staff");
        System.out.println("3. Manage Medical Records");
        System.out.println("4. Manage Appointments");
        System.out.println("5. Manage Admissions");
        System.out.println("6. Exit");
        System.out.print("Enter your choice: ");
    }

    // Submenu for patient operations
    private static void managePatients() {
        while (true) {
            System.out.println("\n===== PATIENT MANAGEMENT =====");
            System.out.println("1. Add Patient");
            System.out.println("2. Update Patient");
            System.out.println("3. Delete Patient");
            System.out.println("4. View All Patients");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    patientService.addPatientInteractive();
                    break;
                case "2":
                    patientService.updatePatientInteractive();
                    break;
                case "3":
                    patientService.deletePatientInteractive();
                    break;
                case "4":
                    patientService.viewPatients();
                    break;
                case "5":
                    return;
                default:
                    System.out.println("Invalid choice! Please select a number between 1 and 5.");
            }
        }
    }

    // Submenu for staff operations
    private static void manageStaff() {
        while (true) {
            System.out.println("\n===== STAFF MANAGEMENT =====");
            System.out.println("1. Add Staff");
            System.out.println("2. Update Staff");
            System.out.println("3. Delete Staff");
            System.out.println("4. View All Staff");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    staffService.addStaffInteractive();
                    break;
                case "2":
                    staffService.updateStaffInteractive();
                    break;
                case "3":
                    staffService.deleteStaffInteractive();
                    break;
                case "4":
                    staffService.viewStaff();
                    break;
                case "5":
                    return;
                default:
                    System.out.println("Invalid choice! Please select a number between 1 and 5.");
            }
        }
    }

    // Submenu for medical record operations
    private static void manageMedicalRecords() {
        while (true) {
            System.out.println("\n===== MEDICAL RECORD MANAGEMENT =====");
            System.out.println("1. Add Medical Record");
            System.out.println("2. Update Medical Record");
            System.out.println("3. Delete Medical Record");
            System.out.println("4. View All Medical Records");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    medicalRecordService.addMedicalRecordInteractive();
                    break;
                case "2":
                    medicalRecordService.updateMedicalRecordInteractive();
                    break;
                case "3":
                    medicalRecordService.deleteMedicalRecordInteractive();
                    break;
                case "4":
                    medicalRecordService.viewMedicalRecords();
                    break;
                case "5":
                    return;
                default:
                    System.out.println("Invalid choice! Please select a number between 1 and 5.");
            }
        }
    }

    // Submenu for appointment operations
    private static void manageAppointments() {
        while (true) {
            System.out.println("\n===== APPOINTMENT MANAGEMENT =====");
            System.out.println("1. Schedule Appointment");
            System.out.println("2. Update Appointment");
            System.out.println("3. Delete Appointment");
            System.out.println("4. View All Appointments");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    appointmentService.scheduleAppointmentInteractive();
                    break;
                case "2":
                    appointmentService.updateAppointmentInteractive();
                    break;
                case "3":
                    appointmentService.deleteAppointmentInteractive();
                    break;
                case "4":
                    appointmentService.viewAppointments();
                    break;
                case "5":
                    return;
                default:
                    System.out.println("Invalid choice! Please select a number between 1 and 5.");
            }
        }
    }

    // Submenu for admission operations
    private static void manageAdmissions() {
        while (true) {
            System.out.println("\n===== ADMISSION MANAGEMENT =====");
            System.out.println("1. Admit Patient");
            System.out.println("2. Update Admission");
            System.out.println("3. Delete Admission");
            System.out.println("4. View All Admissions");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    admissionService.admitPatientInteractive();
                    break;
                case "2":
                    admissionService.updateAdmissionInteractive();
                    break;
                case "3":
                    admissionService.deleteAdmissionInteractive();
                    break;
                case "4":
                    admissionService.viewAdmissions();
                    break;
                case "5":
                    return;
                default:
                    System.out.println("Invalid choice! Please select a number between 1 and 5.");
            }
        }
    }
}