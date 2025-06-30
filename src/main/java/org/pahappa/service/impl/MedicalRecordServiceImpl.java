package org.pahappa.service.impl;

import org.pahappa.dao.MedicalRecordDao;
import org.pahappa.model.MedicalRecord;
import org.pahappa.service.MedicalRecordService;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordDao medicalRecordDao = new MedicalRecordDao();

    @Override
    public void saveMedicalRecord(MedicalRecord record) {
        validateRecord(record);
        medicalRecordDao.save(record);
    }

    @Override
    public void updateMedicalRecord(MedicalRecord record) {
        if (record.getId() == null) {
            throw new IllegalArgumentException("Medical Record ID is required for an update.");
        }
        validateRecord(record);
        medicalRecordDao.update(record);
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
