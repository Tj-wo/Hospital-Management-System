package org.pahappa.service;

import org.hibernate.Session;
import org.pahappa.dao.AppointmentDao;
import org.pahappa.model.*;
import org.pahappa.session.SessionManager;
import org.pahappa.utils.AppointmentStatus;
import org.pahappa.utils.Constants;
import org.pahappa.utils.Role;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class AppointmentService {
    private static final Scanner scanner = new Scanner(System.in);
    private final AppointmentDao appointmentDao = new AppointmentDao();
    private final PatientService patientService = new PatientService();
    private final StaffService staffService = new StaffService();

    // --- Core Business Logic Methods ---

    public void scheduleAppointment(Appointment appointment) {
        validateAppointment(appointment, null); // Pass null for new appointments
        appointment.setStatus(AppointmentStatus.SCHEDULED); // Ensure default status is set
        appointmentDao.save(appointment);
    }

    public void updateAppointment(Appointment appointment, Long appointmentIdToIgnore) {
        validateAppointment(appointment, appointmentIdToIgnore); // Pass the ID to ignore during validation
        appointmentDao.update(appointment);
    }

    public void deleteAppointment(Long id){
        // Note: For a real system, changing status to CANCELLED is often better than deleting.
        // For this project, a hard delete is acceptable as per the flow.
        appointmentDao.delete(id);
    }

    // --- Interactive and View Methods ---

    /**
     * Displays appointments based on the logged-in user's role, now including the status.
     */
    public void viewAppointments() {
        User currentUser = SessionManager.getCurrentUser();
        List<Appointment> appointments;
        System.out.println("\n===== APPOINTMENT LIST =====");

        switch (currentUser.getRole()) {
            case PATIENT:
                appointments = appointmentDao.findByPatientId(currentUser.getPatient().getId());
                break;
            case DOCTOR:
                appointments = appointmentDao.findByDoctorId(currentUser.getStaff().getId());
                break;
            case ADMIN:
            case RECEPTIONIST:
                appointments = appointmentDao.getAll();
                break;
            default:
                System.out.println("You do not have permission to view appointments.");
                return;
        }

        if (appointments.isEmpty()) {
            System.out.println("No appointments found.");
        } else {
            System.out.println("ID  | Patient          | Doctor           | Date/Time         | Status                  | Reason");
            System.out.println("----|------------------|------------------|-------------------|-------------------------|-------------------");
            appointments.forEach(a -> System.out.printf(
                    "%-3d | %-16s | %-16s | %-17s | %-23s | %s%n",
                    a.getId(),
                    truncate(a.getPatient().getFullName(), 16),
                    truncate(a.getDoctor().getFullName(), 16),
                    formatTimestamp(a.getAppointmentDate()),
                    a.getStatus(),
                    truncate(a.getReason(), 20)));
        }
    }

    /**
     * Interactive flow for scheduling a NEW appointment.
     */
    public void scheduleAppointmentInteractive() {
        User currentUser = SessionManager.getCurrentUser();
        Role role = currentUser.getRole();

        try {
            System.out.println("\n===== SCHEDULE NEW APPOINTPOINTMENT =====");
            Patient patient;

            if (role == Role.PATIENT) {
                patient = currentUser.getPatient();
                System.out.println("Scheduling appointment for yourself: " + patient.getFullName());
            } else if (role == Role.ADMIN || role == Role.RECEPTIONIST || role == Role.DOCTOR) {
                patientService.viewPatients();
                patient = null;
                while (patient == null) {
                    Long patientId = getLongInput("\nEnter Patient ID: ");
                    try {
                        patient = patientService.getPatient(patientId);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage() + ". Please try again.");
                    }
                }
            } else {
                System.out.println("Access Denied.");
                return;
            }

            System.out.println("\n--- Available Doctors ---");
            staffService.getStaffByRole(Role.DOCTOR).forEach(d -> System.out.printf("ID: %d | Name: %s | Specialty: %s%n", d.getId(), d.getFullName(), d.getSpecialty()));
            Staff doctor = null;
            while (doctor == null) {
                Long doctorId = getLongInput("\nEnter Doctor ID: ");
                try {
                    Staff selectedStaff = staffService.getStaff(doctorId);
                    if (selectedStaff.getRole() != Role.DOCTOR) {
                        System.out.println("Error: The selected staff member is not a doctor.");
                    } else {
                        doctor = selectedStaff;
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage() + ". Please try again.");
                }
            }

            Timestamp appointmentDate = null;
            while (appointmentDate == null) {
                String dateStr = getRequiredInput(String.format("\nEnter Appointment Date & Time (%s): ", Constants.TIMESTAMP_FORMAT), Constants.ERROR_REQUIRED_FIELD);
                appointmentDate = parseTimestamp(dateStr);
                if (appointmentDate == null) {
                    System.out.println(String.format("Invalid format. Please use: %s", Constants.TIMESTAMP_FORMAT));
                }
            }

            String reason = getRequiredInput("\nEnter Reason for Appointment: ", Constants.ERROR_REQUIRED_FIELD);

            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setDoctor(doctor);
            appointment.setAppointmentDate(appointmentDate);
            appointment.setReason(reason);

            scheduleAppointment(appointment);
            System.out.println("\nAppointment scheduled successfully!");

        } catch (Exception e) {
            System.err.println("\nError scheduling appointment: " + e.getMessage());
        }
    }

    /**
     * Interactive flow for a DOCTOR to update the status of an appointment.
     */
    public void updateAppointmentStatusInteractive() {
        if (SessionManager.getCurrentUser().getRole() != Role.DOCTOR) {
            System.out.println("Access Denied: Only doctors can update appointment statuses.");
            return;
        }

        try {
            viewAppointments();
            Long id = getLongInput("\nEnter Appointment ID to update status: ");
            Appointment appointment = appointmentDao.getById(id);
            if (appointment == null) {
                System.out.println("Appointment not found.");
                return;
            }

            System.out.println("Current status: " + appointment.getStatus());
            System.out.println("Available statuses: " + Arrays.toString(AppointmentStatus.values()));

            AppointmentStatus newStatus = null;
            while (newStatus == null) {
                String statusStr = getRequiredInput("Enter new status: ", "Status cannot be empty.").toUpperCase();
                try {
                    newStatus = AppointmentStatus.valueOf(statusStr);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid status. Please choose from the available list.");
                }
            }

            appointment.setStatus(newStatus);
            appointmentDao.update(appointment);
            System.out.println("Appointment status updated successfully!");

        } catch (Exception e) {
            System.err.println("Error updating status: " + e.getMessage());
        }
    }

    /**
     * Interactive flow for rescheduling or cancelling an appointment.
     */
    public void rescheduleOrCancelAppointmentInteractive() {
        try {
            viewAppointments();
            Long id = getLongInput("\nEnter Appointment ID to reschedule or cancel: ");
            Appointment appointment = appointmentDao.getById(id);
            if (appointment == null) {
                System.out.println("Appointment not found.");
                return;
            }

            User currentUser = SessionManager.getCurrentUser();
            if (currentUser.getRole() == Role.PATIENT && !appointment.getPatient().getId().equals(currentUser.getPatient().getId())) {
                System.out.println("Access Denied: You can only manage your own appointments.");
                return;
            }

            System.out.println("\nWhat would you like to do?");
            System.out.println("1. Reschedule (change time/reason)");
            System.out.println("2. Cancel Appointment");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();

            if ("1".equals(choice)) {
                System.out.println("\n--- Rescheduling Appointment (press Enter to keep current value) ---");

                Timestamp newTime = null;
                while (newTime == null) {
                    String timeInput = getInputWithDefault(String.format("New Date/Time [%s]: ", formatTimestamp(appointment.getAppointmentDate())), formatTimestamp(appointment.getAppointmentDate()));
                    newTime = parseTimestamp(timeInput);
                    if (newTime == null) {
                        System.out.println("Invalid date format. Please try again.");
                    }
                }

                String newReason = getInputWithDefault(String.format("New Reason [%s]: ", appointment.getReason()), appointment.getReason());

                appointment.setAppointmentDate(newTime);
                appointment.setReason(newReason);

                updateAppointment(appointment, appointment.getId());
                System.out.println("Appointment rescheduled successfully!");

            } else if ("2".equals(choice)) {
                if (currentUser.getRole() == Role.PATIENT) {
                    appointment.setStatus(AppointmentStatus.CANCELLED_BY_PATIENT);
                } else {
                    appointment.setStatus(AppointmentStatus.CANCELLED_BY_DOCTOR);
                }
                appointmentDao.update(appointment);
                System.out.println("Appointment has been cancelled.");
            } else {
                System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            System.err.println("Error managing appointment: " + e.getMessage());
        }
    }

    // --- Validation and Helper Methods ---

    /**
     * Validates an appointment, ignoring a specific ID for availability checks (for updates).
     */
    private void validateAppointment(Appointment appointment, Long idToIgnore) {
        if (appointment == null) throw new IllegalArgumentException("Appointment cannot be null");
        if (appointment.getPatient() == null) throw new IllegalArgumentException("Patient is required");
        if (appointment.getDoctor() == null) throw new IllegalArgumentException("Doctor is required");
        if (appointment.getDoctor().getRole() != Role.DOCTOR) throw new IllegalArgumentException("Assigned staff must be a DOCTOR");
        if (appointment.getAppointmentDate() == null) throw new IllegalArgumentException("Appointment date is required");
        if (appointment.getAppointmentDate().before(new Timestamp(System.currentTimeMillis()))) throw new IllegalArgumentException("Appointment cannot be in the past.");
        if (!isDoctorAvailable(appointment.getDoctor(), appointment.getAppointmentDate(), idToIgnore)) {
            throw new IllegalArgumentException("Doctor is already booked at this time for another 'SCHEDULED' appointment.");
        }
    }

    /**
     * Checks if a doctor has another 'SCHEDULED' appointment at a specific time, optionally ignoring an ID.
     */
    private boolean isDoctorAvailable(Staff doctor, Timestamp appointmentTime, Long idToIgnore) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Appointment WHERE doctor = :doctor AND appointmentDate = :appointmentTime AND status = 'SCHEDULED'";

            if (idToIgnore != null) {
                hql += " AND id != :idToIgnore";
            }

            var query = session.createQuery(hql, Appointment.class)
                    .setParameter("doctor", doctor)
                    .setParameter("appointmentTime", appointmentTime);

            if (idToIgnore != null) {
                query.setParameter("idToIgnore", idToIgnore);
            }

            return query.list().isEmpty();
        }
    }

    private String formatTimestamp(Timestamp ts) {
        return ts == null ? "N/A" : new SimpleDateFormat(Constants.TIMESTAMP_FORMAT).format(ts);
    }

    private Timestamp parseTimestamp(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.TIMESTAMP_FORMAT);
            sdf.setLenient(false);
            return new Timestamp(sdf.parse(str).getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    private String truncate(String str, int len) {
        if (str == null) return "";
        return str.length() > len ? str.substring(0, len - 3) + "..." : str;
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
}