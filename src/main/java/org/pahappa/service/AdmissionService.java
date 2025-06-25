package org.pahappa.service;

import org.pahappa.dao.AdmissionDao;
import org.pahappa.model.Admission;
import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
import org.pahappa.model.User;
import org.pahappa.session.SessionManager;
import org.pahappa.utils.Constants;
import org.pahappa.utils.Role;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class AdmissionService {
    private static final Scanner scanner = new Scanner(System.in);
    private final AdmissionDao admissionDao = new AdmissionDao();
    private final PatientService patientService = new PatientService();
    private final StaffService staffService = new StaffService();

    /**
     * Admits a patient. Restricted to Doctors and Admins.
     * Automatically assigns the least busy nurse.
     */
    public void admitPatient(Admission admission) {
        Role role = SessionManager.getCurrentUser().getRole();
        if (role != Role.ADMIN && role != Role.DOCTOR) {
            throw new SecurityException("Access Denied: Only Admins or Doctors can admit patients.");
        }

        // Automatically find and assign the most available nurse
        Staff assignedNurse = findAvailableNurse();
        if (assignedNurse == null) {
            System.out.println("Warning: No nurses are available. Admitting patient without an assigned nurse.");
        }
        admission.setNurse(assignedNurse);

        validateAdmission(admission);
        admissionDao.save(admission);
    }

    /**
     * Finds the nurse with the fewest active (not discharged) patients.
     * @return The Staff object of the available nurse, or null if none are found.
     */
    private Staff findAvailableNurse() {
        List<Staff> nurses = staffService.getStaffByRole(Role.NURSE);
        if (nurses.isEmpty()) {
            return null;
        }
        // Find the nurse with the minimum number of active admissions
        return nurses.stream()
                .min(Comparator.comparingLong(nurse -> admissionDao.countActiveAdmissionsByNurse(nurse.getId())))
                .orElse(null);
    }

    /**
     * Displays admissions based on the logged-in user's role.
     */
    public void viewAdmissions() {
        User currentUser = SessionManager.getCurrentUser();
        List<Admission> admissions;
        System.out.println("\n===== ADMISSIONS LIST =====");

        switch (currentUser.getRole()) {
            case PATIENT:
                admissions = admissionDao.findByPatientId(currentUser.getPatient().getId());
                System.out.println("--- Showing Your Past and Current Admissions ---");
                break;
            case NURSE:
                admissions = admissionDao.findByNurseId(currentUser.getStaff().getId());
                System.out.println("--- Showing Admissions for Patients Assigned to You ---");
                break;
            case ADMIN:
            case DOCTOR:
            case RECEPTIONIST: // Receptionists might need to view admissions too
                admissions = admissionDao.getAll();
                break;
            default:
                System.out.println("You do not have permission to view admissions.");
                return;
        }

        if (admissions.isEmpty()) {
            System.out.println("No admissions found.");
            return;
        }

        System.out.println("ID  | Patient          | Nurse            | Ward      | Ward No. | Admission Date | Discharge Date | Reason");
        System.out.println("----|------------------|------------------|-----------|----------|----------------|----------------|-------------------");
        admissions.forEach(a -> System.out.printf(
                "%-3d | %-16s | %-16s | %-9s | %-8s | %-14s | %-14s | %s%n",
                a.getId(),
                truncate(a.getPatient().getFullName(), 16),
                a.getNurse() != null ? truncate(a.getNurse().getFullName(), 16) : "N/A",
                truncate(a.getWard(), 9),
                truncate(a.getWardNumber(), 8),
                formatDate(a.getAdmissionDate()),
                formatDate(a.getDischargeDate()), // Show discharge date
                truncate(a.getReason(), 20)
        ));
    }

    /**
     * Interactive flow for admitting a patient.
     */
    public void admitPatientInteractive() {
        Role role = SessionManager.getCurrentUser().getRole();
        if (role != Role.ADMIN && role != Role.DOCTOR) {
            System.out.println("Access Denied.");
            return;
        }

        try {
            System.out.println("\n===== ADMIT PATIENT =====");
            patientService.viewPatients();
            Long patientId = getLongInput("\nEnter Patient ID to admit: ");
            Patient patient = patientService.getPatient(patientId);

            // Admission date defaults to the current time
            Date admissionDate = new Date(System.currentTimeMillis());
            String reason = getRequiredInput("\nReason for admission: ", Constants.ERROR_REQUIRED_FIELD);
            String ward = getRequiredInput("Enter Ward (e.g., Maternity, ICU): ", Constants.ERROR_REQUIRED_FIELD);
            String wardNumber = getRequiredInput("Enter Ward/Bed Number: ", Constants.ERROR_REQUIRED_FIELD);

            Admission admission = new Admission();
            admission.setPatient(patient);
            admission.setAdmissionDate(admissionDate);
            admission.setReason(reason);
            admission.setWard(ward);
            admission.setWardNumber(wardNumber);

            admitPatient(admission);
            System.out.println("\nPatient admitted successfully!");
            if (admission.getNurse() != null) {
                System.out.println("Assigned Nurse: " + admission.getNurse().getFullName());
            }

        } catch (Exception e) {
            System.err.println("\nError admitting patient: " + e.getMessage());
        }
    }

    public void updateAdmissionInteractive() {
        Role role = SessionManager.getCurrentUser().getRole();
        if (role != Role.ADMIN && role != Role.DOCTOR && role != Role.NURSE) {
            System.out.println("Access Denied.");
            return;
        }

        try {
            viewAdmissions();
            Long id = getLongInput("\nEnter Admission ID to update: ");
            Admission admission = admissionDao.getById(id);
            if(admission == null) {
                System.out.println("Admission not found.");
                return;
            }

            // A nurse can only update ward details, not discharge a patient.
            if(role == Role.NURSE){
                String newWard = getInputWithDefault("Update Ward [" + admission.getWard() + "]: ", admission.getWard());
                String newWardNumber = getInputWithDefault("Update Ward/Bed Number [" + admission.getWardNumber() + "]: ", admission.getWardNumber());
                admission.setWard(newWard);
                admission.setWardNumber(newWardNumber);
            }

            // Doctors/Admins can update everything, including discharging a patient.
            if(role == Role.DOCTOR || role == Role.ADMIN){
                String dischargeDateStr = getOptionalInput(String.format("Enter Discharge Date (%s) (leave blank if not discharged): ", Constants.DATE_FORMAT));
                if (!dischargeDateStr.isEmpty()) {
                    admission.setDischargeDate(new Date(new SimpleDateFormat(Constants.DATE_FORMAT).parse(dischargeDateStr).getTime()));
                }
            }

            admissionDao.update(admission);
            System.out.println("Admission record updated successfully.");

        } catch(Exception e){
            System.err.println("Error updating admission: " + e.getMessage());
        }
    }

    public void deleteAdmissionInteractive() {
        Role role = SessionManager.getCurrentUser().getRole();
        if (role != Role.ADMIN && role != Role.DOCTOR) {
            System.out.println("Access Denied. Only Admins or Doctors can discharge a patient.");
            return;
        }
        try {
            viewAdmissions();
            Long id = getLongInput("\nEnter Admission ID to discharge (delete): ");
            System.out.print("\nAre you sure you want to discharge this patient? This will remove the admission record. (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                admissionDao.delete(id);
                System.out.println("\nPatient discharged successfully.");
            } else {
                System.out.println("\nDischarge cancelled.");
            }
        } catch(Exception e){
            System.err.println("Error discharging patient: " + e.getMessage());
        }
    }

    // --- Helper and Validation Methods ---
    private void validateAdmission(Admission admission) {
        if (admission == null) throw new IllegalArgumentException("Admission cannot be null.");
        if (admission.getPatient() == null) throw new IllegalArgumentException("Patient is required.");
        if (admission.getAdmissionDate() == null) throw new IllegalArgumentException("Admission date is required.");
        if (admission.getReason() == null || admission.getReason().trim().isEmpty()) throw new IllegalArgumentException("Reason is required.");
        if (admission.getWard() == null || admission.getWard().trim().isEmpty()) throw new IllegalArgumentException("Ward is required.");
        if (admission.getWardNumber() == null || admission.getWardNumber().trim().isEmpty()) throw new IllegalArgumentException("Ward number is required.");
    }
    private String formatDate(Date date) {
        if (date == null) return "N/A";
        return new SimpleDateFormat(Constants.DATE_FORMAT).format(date);
    }
    private String truncate(String str, int length) {
        if (str == null) return "N/A";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
    private String getRequiredInput(String prompt, String errorMessage) {
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) System.out.println(errorMessage);
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
                System.out.println("Invalid ID. Please enter a number.");
            }
        }
    }

    public void dischargePatientInteractive() {
        Role role = SessionManager.getCurrentUser().getRole();
        if (role != Role.ADMIN && role != Role.DOCTOR) {
            System.out.println("Access Denied. Only Admins or Doctors can discharge a patient.");
            return;
        }
        try {
            System.out.println("\n--- Active Admissions (Not Discharged) ---");
            // We need a way to view only active admissions to make this list useful
            List<Admission> activeAdmissions = admissionDao.getAll().stream()
                    .filter(a -> a.getDischargeDate() == null)
                    .toList();

            if (activeAdmissions.isEmpty()) {
                System.out.println("No active admissions to discharge.");
                return;
            }

            System.out.println("ID  | Patient          | Nurse            | Admission Date");
            System.out.println("----|------------------|------------------|----------------");
            activeAdmissions.forEach(a -> System.out.printf(
                    "%-3d | %-16s | %-16s | %s%n",
                    a.getId(),
                    truncate(a.getPatient().getFullName(), 16),
                    a.getNurse() != null ? truncate(a.getNurse().getFullName(), 16) : "N/A",
                    formatDate(a.getAdmissionDate())
            ));

            Long id = getLongInput("\nEnter Admission ID to discharge: ");
            Admission admission = admissionDao.getById(id);
            if (admission == null) {
                System.out.println("Admission not found.");
                return;
            }

            // Set discharge date to now
            admission.setDischargeDate(new java.sql.Date(System.currentTimeMillis()));
            admissionDao.update(admission);

            System.out.println("Patient " + admission.getPatient().getFullName() + " has been discharged successfully.");

        } catch(Exception e){
            System.err.println("Error discharging patient: " + e.getMessage());
        }
    }
}