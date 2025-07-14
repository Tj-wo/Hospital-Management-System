package org.pahappa.service.medicalRecord.impl;

import org.pahappa.dao.MedicalRecordDao;
import org.pahappa.model.MedicalRecord;
import org.pahappa.model.User;
import org.pahappa.service.audit.AuditService;
import org.pahappa.service.medicalRecord.MedicalRecordService;
import org.pahappa.exception.HospitalServiceException;
import org.pahappa.exception.ValidationException;
import org.pahappa.exception.ResourceNotFoundException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.Date;
import java.util.List;

import org.pahappa.controller.LoginBean;

@ApplicationScoped
public class MedicalRecordServiceImpl implements MedicalRecordService {

    @Inject
    private MedicalRecordDao medicalRecordDao;

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
    public void saveMedicalRecord(MedicalRecord record) throws HospitalServiceException {
        try {
            validateRecord(record); 
            medicalRecordDao.save(record);

            String details = "Patient: " + record.getPatient().getFirstName() + " " + record.getPatient().getLastName() +
                    ", Doctor: " + record.getDoctor().getFirstName() + " " + record.getDoctor().getLastName() +
                    ", Diagnosis: " + record.getDiagnosis();
            auditService.logCreate(record, getCurrentUserId(), getCurrentUser(), details); 
        } catch (ValidationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to save medical record: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateMedicalRecord(MedicalRecord record) throws HospitalServiceException {
        try {
            MedicalRecord original = medicalRecordDao.getById(record.getId());
            if (original == null) {
                throw new ResourceNotFoundException("Medical Record not found for ID: " + record.getId()); 
            }
            if (record.getId() == null) {
                throw new ValidationException("Medical Record ID is required for an update."); 
            }
            validateRecord(record);
            medicalRecordDao.update(record);

            String details = "Medical Record ID: " + record.getId() +
                    ", Patient: " + record.getPatient().getFirstName() + " " + record.getPatient().getLastName();
            auditService.logUpdate(original, record, getCurrentUserId(), getCurrentUser(), details); 
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to update medical record: " + e.getMessage(), e);
        }
    }

    @Override
    public MedicalRecord getMedicalRecordById(Long id) {
        return medicalRecordDao.getById(id);
    }

    @Override
    public List getAllMedicalRecords() {
        return medicalRecordDao.getAll();
    }

    @Override
    public List getRecordsForPatient(Long patientId) {
        return medicalRecordDao.getByPatientId(patientId); 
    }

    private void validateRecord(MedicalRecord record) throws ValidationException { // Added throws
        if (record.getPatient() == null) throw new ValidationException("Patient is required for a medical record."); 
        if (record.getDoctor() == null) throw new ValidationException("Doctor is required for a medical record."); 
        if (record.getDiagnosis() == null || record.getDiagnosis().trim().isEmpty()) throw new ValidationException("Diagnosis cannot be empty.");
        if (record.getRecordDate() == null) {
            throw new ValidationException("Record date is required.");
        }
        if (record.getRecordDate().after(new Date())) {
            throw new ValidationException("Record date cannot be in the future.");
        }
        if (record.getRecordDate().before(new Date())) {
            throw new ValidationException("Record date cannot be in the past.");
        }
    }
}