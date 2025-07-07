package org.pahappa.service.appointment.impl;

import org.pahappa.dao.AppointmentDao;
import org.pahappa.model.Appointment;
import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
import org.pahappa.model.User;
import org.pahappa.service.audit.AuditService;
import org.pahappa.service.appointment.AppointmentService;
import org.pahappa.utils.AppointmentStatus;
import org.pahappa.utils.Role;
import org.pahappa.exception.HospitalServiceException;
import org.pahappa.exception.ValidationException;
import org.pahappa.exception.ResourceNotFoundException;
import org.pahappa.exception.AppointmentConflictException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.pahappa.controller.LoginBean;

@ApplicationScoped
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentDao appointmentDao = new AppointmentDao(); 

    @Inject
    private AuditService auditService;

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
    public void scheduleAppointment(Appointment appointment) throws HospitalServiceException {
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

    private void validateAppointment(Appointment appointment, Long idToIgnore) throws ValidationException, AppointmentConflictException { // Added throws
        if (appointment == null) throw new ValidationException("Appointment object cannot be null."); 
        if (appointment.getPatient() == null) throw new ValidationException("Patient is required for the appointment."); 
        if (appointment.getDoctor() == null) throw new ValidationException("Doctor is required for the appointment."); 
        if (appointment.getDoctor().getRole() != Role.DOCTOR) throw new ValidationException("Assigned staff must be a DOCTOR."); 
        if (appointment.getAppointmentDate() == null) throw new ValidationException("Appointment date is required."); 
        if (appointment.getReason() == null || appointment.getReason().trim().isEmpty()) throw new ValidationException("A reason for the appointment is required."); 

        Date fiveMinutesAgo = Date.from(LocalDateTime.now().minusMinutes(5).atZone(ZoneId.systemDefault()).toInstant()); 
        if (appointment.getAppointmentDate().before(fiveMinutesAgo)) {
            throw new ValidationException("Appointment date cannot be in the past."); 
        }

        if (!isDoctorAvailable(appointment.getDoctor(), appointment.getAppointmentDate(), idToIgnore)) { 
            throw new AppointmentConflictException("Doctor is already booked for a 'SCHEDULED' appointment at this time."); // Used specific conflict exception
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