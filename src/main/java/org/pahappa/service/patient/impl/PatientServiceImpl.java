package org.pahappa.service.impl;

import org.pahappa.dao.PatientDao;
import org.pahappa.model.Patient;
import org.pahappa.service.PatientService;
import org.pahappa.utils.Constants;

import javax.enterprise.context.ApplicationScoped;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@ApplicationScoped
public class PatientServiceImpl implements PatientService {

    private final PatientDao patientDao = new PatientDao();

    @Override
    public void addPatient(Patient patient) {
        if (patient.getId() != null) {
            throw new IllegalArgumentException("Cannot add a patient that already has an ID.");
        }
        validatePatient(patient);
        patientDao.save(patient);
    }

    @Override
    public void updatePatient(Patient patient) {
        if (patient.getId() == null) {
            throw new IllegalArgumentException("Patient ID is required for an update.");
        }
        validatePatient(patient);
        patientDao.update(patient);
    }

    @Override
    public Patient getPatient(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return patientDao.getById(id);
    }

    @Override
    public List<Patient> getAllPatients() {
        return patientDao.getAll();
    }

    @Override
    public void deletePatient(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid ID provided for deletion.");
        }
        patientDao.delete(id);
    }

    @Override
    public long countPatients() {
        return patientDao.getAll().size(); // Or better: implement patientDao.count()
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
