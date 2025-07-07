package org.pahappa.service.medicalRecord.impl;

import org.pahappa.dao.MedicalRecordDao;
import org.pahappa.model.MedicalRecord;
import org.pahappa.model.User; // Ensure User model is imported
import org.pahappa.service.audit.AuditService;
import org.pahappa.service.audit.impl.AuditServiceImpl;
import org.pahappa.service.medicalRecord.MedicalRecordService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import org.pahappa.controller.LoginBean;


@ApplicationScoped
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordDao medicalRecordDao = new MedicalRecordDao();

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
    public void saveMedicalRecord(MedicalRecord record) {
        validateRecord(record);
        medicalRecordDao.save(record);
        String details = "Patient: " + record.getPatient().getFirstName() + " " + record.getPatient().getLastName() +
                ", Doctor: " + record.getDoctor().getFirstName() + " " + record.getDoctor().getLastName() +
                ", Diagnosis: " + record.getDiagnosis();
        // Corrected logCreate call: userId, username, details
        auditService.logCreate(record, getCurrentUserId(), getCurrentUser(), details);
    }

    @Override
    public void updateMedicalRecord(MedicalRecord record) {
        MedicalRecord original = medicalRecordDao.getById(record.getId());
        if (original == null) { // Defensive check
            throw new IllegalArgumentException("Original Medical Record not found for ID: " + record.getId());
        }
        if (record.getId() == null) {
            throw new IllegalArgumentException("Medical Record ID is required for an update.");
        }
        validateRecord(record);
        medicalRecordDao.update(record);
        String details = "Medical Record ID: " + record.getId() +
                ", Patient: " + record.getPatient().getFirstName() + " " + record.getPatient().getLastName();
        // Corrected logUpdate call: original, updated, userId, username, details
        auditService.logUpdate(original, record, getCurrentUserId(), getCurrentUser(), details);
    }

    @Override
    public MedicalRecord getMedicalRecordById(Long id) {
        return medicalRecordDao.getById(id);
    }

    @Override
    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordDao.getAll();
    }

    @Override
    public List<MedicalRecord> getRecordsForPatient(Long patientId) {
        return medicalRecordDao.getByPatientId(patientId);
    }

    private void validateRecord(MedicalRecord record) {
        if (record.getPatient() == null) throw new IllegalArgumentException("Patient is required for a medical record.");
        if (record.getDoctor() == null) throw new IllegalArgumentException("Doctor is required for a medical record.");
        if (record.getDiagnosis() == null || record.getDiagnosis().trim().isEmpty()) throw new IllegalArgumentException("Diagnosis cannot be empty.");
    }
}