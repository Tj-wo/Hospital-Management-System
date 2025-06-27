package org.pahappa.service;

import org.pahappa.dao.MedicalRecordDao;
import org.pahappa.model.MedicalRecord;

import javax.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.util.List;

@ApplicationScoped
public class MedicalRecordService implements Serializable {

    private final MedicalRecordDao medicalRecordDao = new MedicalRecordDao();

    public MedicalRecordService() {}

    public void saveMedicalRecord(MedicalRecord record) {
        validateRecord(record);
        medicalRecordDao.save(record);
    }

    public void updateMedicalRecord(MedicalRecord record) {
        if (record.getId() == null) {
            throw new IllegalArgumentException("Medical Record ID is required for an update.");
        }
        validateRecord(record);
        medicalRecordDao.update(record);
    }

    public MedicalRecord getMedicalRecordById(Long id) {
        return medicalRecordDao.getById(id);
    }

    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordDao.getAll();
    }

    public List<MedicalRecord> getRecordsForPatient(Long patientId) {
        return medicalRecordDao.getByPatientId(patientId);
    }

    private void validateRecord(MedicalRecord record) {
        if (record.getPatient() == null) throw new IllegalArgumentException("Patient is required for a medical record.");
        if (record.getDoctor() == null) throw new IllegalArgumentException("Doctor is required for a medical record.");
        if (record.getDiagnosis() == null || record.getDiagnosis().trim().isEmpty()) throw new IllegalArgumentException("Diagnosis cannot be empty.");
    }
}