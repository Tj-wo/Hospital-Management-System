package org.pahappa.service.admission;

import org.pahappa.model.Admission;
import org.pahappa.exception.HospitalServiceException;
import java.util.List;

public interface AdmissionService {
    void admitPatient(Admission admission) throws HospitalServiceException;
    void updateAdmission(Admission admission) throws HospitalServiceException;
    void dischargePatient(Long admissionId) throws HospitalServiceException;
    Admission getAdmissionById(Long id) ;
    List<Admission> getAllAdmissions();
    List<Admission> getAdmissionsForPatient(Long patientId);
    List<Admission> getAdmissionsForNurse(Long nurseId);
}