package org.pahappa.service.patient.impl;

import org.pahappa.dao.PatientDao;
import org.pahappa.model.Patient;
import org.pahappa.model.User;
import org.pahappa.service.audit.AuditService;
import org.pahappa.service.audit.impl.AuditServiceImpl;
import org.pahappa.service.patient.PatientService;
import org.pahappa.utils.Constants;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.pahappa.controller.LoginBean;


@ApplicationScoped
public class PatientServiceImpl implements PatientService {

    @Inject
    private PatientDao patientDao;

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
    public List<Patient> getAllPatients() {
        return patientDao.getAll().stream()
                .filter(patient -> !patient.isDeleted())
                .collect(Collectors.toList());
    }

    @Override
    public Patient getPatient(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return patientDao.getById(id);
    }

    @Override
    public void addPatient(Patient patient) {
        if (patient.getId() != null) {
            throw new IllegalArgumentException("Cannot add a patient that already has an ID.");
        }
        validatePatient(patient);
        patient.setDeleted(false);
        patientDao.save(patient);
        String details = "Name: " + patient.getFirstName() + " " + patient.getLastName() + ", Email: " + patient.getEmail();
        // Corrected logCreate call: userId, username, details
        auditService.logCreate(patient, getCurrentUserId(), getCurrentUser(), details);
    }

    @Override
    public void updatePatient(Patient patient) {
        Patient original = patientDao.getById(patient.getId());
        if (original == null) { // Defensive check
            throw new IllegalArgumentException("Original Patient not found for ID: " + patient.getId());
        }
        if (patient.getId() == null) {
            throw new IllegalArgumentException("Patient ID is required for an update.");
        }
        validatePatient(patient);
        patientDao.update(patient);
        String details = "Patient ID: " + patient.getId() + ", New Name: " + patient.getFirstName() + " " + patient.getLastName();
        // Corrected logUpdate call: original, updated, userId, username, details
        auditService.logUpdate(original, patient, getCurrentUserId(), getCurrentUser(), details);
    }

    @Override
    public void deletePatient(Long id) {
        softDeletePatient(id);
    }

    @Override
    public void softDeletePatient(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid ID for soft deletion.");
        }
        Patient patient = patientDao.getById(id);
        if (patient != null) {
            patient.setDeleted(true);
            patientDao.update(patient);
            String details = "Soft Deleted Patient ID: " + patient.getId() + ", Name: " + patient.getFirstName() + " " + patient.getLastName();
            // Corrected logDelete call: entity, userId, username, details
            auditService.logDelete(patient, getCurrentUserId(), getCurrentUser(), details);
        }
    }

    @Override
    public void restorePatient(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid ID for restoration.");
        }

        Patient patient = patientDao.getByIdIncludingDeleted(id);

        if (patient != null && patient.isDeleted()) {
            patient.setDeleted(false);
            patientDao.update(patient);
            String details = "Restored Patient ID: " + patient.getId() + ", Name: " + patient.getFirstName() + " " + patient.getLastName();
            auditService.logUpdate(patient, patient, getCurrentUserId(), getCurrentUser(), details);
        }
    }

    @Override
    public void permanentlyDeletePatient(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid ID for permanent deletion.");
        }
        Patient patient = patientDao.getByIdIncludingDeleted(id);
        patientDao.delete(id);
        String details = "Permanently Deleted Patient ID: " + patient.getId() + ", Name: " + patient.getFirstName() + " " + patient.getLastName();
        // Corrected logDelete call: entity, userId, username, details
        auditService.logDelete(patient, getCurrentUserId(), getCurrentUser(), details);
    }

    @Override
    public long countPatients() {
        return getAllPatients().size();
    }

    private void validatePatient(Patient patient) {
        if (patient == null) throw new IllegalArgumentException("Patient object cannot be null.");
        if (patient.getFirstName() == null || patient.getFirstName().trim().isEmpty() || patient.getFirstName().length() > Constants.MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Valid first name is required and must be less than 50 characters.");
        }
        if (patient.getLastName() == null || patient.getLastName().trim().isEmpty() || patient.getLastName().length() > Constants.MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Valid last name is required and must be less than 50 characters.");
        }
        if (patient.getEmail() == null || !isValidEmail(patient.getEmail())) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_EMAIL);
        }
        if (patient.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Date of Birth is required.");
        }
        if (patient.getDateOfBirth().after(new Date())) {
            throw new IllegalArgumentException("Date of Birth cannot be in the future.");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && Pattern.matches(Constants.EMAIL_REGEX, email);
    }
}