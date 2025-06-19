package org.pahappa.service;

import org.pahappa.dao.AppointmentDao;
import org.pahappa.model.Appointment;
import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
import org.pahappa.utils.Constants;
import org.pahappa.utils.Role;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

// Service class to manage appointment operations
public class AppointmentService {
    private static final Scanner scanner = new Scanner(System.in);
    private final AppointmentDao appointmentDao = new AppointmentDao();
    private final PatientService patientService = new PatientService();
    private final StaffService staffService = new StaffService();

    // Schedule a new appointment
    public void scheduleAppointment(Appointment appointment) {
        validateAppointment(appointment);
        appointmentDao.save(appointment);
    }

    // Get an appointment by ID
    public Appointment getAppointment(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
        }
        Appointment appointment = appointmentDao.getById(id);
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment not found with ID: " + id);
        }
        return appointment;
    }

    // Get all appointments
    public List<Appointment> getAllAppointments() {
        return appointmentDao.getAll();
    }

    // Update an appointment
    public void updateAppointment(Appointment appointment) {
        if (appointment.getId() == null) {
            throw new IllegalArgumentException("Appointment ID is required for update");
        }
        validateAppointment(appointment);
        appointmentDao.update(appointment);
    }

    // Delete an appointment by ID
    public void deleteAppointment(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_ID);
        }
        appointmentDao.delete(id);
    }

    // Interactive method to schedule an appointment
    public void scheduleAppointmentInteractive() {
        try {
            List<Patient> patients = patientService.getAllPatients();
            if (patients.isEmpty()) {
                System.out.println("No patients available. Add a patient first!");
                return;
            }
            System.out.println("\nAvailable Patients:");
            patients.forEach(p -> System.out.printf("%d: %s%n", p.getId(), p.getFullName()));
            Long patientId = getLongInput("Enter Patient ID: ");
            Patient patient = patientService.getPatient(patientId);

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
                System.out.println(Constants.ERROR_INVALID_DOCTOR);
                return;
            }

            String dateStr = getRequiredInput("Enter Appointment Date (" + Constants.TIMESTAMP_FORMAT + "): ", Constants.ERROR_REQUIRED_FIELD);
            Timestamp appointmentDate = parseTimestamp(dateStr);
            if (appointmentDate == null) {
                System.out.println(Constants.ERROR_INVALID_TIMESTAMP);
                return;
            }
            if (appointmentDate.before(new Timestamp(System.currentTimeMillis()))) {
                System.out.println(Constants.ERROR_PAST_APPOINTMENT);
                return;
            }

            String reason = getRequiredInput("Enter Reason: ", Constants.ERROR_REQUIRED_FIELD);
            if (reason.length() > Constants.MAX_REASON_LENGTH) {
                System.out.println(Constants.ERROR_REASON_TOO_LONG);
                return;
            }

            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setDoctor(doctor);
            appointment.setAppointmentDate(appointmentDate);
            appointment.setReason(reason);
            scheduleAppointment(appointment);
            System.out.println("Appointment scheduled successfully!");
        } catch (Exception e) {
            System.out.println("Error scheduling appointment: " + e.getMessage());
        }
    }

    // Interactive method to update an appointment
    public void updateAppointmentInteractive() {
        try {
            Long id = getLongInput("Enter Appointment ID to update: ");
            Appointment appointment = getAppointment(id);
            System.out.println("Current details:\n" + appointment);

            List<Patient> patients = patientService.getAllPatients();
            if (patients.isEmpty()) {
                System.out.println("No patients available!");
                return;
            }
            System.out.println("\nAvailable Patients:");
            patients.forEach(p -> System.out.printf("%d: %s%n", p.getId(), p.getFullName()));
            Long patientId = getLongInput("Enter new Patient ID [" + appointment.getPatient().getId() + "]: ");
            Patient patient = patientService.getPatient(patientId);

            List<Staff> doctors = staffService.getAllStaff().stream()
                    .filter(s -> s.getRole() == Role.DOCTOR)
                    .toList();
            if (doctors.isEmpty()) {
                System.out.println("No doctors available!");
                return;
            }
            System.out.println("\nAvailable Doctors:");
            doctors.forEach(d -> System.out.printf("%d: %s %s (%s)%n", d.getId(), d.getFirstName(), d.getLastName(), d.getSpecialty()));
            Long doctorId = getLongInput("Enter new Doctor ID [" + appointment.getDoctor().getId() + "]: ");
            Staff doctor = staffService.getStaff(doctorId);
            if (doctor.getRole() != Role.DOCTOR) {
                System.out.println(Constants.ERROR_INVALID_DOCTOR);
                return;
            }

            String dateStr = getRequiredInput("Enter new Appointment Date (" + Constants.TIMESTAMP_FORMAT + ") [" + appointment.getAppointmentDate() + "]: ", appointment.getAppointmentDate().toString());
            Timestamp appointmentDate = parseTimestamp(dateStr);
            if (appointmentDate == null) {
                System.out.println(Constants.ERROR_INVALID_TIMESTAMP);
                return;
            }
            if (appointmentDate.before(new Timestamp(System.currentTimeMillis()))) {
                System.out.println(Constants.ERROR_PAST_APPOINTMENT);
                return;
            }

            String reason = getRequiredInput("Enter new Reason [" + appointment.getReason() + "]: ", appointment.getReason());
            if (reason.length() > Constants.MAX_REASON_LENGTH) {
                System.out.println(Constants.ERROR_REASON_TOO_LONG);
                return;
            }

            appointment.setPatient(patient);
            appointment.setDoctor(doctor);
            appointment.setAppointmentDate(appointmentDate);
            appointment.setReason(reason);
            updateAppointment(appointment);
            System.out.println("Appointment updated successfully!");
        } catch (Exception e) {
            System.out.println("Error updating appointment: " + e.getMessage());
        }
    }

    // Interactive method to delete an appointment
    public void deleteAppointmentInteractive() {
        try {
            Long id = getLongInput("Enter Appointment ID to delete: ");
            Appointment appointment = getAppointment(id);
            System.out.println("Appointment to delete:\n" + appointment);
            System.out.print("Are you sure? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                deleteAppointment(id);
                System.out.println("Appointment deleted successfully!");
            }
        } catch (Exception e) {
            System.out.println("Error deleting appointment: " + e.getMessage());
        }
    }

    // Interactive method to view all appointments
    public void viewAppointments() {
        List<Appointment> appointments = getAllAppointments();
        System.out.println("\n===== APPOINTMENTS =====");
        if (appointments.isEmpty()) {
            System.out.println("No appointments found.");
        } else {
            appointments.forEach(System.out::println);
        }
    }

    // Validate appointment data
    private void validateAppointment(Appointment appointment) {
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment cannot be null");
        }
        if (appointment.getPatient() == null) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Patient)");
        }
        if (appointment.getDoctor() == null) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Doctor)");
        }
        if (appointment.getAppointmentDate() == null) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Appointment Date)");
        }
        if (appointment.getReason() == null || appointment.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException(Constants.ERROR_REQUIRED_FIELD + " (Reason)");
        }

        if (appointment.getDoctor().getRole() != Role.DOCTOR) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_DOCTOR);
        }
        if (appointment.getReason().length() > Constants.MAX_REASON_LENGTH) {
            throw new IllegalArgumentException(Constants.ERROR_REASON_TOO_LONG);
        }
        if (appointment.getAppointmentDate().before(new Timestamp(System.currentTimeMillis()))) {
            throw new IllegalArgumentException(Constants.ERROR_PAST_APPOINTMENT);
        }
    }

    // Parse timestamp string
    private Timestamp parseTimestamp(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.TIMESTAMP_FORMAT);
            sdf.setLenient(false);
            return new Timestamp(sdf.parse(dateStr).getTime());
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