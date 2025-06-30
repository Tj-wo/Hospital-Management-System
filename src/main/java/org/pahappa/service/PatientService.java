package org.pahappa.service;

import org.pahappa.model.Patient;
import java.util.List;

public interface PatientService {
    void addPatient(Patient patient);
    void updatePatient(Patient patient);
    Patient getPatient(Long id);
    List<Patient> getAllPatients();
    void deletePatient(Long id);
}