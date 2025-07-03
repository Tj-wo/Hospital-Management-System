package org.pahappa.service.patient;

import org.pahappa.model.Patient;

import java.util.List;

public interface PatientService {
    void addPatient(Patient patient);
    void updatePatient(Patient patient);
    void deletePatient(Long id); // Keep existing method, now soft-delete
    void softDeletePatient(Long id); // Explicit soft-delete method
    void restorePatient(Long id);
    void permanentlyDeletePatient(Long id);
    Patient getPatient(Long id);
    List<Patient> getAllPatients();
    long countPatients();
}