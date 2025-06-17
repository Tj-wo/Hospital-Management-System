package org.pahappa.service;

import org.pahappa.dao.AppointmentDao;
import org.pahappa.model.Appointment;
import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

public class AppointmentService {
    private final AppointmentDao appointmentDao = new AppointmentDao();
    private static final Scanner scanner = new Scanner(System.in);
    private final PatientService patientService = new PatientService();
    private final StaffService staffService = new StaffService();

    public void bookAppointment(Appointment appointment) {
        appointmentDao.save(appointment);
    }

    public Appointment getAppointment(Long id) {
        return appointmentDao.getById(id);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentDao.getAll();
    }

    public List<Appointment> getDoctorSchedule(Long doctorId) {
        return appointmentDao.getByDoctorId(doctorId);
    }

    public void rescheduleAppointment(Long id, Timestamp newDate) {
        Appointment appointment = getAppointment(id);
        if (appointment != null) {
            appointment.setAppointmentDate(newDate);
            appointmentDao.update(appointment);
        }
    }

    public void cancelAppointment(Long id) {
        appointmentDao.delete(id);
    }

    public boolean isDoctorAvailable(Long doctorId, Timestamp date) {
        return appointmentDao.countByDoctorAndDate(doctorId, date) == 0;
    }

    public void addAppointmentInteractive() {
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

            String dateStr = getStringInput("Enter Appointment Date (yyyy-mm-dd HH:mm:ss): ");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setLenient(false);
            java.util.Date utilDate = sdf.parse(dateStr);
            if (utilDate.before(new java.util.Date())) {
                System.out.println("Appointment date cannot be in the past!");
                return;
            }
            Timestamp appointmentDate = new Timestamp(utilDate.getTime());

            String reason = getStringInput("Enter Reason: ");
            if (reason.isEmpty()) {
                System.out.println("Reason is required!");
                return;
            }

            Patient patient = patientService.getPatient(patientId);
            Staff doctor = staffService.getStaff(doctorId);
            if (patient == null || doctor == null) {
                System.out.println("Invalid Patient or Doctor ID!");
                return;
            }

            Appointment appointment = new Appointment(patient, doctor, appointmentDate, reason);
            bookAppointment(appointment);
            System.out.println("Appointment added successfully!");
        } catch (ParseException e) {
            System.out.println("Invalid date format! Use yyyy-mm-dd HH:mm:ss.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format!");
        } catch (Exception e) {
            System.out.println("Error adding appointment: " + e.getMessage());
        }
    }

    public void viewAppointments() {
        System.out.println("Appointments:");
        getAllAppointments().forEach(System.out::println);
        if (getAllAppointments().isEmpty()) {
            System.out.println("No appointments found.");
        }
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}