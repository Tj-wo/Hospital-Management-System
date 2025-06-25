package org.pahappa.main;

import org.pahappa.model.User;
import org.pahappa.service.*;
import org.pahappa.session.SessionManager;

import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserService userService = new UserService();
    private static final PatientService patientService = new PatientService();
    private static final StaffService staffService = new StaffService();
    private static final MedicalRecordService medicalRecordService = new MedicalRecordService();
    private static final AppointmentService appointmentService = new AppointmentService();
    private static final AdmissionService admissionService = new AdmissionService();

    public static void main(String[] args) {
        // Run the data initializer on startup to ensure a default admin exists
        DataInitializer.initialize();

        System.out.println("Welcome to the Enhanced Hospital Management System!");
        while (true) {
            if (SessionManager.getCurrentUser() == null) {
                showLoginMenu();
            } else {
                showDashboard();
            }
        }
    }

    private static void showLoginMenu() {
        System.out.println("\n===== MAIN MENU =====");
        System.out.println("1. Login");
        System.out.println("2. Register (for Patients)");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");
        String choice = scanner.nextLine().trim();

        try {
            switch (choice) {
                case "1":
                    userService.loginInteractive();
                    break;
                case "2":
                    userService.registerPatientInteractive();
                    break;
                case "3":
                    System.out.println("Exiting system. Goodbye!");
                    scanner.close();
                    HibernateUtil.shutdown(); // Gracefully shutdown Hibernate
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please select a number between 1 and 3.");
            }
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    private static void showDashboard() {
        User currentUser = SessionManager.getCurrentUser();
        System.out.printf("\nWelcome, %s! [%s]%n", currentUser.getUsername(), currentUser.getRole());

        switch (currentUser.getRole()) {
            case ADMIN:
                showAdminDashboard();
                break;
            case DOCTOR:
                showDoctorDashboard();
                break;
            case NURSE:
                showNurseDashboard();
                break;
            case RECEPTIONIST:
                showReceptionistDashboard();
                break;
            case PATIENT:
                showPatientDashboard();
                break;
        }
    }

    private static void showAdminDashboard() {
        while (SessionManager.getCurrentUser() != null) {
            System.out.println("\n===== ADMIN DASHBOARD =====");
            System.out.println("1. Manage Staff");
            System.out.println("2. Manage Patients");
            System.out.println("3. Manage Appointments");
            System.out.println("4. Manage Medical Records");
            System.out.println("5. Manage Admissions");
            System.out.println("6. Logout");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": manageStaff(); break;
                case "2": managePatients(); break;
                case "3": manageAppointments(); break;
                case "4": manageMedicalRecords(); break;
                case "5": manageAdmissions(); break;
                case "6": userService.logout(); return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void showDoctorDashboard() {
        while (SessionManager.getCurrentUser() != null) {
            System.out.println("\n===== DOCTOR DASHBOARD =====");
            System.out.println("1. Manage My Appointments");
            System.out.println("2. Manage Medical Records");
            System.out.println("3. Manage Admissions");
            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": manageAppointments(); break;
                case "2": manageMedicalRecords(); break;
                case "3": manageAdmissions(); break;
                case "4": userService.logout(); return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void showNurseDashboard() {
        while (SessionManager.getCurrentUser() != null) {
            System.out.println("\n===== NURSE DASHBOARD =====");
            System.out.println("1. View My Assigned Patients/Admissions");
            System.out.println("2. Update Medical Records of Assigned Patients");
            System.out.println("3. Logout");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": admissionService.viewAdmissions(); break;
                case "2": medicalRecordService.updateMedicalRecordInteractive(); break;
                case "3": userService.logout(); return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void showReceptionistDashboard() {
        while (SessionManager.getCurrentUser() != null) {
            System.out.println("\n===== RECEPTIONIST DASHBOARD =====");
            System.out.println("1. Register New Patient");
            System.out.println("2. Manage Appointments");
            System.out.println("3. Logout");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": patientService.addPatientInteractive(); break;
                case "2": manageAppointments(); break;
                case "3": userService.logout(); return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void showPatientDashboard() {
        while (SessionManager.getCurrentUser() != null) {
            System.out.println("\n===== PATIENT DASHBOARD =====");
            System.out.println("1. Manage My Appointments");
            System.out.println("2. View My Medical Records");
            System.out.println("3. View My Admissions");
            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": managePatientAppointments(); break;
                case "2": medicalRecordService.viewMedicalRecords(); break;
                case "3": admissionService.viewAdmissions(); break;
                case "4": userService.logout(); return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    // --- Looping Sub-menus for different management tasks ---

    private static void manageStaff() {
        while(true) {
            System.out.println("\n--- Staff Management ---");
            System.out.println("1. Add Staff | 2. Update Staff | 3. Delete Staff | 4. View All Staff | 5. Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": staffService.addStaffInteractive(); break;
                case "2": staffService.updateStaffInteractive(); break;
                case "3": staffService.deleteStaffInteractive(); break;
                case "4": staffService.viewStaff(); break;
                case "5": return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void managePatients() {
        while(true) {
            System.out.println("\n--- Patient Management ---");
            System.out.println("1. Add Patient | 2. Update Patient | 3. Delete Patient | 4. View All Patients | 5. Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": patientService.addPatientInteractive(); break;
                case "2": patientService.updatePatientInteractive(); break;
                case "3": patientService.deletePatientInteractive(); break;
                case "4": patientService.viewPatients(); break;
                case "5": return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void manageAppointments() {
        while (true) {
            System.out.println("\n--- Appointment Management ---");
            System.out.println("1. Schedule New Appointment");
            System.out.println("2. Update Appointment Status (for Doctors)");
            System.out.println("3. Reschedule or Cancel an Appointment");
            System.out.println("4. View Appointments");
            System.out.println("5. Back to Dashboard");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    appointmentService.scheduleAppointmentInteractive();
                    break;
                case "2":
                    appointmentService.updateAppointmentStatusInteractive();
                    break;
                case "3":
                    appointmentService.rescheduleOrCancelAppointmentInteractive();
                    break;
                case "4":
                    appointmentService.viewAppointments();
                    break;
                case "5":
                    return;
                default:
                    System.out.println("Invalid choice. Please select a number between 1 and 5.");
            }
        }
    }

    private static void managePatientAppointments() {
        while (true) {
            System.out.println("\n--- My Appointments ---");
            System.out.println("1. Book New Appointment");
            System.out.println("2. Reschedule or Cancel an Appointment");
            System.out.println("3. View My Appointments");
            System.out.println("4. Back to Dashboard");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    appointmentService.scheduleAppointmentInteractive();
                    break;
                case "2":
                    appointmentService.rescheduleOrCancelAppointmentInteractive();
                    break;
                case "3":
                    appointmentService.viewAppointments();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid choice. Please select a number between 1 and 4.");
            }
        }
    }

    private static void manageMedicalRecords() {
        while(true) {
            System.out.println("\n--- Medical Record Management ---");
            System.out.println("1. Add Record | 2. Update Record | 3. Delete Record | 4. View Records | 5. Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": medicalRecordService.addMedicalRecordInteractive(); break;
                case "2": medicalRecordService.updateMedicalRecordInteractive(); break;
                case "3": medicalRecordService.deleteMedicalRecordInteractive(); break;
                case "4": medicalRecordService.viewMedicalRecords(); break;
                case "5": return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void manageAdmissions() {
        while(true) {
            System.out.println("\n--- Admission Management ---");
            System.out.println("1. Admit Patient | 2. Update Admission | 3. Discharge Patient | 4. View Admissions | 5. Back");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": admissionService.admitPatientInteractive(); break;
                case "2": admissionService.updateAdmissionInteractive(); break;
                case "3": admissionService.dischargePatientInteractive(); break;
                case "4": admissionService.viewAdmissions(); break;
                case "5": return;
                default: System.out.println("Invalid choice.");
            }
        }
    }
}