package org.pahappa.service.admission;

import org.pahappa.model.Admission;
import java.util.List;

public interface AdmissionService {
    void admitPatient(Admission admission);
    void updateAdmission(Admission admission);
    void dischargePatient(Long admissionId);
    Admission getAdmissionById(Long id);
    List<Admission> getAllAdmissions();
    List<Admission> getAdmissionsForPatient(Long patientId);
    List<Admission> getAdmissionsForNurse(Long nurseId);
}