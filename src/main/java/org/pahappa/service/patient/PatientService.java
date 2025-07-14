package org.pahappa.service.patient;

import org.pahappa.exception.HospitalServiceException;
import org.pahappa.model.Patient;

import java.util.List;

public interface PatientService {
    void addPatient(Patient patient) throws HospitalServiceException;
    void updatePatient(Patient patient) throws HospitalServiceException;
    void deletePatient(Long id) throws HospitalServiceException;
    void softDeletePatient(Long id) throws HospitalServiceException;
    void restorePatient(Long id) throws HospitalServiceException;
    void permanentlyDeletePatient(Long id) throws HospitalServiceException;
    Patient getPatient(Long id);
    List <Patient> getAllPatients();
    List<Patient> getSoftDeletedPatients();
    long countPatients();
}