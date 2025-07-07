package org.pahappa.service.patient.impl;

import org.pahappa.dao.PatientDao;
import org.pahappa.model.Patient;
import org.pahappa.model.User;
import org.pahappa.service.audit.AuditService;
import org.pahappa.service.patient.PatientService;
import org.pahappa.utils.Constants;
import org.pahappa.exception.HospitalServiceException;
import org.pahappa.exception.ValidationException;
import org.pahappa.exception.ResourceNotFoundException;
import org.pahappa.exception.DuplicateEntryException;

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
    public List getAllPatients() {
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
    public void addPatient(Patient patient) throws HospitalServiceException {
        try {
            if (patient.getId() != null) {
                throw new ValidationException("Cannot add a patient that already has an ID."); 
            }
            if (patientDao.getAll().stream().anyMatch(p -> p.getEmail().equalsIgnoreCase(patient.getEmail()) && !p.isDeleted())) {
                throw new DuplicateEntryException("Patient with email '" + patient.getEmail() + "' already exists.");
            }

            validatePatient(patient);
            patient.setDeleted(false);
            patientDao.save(patient);

            String details = "Name: " + patient.getFirstName() + " " + patient.getLastName() + ", Email: " + patient.getEmail();
            auditService.logCreate(patient, getCurrentUserId(), getCurrentUser(), details); 
        } catch (ValidationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to add patient: " + e.getMessage(), e);
        }
    }

    @Override
    public void updatePatient(Patient patient) throws HospitalServiceException {
        try {
            Patient original = patientDao.getById(patient.getId()); 
            if (original == null) {
                throw new ResourceNotFoundException("Original Patient not found for ID: " + patient.getId()); 
            }
            if (patient.getId() == null) {
                throw new ValidationException("Patient ID is required for an update."); 
            }
            if (!original.getEmail().equalsIgnoreCase(patient.getEmail()) &&
                    patientDao.getAll().stream().anyMatch(p -> p.getEmail().equalsIgnoreCase(patient.getEmail()) && !p.isDeleted() && !p.getId().equals(patient.getId()))) {
                throw new DuplicateEntryException("Patient with email '" + patient.getEmail() + "' already exists.");
            }

            validatePatient(patient);
            patientDao.update(patient);

            String details = "Patient ID: " + patient.getId() + ", New Name: " + patient.getFirstName() + " " + patient.getLastName();
            auditService.logUpdate(original, patient, getCurrentUserId(), getCurrentUser(), details); 
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to update patient: " + e.getMessage(), e);
        }
    }

    @Override
    public void deletePatient(Long id) throws HospitalServiceException {
        softDeletePatient(id); 
    }

    @Override
    public void softDeletePatient(Long id) throws HospitalServiceException {
        try {
            if (id == null || id <= 0) {
                throw new ValidationException("Invalid ID for soft deletion."); 
            }
            Patient patient = patientDao.getById(id);
            if (patient == null) {
                throw new ResourceNotFoundException("Patient not found with ID: " + id);
            }
            if (patient.isDeleted()) {
                throw new ValidationException("Patient is already soft-deleted.");
            }

            patient.setDeleted(true); 
            patientDao.update(patient); 

            String details = "Soft Deleted Patient ID: " + patient.getId() + ", Name: " + patient.getFirstName() + " " + patient.getLastName();
            auditService.logDelete(patient, getCurrentUserId(), getCurrentUser(), details); 
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to soft-delete patient: " + e.getMessage(), e);
        }
    }

    @Override
    public void restorePatient(Long id) throws HospitalServiceException {
        try {
            if (id == null || id <= 0) {
                throw new ValidationException("Invalid ID for restoration."); 
            }
            System.out.println("patient id" +id); 
            Patient patient = patientDao.getById(id);
            System.out.println("the patient is " +patient.getFirstName()); 
            if (patient == null) {
                throw new ResourceNotFoundException("Patient not found with ID: " + id);
            }
            if (!patient.isDeleted()) {
                throw new ValidationException("Patient is not soft-deleted and cannot be restored.");
            }

            patient.setDeleted(false); 
            patientDao.update(patient); 

            String details = "Restored Patient ID: " + patient.getId() + ", Name: " + patient.getFirstName() + " " + patient.getLastName();
            auditService.logUpdate(patient, patient, getCurrentUserId(), getCurrentUser(), details); 
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to restore patient: " + e.getMessage(), e);
        }
    }

    @Override
    public void permanentlyDeletePatient(Long id) throws HospitalServiceException {
        try {
            if (id == null || id <= 0) {
                throw new ValidationException("Invalid ID for permanent deletion."); 
            }
            Patient patient = patientDao.getById(id); 
            if (patient == null) {
                throw new ResourceNotFoundException("Patient not found with ID: " + id);
            }
            patientDao.delete(id); // This performs a soft-delete based on BaseDao [40]

            String details = "Permanently Deleted Patient ID: " + patient.getId() + ", Name: " + patient.getFirstName() + " " + patient.getLastName();
            auditService.logDelete(patient, getCurrentUserId(), getCurrentUser(), details); 
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to permanently delete patient: " + e.getMessage(), e);
        }
    }

    @Override
    public long countPatients() {
        return getAllPatients().size(); 
    }

    private void validatePatient(Patient patient) throws ValidationException { // Added throws
        if (patient == null) throw new ValidationException("Patient object cannot be null."); 
        if (patient.getFirstName() == null || patient.getFirstName().trim().isEmpty() || patient.getFirstName().length() > Constants.MAX_NAME_LENGTH) {
            throw new ValidationException("Valid first name is required and must be less than 50 characters."); 
        }
        if (patient.getLastName() == null || patient.getLastName().trim().isEmpty() || patient.getLastName().length() > Constants.MAX_NAME_LENGTH) {
            throw new ValidationException("Valid last name is required and must be less than 50 characters."); 
        }
        if (patient.getEmail() == null || !isValidEmail(patient.getEmail())) {
            throw new ValidationException(Constants.ERROR_INVALID_EMAIL);
        }
        if (patient.getDateOfBirth() == null) {
            throw new ValidationException("Date of Birth is required."); 
        }
        if (patient.getDateOfBirth().after(new Date())) {
            throw new ValidationException("Date of Birth cannot be in the future."); 
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && Pattern.matches(Constants.EMAIL_REGEX, email);
    }
}