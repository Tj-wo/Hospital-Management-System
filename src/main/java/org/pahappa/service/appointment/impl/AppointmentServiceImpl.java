package org.pahappa.service.appointment.impl;

import org.pahappa.dao.AppointmentDao;
import org.pahappa.model.Appointment;
import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
import org.pahappa.model.User;
import org.pahappa.service.audit.AuditService;
import org.pahappa.service.audit.impl.AuditServiceImpl;
import org.pahappa.service.appointment.AppointmentService;
import org.pahappa.utils.AppointmentStatus;
import org.pahappa.utils.Role;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.pahappa.controller.LoginBean;


@ApplicationScoped
public class AppointmentServiceImpl implements AppointmentService {

    @Inject
    private AppointmentDao appointmentDao;

    @Inject
    private AuditService auditService;

    // Inject the LoginBean
    @Inject
    private LoginBean loginBean;

    // Helper method to get the current username from the LoginBean
    private String getCurrentUser() {
        if (loginBean != null && loginBean.getLoggedInUser() != null) {
            return loginBean.getLoggedInUser().getUsername();
        }
        return "system"; // Fallback for background tasks or unauthenticated operations
    }

    // Helper method to get the current user ID from the LoginBean
    private String getCurrentUserId() {
        if (loginBean != null && loginBean.getLoggedInUser() != null && loginBean.getLoggedInUser().getId() != null) {
            return loginBean.getLoggedInUser().getId().toString();
        }
        return "0"; // Default ID for system or unauthenticated actions
    }


    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentDao.getAll();
    }

    @Override
    public Appointment getAppointmentById(Long appointmentId) {
        return appointmentDao.getById(appointmentId);
    }

    @Override
    public void scheduleAppointment(Appointment appointment) {
        validateAppointment(appointment, null);
        appointment.setDeleted(false);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentDao.save(appointment);
        String details = "Patient: " + appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName() +
                ", Doctor: " + appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName() +
                ", Date: " + appointment.getAppointmentDate();
        // Corrected logCreate call: userId, username, details
        auditService.logCreate(appointment, getCurrentUserId(), getCurrentUser(), details);
    }

    @Override
    public void updateAppointment(Appointment appointment) {
        Appointment original = appointmentDao.getById(appointment.getId());
        if (original == null) { // Defensive check
            throw new IllegalArgumentException("Original Appointment not found for ID: " + appointment.getId());
        }
        if (appointment.getId() == null) {
            throw new IllegalArgumentException("Appointment ID is required for an update.");
        }
        validateAppointment(appointment, appointment.getId());
        appointmentDao.update(appointment);
        String details = "Appointment ID: " + appointment.getId() +
                ", Patient: " + appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName() +
                ", Doctor: " + appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName();
        // Corrected logUpdate call: original, updated, userId, username, details
        auditService.logUpdate(original, appointment, getCurrentUserId(), getCurrentUser(), details);
    }

    @Override
    public void cancelAppointment(Long appointmentId, boolean cancelledByPatient) {
        Appointment appointment = appointmentDao.getById(appointmentId);
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment not found with ID: " + appointmentId);
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed appointment.");
        }
        Appointment original = appointmentDao.getById(appointmentId); // Get original for logging
        appointment.setStatus(cancelledByPatient ? AppointmentStatus.CANCELLED_BY_PATIENT : AppointmentStatus.CANCELLED_BY_DOCTOR);
        appointmentDao.update(appointment);
        String details = "Appointment ID: " + appointment.getId() +
                ", Patient: " + appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName() +
                ", Status: " + appointment.getStatus().name();
        // Corrected logUpdate call: original, updated, userId, username, details
        auditService.logUpdate(original, appointment, getCurrentUserId(), getCurrentUser(), details);
    }

    @Override
    public List<Appointment> getAppointmentsForDoctor(long doctorId) {
        return appointmentDao.findByDoctorId(doctorId);
    }

    @Override
    public List<Appointment> getAppointmentsForPatient(long patientId) {
        return appointmentDao.findByPatientId(patientId);
    }

    @Override
    public void updateAppointmentStatus(Long appointmentId, AppointmentStatus newStatus) {
        Appointment appointment = appointmentDao.getById(appointmentId);
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment not found with ID: " + appointmentId);
        }
        Appointment original = appointmentDao.getById(appointmentId); // Get original for logging
        appointment.setStatus(newStatus);
        appointmentDao.update(appointment);
        String details = "Appointment ID: " + appointment.getId() +
                ", Patient: " + appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName() +
                ", New Status: " + newStatus.name();
        // Corrected logUpdate call: original, updated, userId, username, details
        auditService.logUpdate(original, appointment, getCurrentUserId(), getCurrentUser(), details);
    }

    @Override
    public List<Appointment> getAppointmentsByPatient(Patient patient) {
        if (patient == null || patient.getId() == null) {
            throw new IllegalArgumentException("Patient must not be null and must have a valid ID.");
        }
        return appointmentDao.findByPatientId(patient.getId());
    }

    @Override
    public long countAppointments() {
        return appointmentDao.getAll().size();
    }

    private void validateAppointment(Appointment appointment, Long idToIgnore) {
        if (appointment == null) throw new IllegalArgumentException("Appointment object cannot be null.");
        if (appointment.getPatient() == null) throw new IllegalArgumentException("Patient is required for the appointment.");
        if (appointment.getDoctor() == null) throw new IllegalArgumentException("Doctor is required for the appointment.");
        if (appointment.getDoctor().getRole() != Role.DOCTOR) throw new IllegalArgumentException("Assigned staff must be a DOCTOR.");
        if (appointment.getAppointmentDate() == null) throw new IllegalArgumentException("Appointment date is required.");
        if (appointment.getReason() == null || appointment.getReason().trim().isEmpty()) throw new IllegalArgumentException("A reason for the appointment is required.");

        Date fiveMinutesAgo = Date.from(LocalDateTime.now().minusMinutes(5).atZone(ZoneId.systemDefault()).toInstant());
        if (appointment.getAppointmentDate().before(fiveMinutesAgo)) {
            throw new IllegalArgumentException("Appointment date cannot be in the past.");
        }

        if (!isDoctorAvailable(appointment.getDoctor(), appointment.getAppointmentDate(), idToIgnore)) {
            throw new IllegalArgumentException("Doctor is already booked for a 'SCHEDULED' appointment at this time.");
        }
    }

    private boolean isDoctorAvailable(Staff doctor, Date appointmentTime, Long idToIgnore) {
        List<Appointment> existing = appointmentDao.findByDoctorId(doctor.getId());
        return existing.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .filter(a -> !a.isDeleted())
                .filter(a -> idToIgnore == null || !a.getId().equals(idToIgnore))
                .noneMatch(a -> a.getAppointmentDate().equals(appointmentTime));
    }
}