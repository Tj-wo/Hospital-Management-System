package org.pahappa.service.appointment.impl;

import org.pahappa.dao.AppointmentDao;
import org.pahappa.model.Appointment;
import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
import org.pahappa.model.Role;
import org.pahappa.service.audit.AuditService;
import org.pahappa.service.appointment.AppointmentService;
import org.pahappa.service.role.RoleService;
import org.pahappa.utils.AppointmentStatus;
import org.pahappa.exception.HospitalServiceException;
import org.pahappa.exception.ValidationException;
import org.pahappa.exception.ResourceNotFoundException;
import org.pahappa.exception.AppointmentConflictException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import org.pahappa.controller.LoginBean;
import org.pahappa.utils.Constants;

@ApplicationScoped
public class AppointmentServiceImpl implements AppointmentService {

    @Inject
    private AppointmentDao appointmentDao;

    @Inject
    private AuditService auditService;

    @Inject
    private RoleService roleService;

    @Inject
    private LoginBean loginBean;


    private String getCurrentUser() {
        if (loginBean != null && loginBean.getLoggedInUser() != null) {
            return loginBean.getLoggedInUser().getUsername(); 
        }
        return "system";
    }

    private String getCurrentUserId() {
        if (loginBean != null && loginBean.getLoggedInUser() != null && loginBean.getLoggedInUser().getId() != null) {
            return loginBean.getLoggedInUser().getId().toString(); 
        }
        return "0";
    }

    @Override
    public List getAllAppointments() {
        return appointmentDao.getAll(); 
    }

    @Override
    public Appointment getAppointmentById(Long appointmentId) {
        return appointmentDao.getById(appointmentId);
    }

