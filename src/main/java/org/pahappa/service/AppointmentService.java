package org.pahappa.service;

import org.pahappa.dao.AppointmentDao;
import org.pahappa.model.Appointment;
import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
import org.pahappa.utils.Constants;
import org.pahappa.utils.Role;
import org.hibernate.Session;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
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

    // New validation methods
    private boolean isDoctorAvailable(Staff doctor, Timestamp appointmentTime) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Appointment WHERE doctor = :doctor AND appointmentDate = :appointmentTime";
            List<Appointment> existing = session.createQuery(hql, Appointment.class)
                    .setParameter("doctor", doctor)
                    .setParameter("appointmentTime", appointmentTime)
                    .list();
            return existing.isEmpty();
        }
    }

    private boolean isValidAppointmentTime(Timestamp appointmentTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(appointmentTime);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        // Only allow appointments on the hour or half-hour
        if (minute != 0 && minute != 30) {
            return false;
        }

        return hour >= 8 && hour <= 17; // 8am to 5pm
    }

    // Updated validation method
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
        if (!isDoctorAvailable(appointment.getDoctor(), appointment.getAppointmentDate())) {
            throw new IllegalArgumentException("Doctor is already booked at this time");
        }
        if (!isValidAppointmentTime(appointment.getAppointmentDate())) {
            throw new IllegalArgumentException("Appointments must be at :00 or :30 during 8am-5pm");
        }

        // Ensure appointment is at least 15 minutes from now
        long diff = appointment.getAppointmentDate().getTime() - System.currentTimeMillis();
        if (diff < (15 * 60 * 1000)) {
            throw new IllegalArgumentException("Appointments must be scheduled at least 15 minutes in advance");
        }
    }

    // Updated interactive methods
    public void scheduleAppointmentInteractive() {
        try {
            System.out.println("\n===== SCHEDULE NEW APPOINTMENT =====");

            // Show available patients
            System.out.println("\n--- Available Patients ---");
            List<Patient> patients = patientService.getAllPatients();
            if (patients.isEmpty()) {
                System.out.println("No patients available. Add a patient first!");
                return;
            }
            patients.forEach(p -> System.out.printf("ID: %d | Name: %s | Email: %s%n",
                    p.getId(), p.getFullName(), p.getEmail()));

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

            Long doctorId = getLongInput("\nEnter Doctor ID: ");
            Staff doctor = staffService.getStaff(doctorId);
            if (doctor.getRole() != Role.DOCTOR) {
                System.out.println(Constants.ERROR_INVALID_DOCTOR);
                return;
            }

            // Get appointment time
            String dateStr = getRequiredInput(String.format(
                            "\nEnter Appointment Date (%s): ", Constants.TIMESTAMP_FORMAT),
                    Constants.ERROR_REQUIRED_FIELD);
            Timestamp appointmentDate = parseTimestamp(dateStr);
            if (appointmentDate == null) {
                System.out.println(Constants.ERROR_INVALID_TIMESTAMP);
                return;
            }

            // Get reason
            String reason = getRequiredInput("\nEnter Reason: ", Constants.ERROR_REQUIRED_FIELD);
            if (reason.length() > Constants.MAX_REASON_LENGTH) {
                System.out.println(Constants.ERROR_REASON_TOO_LONG);
                return;
            }

            // Create and validate appointment
            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setDoctor(doctor);
            appointment.setAppointmentDate(appointmentDate);
            appointment.setReason(reason);

            scheduleAppointment(appointment);
            System.out.println("\nAppointment scheduled successfully!");
        } catch (Exception e) {
            System.out.println("\nError scheduling appointment: " + e.getMessage());
        }
    }

    public void updateAppointmentInteractive() {
        try {
            System.out.println("\n===== UPDATE APPOINTMENT =====");

            // Show all appointments
            System.out.println("\n--- Current Appointments ---");
            List<Appointment> appointments = getAllAppointments();
            if (appointments.isEmpty()) {
                System.out.println("No appointments available!");
                return;
            }
            appointments.forEach(a -> System.out.printf(
                    "ID: %d | Patient: %s | Doctor: %s | Time: %s | Reason: %s%n",
                    a.getId(), a.getPatient().getFullName(), a.getDoctor().getFullName(),
                    formatTimestamp(a.getAppointmentDate()), a.getReason()));

            Long id = getLongInput("\nEnter Appointment ID to update: ");
            Appointment appointment = getAppointment(id);

            // Show current details
            System.out.println("\n--- Current Appointment Details ---");
            System.out.println("1. Patient: " + appointment.getPatient().getFullName());
            System.out.println("2. Doctor: " + appointment.getDoctor().getFullName());
            System.out.println("3. Appointment Time: " + formatTimestamp(appointment.getAppointmentDate()));
            System.out.println("4. Reason: " + appointment.getReason());

            // Get updates
            System.out.println("\n--- Update Fields (press Enter to keep current value) ---");

            // Update patient
            System.out.println("\nAvailable Patients:");
            List<Patient> patients = patientService.getAllPatients();
            patients.forEach(p -> System.out.printf("ID: %d | Name: %s%n", p.getId(), p.getFullName()));
            String patientInput = getInputWithDefault(
                    "Enter new Patient ID [" + appointment.getPatient().getId() + "]: ",
                    appointment.getPatient().getId().toString());
            if (!patientInput.isEmpty()) {
                appointment.setPatient(patientService.getPatient(Long.parseLong(patientInput)));
            }

            // Update doctor
            System.out.println("\nAvailable Doctors:");
            List<Staff> doctors = staffService.getAllStaff().stream()
                    .filter(s -> s.getRole() == Role.DOCTOR)
                    .toList();
            doctors.forEach(d -> System.out.printf("ID: %d | Name: %s | Specialty: %s%n",
                    d.getId(), d.getFullName(), d.getSpecialty()));
            String doctorInput = getInputWithDefault(
                    "Enter new Doctor ID [" + appointment.getDoctor().getId() + "]: ",
                    appointment.getDoctor().getId().toString());
            if (!doctorInput.isEmpty()) {
                Staff doctor = staffService.getStaff(Long.parseLong(doctorInput));
                if (doctor.getRole() != Role.DOCTOR) {
                    System.out.println(Constants.ERROR_INVALID_DOCTOR);
                    return;
                }
                appointment.setDoctor(doctor);
            }

            // Update time
            String timeInput = getInputWithDefault(
                    String.format("Enter new Appointment Time (%s) [%s]: ",
                            Constants.TIMESTAMP_FORMAT, formatTimestamp(appointment.getAppointmentDate())),
                    formatTimestamp(appointment.getAppointmentDate()));
            if (!timeInput.isEmpty()) {
                Timestamp newTime = parseTimestamp(timeInput);
                if (newTime != null) {
                    appointment.setAppointmentDate(newTime);
                }
            }

            // Update reason
            String reason = getInputWithDefault(
                    "Enter new Reason [" + appointment.getReason() + "]: ",
                    appointment.getReason());
            if (!reason.isEmpty()) {
                appointment.setReason(reason);
            }

            updateAppointment(appointment);
            System.out.println("\nAppointment updated successfully!");
        } catch (Exception e) {
            System.out.println("\nError updating appointment: " + e.getMessage());
        }
    }

    public void deleteAppointmentInteractive() {
        try {
            System.out.println("\n===== DELETE APPOINTMENT =====");

            // Show all appointments
            System.out.println("\n--- Current Appointments ---");
            List<Appointment> appointments = getAllAppointments();
            if (appointments.isEmpty()) {
                System.out.println("No appointments available!");
                return;
            }
            appointments.forEach(a -> System.out.printf(
                    "ID: %d | Patient: %s | Doctor: %s | Time: %s%n",
                    a.getId(), a.getPatient().getFullName(), a.getDoctor().getFullName(),
                    formatTimestamp(a.getAppointmentDate())));

            Long id = getLongInput("\nEnter Appointment ID to delete: ");

            // Confirm deletion
            System.out.print("\nAre you sure you want to delete this appointment? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                deleteAppointment(id);
                System.out.println("\nAppointment deleted successfully!");
            } else {
                System.out.println("\nDeletion cancelled.");
            }
        } catch (Exception e) {
            System.out.println("\nError deleting appointment: " + e.getMessage());
        }
    }

    public void viewAppointments() {
        System.out.println("\n===== APPOINTMENT LIST =====");
        List<Appointment> appointments = getAllAppointments();
        if (appointments.isEmpty()) {
            System.out.println("No appointments found.");
        } else {
            System.out.println("ID  | Patient          | Doctor           | Date/Time         | Reason");
            System.out.println("----|------------------|------------------|-------------------|-------------------");
            appointments.forEach(a -> System.out.printf(
                    "%-3d | %-16s | %-16s | %-17s | %s%n",
                    a.getId(),
                    truncate(a.getPatient().getFullName(), 16),
                    truncate(a.getDoctor().getFullName(), 16),
                    formatTimestamp(a.getAppointmentDate()),
                    truncate(a.getReason(), 20)));
        }
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        return new SimpleDateFormat(Constants.TIMESTAMP_FORMAT).format(timestamp);
    }

    private String truncate(String str, int length) {
        if (str == null) return "";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }

    private String getInputWithDefault(String prompt, String defaultValue) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
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