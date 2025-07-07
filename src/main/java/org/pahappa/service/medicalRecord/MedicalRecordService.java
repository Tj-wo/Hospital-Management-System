package org.pahappa.service.medicalRecord;

import org.pahappa.exception.HospitalServiceException;
import org.pahappa.model.MedicalRecord;
import java.util.List;

public interface MedicalRecordService {
    void saveMedicalRecord(MedicalRecord record) throws HospitalServiceException;
    void updateMedicalRecord(MedicalRecord record) throws HospitalServiceException;
    MedicalRecord getMedicalRecordById(Long id);
    List getAllMedicalRecords();
    List getRecordsForPatient(Long patientId);
}