    @Override
    public void scheduleAppointment(Appointment appointment,String userId, String username) throws HospitalServiceException {
        try { 
            validateAppointment(appointment, null); 
            appointment.setDeleted(false);
            appointment.setStatus(AppointmentStatus.SCHEDULED);
            appointmentDao.save(appointment);

            String details = "Patient: " + appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName() +
                    ", Doctor: " + appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName() +
                    ", Date: " + appointment.getAppointmentDate();
            auditService.logCreate(appointment, getCurrentUserId(), getCurrentUser(), details); 
        } catch (ValidationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to schedule appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateAppointment(Appointment appointment) throws HospitalServiceException {
        try {
            Appointment original = appointmentDao.getById(appointment.getId());
            if (original == null) {
                throw new ResourceNotFoundException("Appointment not found for ID: " + appointment.getId()); 
            }
            if (appointment.getId() == null) {
                throw new ValidationException("Appointment ID is required for an update."); 
            }
            validateAppointment(appointment, appointment.getId()); 
            appointmentDao.update(appointment);

            String details = "Appointment ID: " + appointment.getId() +
                    ", Patient: " + appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName() +
                    ", Doctor: " + appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName();
            auditService.logUpdate(original, appointment, getCurrentUserId(), getCurrentUser(), details); 
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to update appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelAppointment(Long appointmentId, boolean cancelledByPatient) throws HospitalServiceException {
        try {
            Appointment appointment = appointmentDao.getById(appointmentId);
            if (appointment == null) {
                throw new ResourceNotFoundException("Appointment not found with ID: " + appointmentId); 
            }
            if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
                throw new ValidationException("Cannot cancel a completed appointment."); 
            }
            if (appointment.getStatus() == AppointmentStatus.CANCELLED_BY_PATIENT || appointment.getStatus() == AppointmentStatus.CANCELLED_BY_DOCTOR) {
                throw new ValidationException("Appointment is already cancelled.");
            }

            Appointment original = appointmentDao.getById(appointmentId);
            appointment.setStatus(cancelledByPatient ? AppointmentStatus.CANCELLED_BY_PATIENT : AppointmentStatus.CANCELLED_BY_DOCTOR); 
            appointmentDao.update(appointment);

            String details = "Appointment ID: " + appointment.getId() +
                    ", Patient: " + appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName() +
                    ", Status: " + appointment.getStatus().name();
            auditService.logUpdate(original, appointment, getCurrentUserId(), getCurrentUser(), details); 
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to cancel appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public List getAppointmentsForDoctor(long doctorId) {
        return appointmentDao.findByDoctorId(doctorId); 
    }

    @Override
    public List getAppointmentsForPatient(long patientId) {
        return appointmentDao.findByPatientId(patientId); 
    }

    @Override
    public void updateAppointmentStatus(Long appointmentId, AppointmentStatus newStatus) throws HospitalServiceException {
        try {
            Appointment appointment = appointmentDao.getById(appointmentId);
            if (appointment == null) {
                throw new ResourceNotFoundException("Appointment not found with ID: " + appointmentId); 
            }
            if (newStatus == null) {
                throw new ValidationException("New status cannot be null.");
            }

            Appointment original = appointmentDao.getById(appointmentId);
            appointment.setStatus(newStatus); 
            appointmentDao.update(appointment);

            String details = "Appointment ID: " + appointment.getId() +
                    ", Patient: " + appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName() +
                    ", New Status: " + newStatus.name();
            auditService.logUpdate(original, appointment, getCurrentUserId(), getCurrentUser(), details); 
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to update appointment status: " + e.getMessage(), e);
        }
    }

    @Override
    public List getAppointmentsByPatient(Patient patient) {
        if (patient == null || patient.getId() == null) {
            throw new IllegalArgumentException("Patient must not be null and must have a valid ID."); 
        }
        return appointmentDao.findByPatientId(patient.getId());
    }

    @Override
    public long countAppointments() {
        return appointmentDao.getAll().size();
    }

    private void validateAppointment(Appointment appointment, Long idToIgnore) throws ValidationException, AppointmentConflictException {
        if (appointment == null) throw new ValidationException("Appointment object cannot be null.");
        if (appointment.getPatient() == null) throw new ValidationException("Patient is required for the appointment.");
        if (appointment.getDoctor() == null) throw new ValidationException("Doctor is required for the appointment.");

        if (appointment.getDoctor().getRole() == null || !appointment.getDoctor().getRole().getName().equalsIgnoreCase("DOCTOR")) {
            throw new ValidationException("Assigned staff must be a DOCTOR.");
        }

        if (appointment.getAppointmentDate() == null) throw new ValidationException("Appointment date is required.");
        if (appointment.getReason() == null || appointment.getReason().trim().isEmpty()) throw new ValidationException("A reason for the appointment is required.");
        Date fiveMinutesAgo = Date.from(LocalDateTime.now().minusMinutes(5).atZone(ZoneId.systemDefault()).toInstant());
        if (!isDoctorAvailable(appointment.getDoctor(), appointment.getAppointmentDate(), idToIgnore)) {
            throw new AppointmentConflictException("Doctor is already booked within this time slot.");
        }
        if (!isPatientAvailable(appointment.getPatient(), appointment.getAppointmentDate(), idToIgnore)) {
            throw new AppointmentConflictException("Patient already has an appointment within this time slot.");
        }
    }

    private boolean isDoctorAvailable(Staff doctor, Date appointmentTime, Long idToIgnore) {
        List<Appointment> existing = appointmentDao.findByDoctorId(doctor.getId());
        Instant newStart = appointmentTime.toInstant();
        Instant newEnd = newStart.plus(Constants.APPOINTMENT_DURATION_MINUTES, ChronoUnit.MINUTES);

        return existing.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED && !a.isDeleted())
                .filter(a -> idToIgnore == null || !a.getId().equals(idToIgnore))
                .noneMatch(a -> {
                    Instant existingStart = a.getAppointmentDate().toInstant();
                    Instant existingEnd = existingStart.plus(Constants.APPOINTMENT_DURATION_MINUTES, ChronoUnit.MINUTES);
                    // Check for overlap: (StartA < EndB) and (EndA > StartB)
                    return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
                });
    }

    private boolean isPatientAvailable(Patient patient, Date appointmentTime, Long idToIgnore) {
        List<Appointment> existing = appointmentDao.findByPatientId(patient.getId());
        Instant newStart = appointmentTime.toInstant();
        Instant newEnd = newStart.plus(Constants.APPOINTMENT_DURATION_MINUTES, ChronoUnit.MINUTES);

        return existing.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED && !a.isDeleted())
                .filter(a -> idToIgnore == null || !a.getId().equals(idToIgnore))
                .noneMatch(a -> {
                    Instant existingStart = a.getAppointmentDate().toInstant();
                    Instant existingEnd = existingStart.plus(Constants.APPOINTMENT_DURATION_MINUTES, ChronoUnit.MINUTES);
                    return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
                });
    }
    @Override
    public void handleDeactivatedDoctorAppointments(long doctorId) throws HospitalServiceException {
        try {
            List<Appointment> futureAppointments = appointmentDao.findFutureScheduledAppointmentsByDoctor(doctorId);
            if (futureAppointments.isEmpty()) {
                return; // Nothing to do
            }

            for (Appointment appt : futureAppointments) {
                Appointment original = appointmentDao.getById(appt.getId()); // For auditing
                appt.setStatus(AppointmentStatus.NEEDS_RESCHEDULING);
                appointmentDao.update(appt);

                String details = "Appointment status changed to NEEDS_RESCHEDULING because doctor (ID: " + doctorId + ") was deactivated.";
                auditService.logUpdate(original, appt, getCurrentUserId(), getCurrentUser(), details);
            }
            System.out.println("Flagged " + futureAppointments.size() + " future appointments for rescheduling due to doctor deactivation.");
        } catch (Exception e) {
            throw new HospitalServiceException("Failed to handle appointments for deactivated doctor.", e);
        }
    }
}