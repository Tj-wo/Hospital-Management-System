package org.pahappa.service;

import org.pahappa.dao.AdmissionDao;
import org.pahappa.model.Admission;
import org.pahappa.model.Staff;
import org.pahappa.utils.Role;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.sql.Date;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class AdmissionService implements Serializable {

    private final AdmissionDao admissionDao = new AdmissionDao();

    @Inject
    private StaffService staffService;

    public AdmissionService() {}

    public void admitPatient(Admission admission) {
        Staff assignedNurse = findAvailableNurse();
        if (assignedNurse == null) {
            System.err.println("Warning: No available nurses to assign during admission.");
        }
        admission.setNurse(assignedNurse);
        validateAdmission(admission);
        admissionDao.save(admission);
    }

    public void updateAdmission(Admission admission) {
        if (admission.getId() == null) {
            throw new IllegalArgumentException("Admission ID is required for an update.");
        }
        validateAdmission(admission);
        admissionDao.update(admission);
    }

    public void dischargePatient(Long admissionId) {
        if (admissionId == null) {
            throw new IllegalArgumentException("Admission ID is required to discharge patient.");
        }
        Admission admission = admissionDao.getById(admissionId);
        if (admission != null && admission.getDischargeDate() == null) {
            admission.setDischargeDate(new Date(System.currentTimeMillis()));
            admissionDao.update(admission);
        }
    }

    public Admission getAdmissionById(Long id) {
        return admissionDao.getById(id);
    }

    public List<Admission> getAllAdmissions() {
        return admissionDao.getAll();
    }

    public List<Admission> getAdmissionsForPatient(Long patientId) {
        return admissionDao.findByPatientId(patientId);
    }

    public List<Admission> getAdmissionsForNurse(Long nurseId) {
        return admissionDao.findByNurseId(nurseId);
    }

    private Staff findAvailableNurse() {
        List<Staff> nurses = staffService.getStaffByRole(Role.NURSE);
        if (nurses.isEmpty()) {
            return null;
        }
        return nurses.stream()
                .min(Comparator.comparingLong(nurse -> admissionDao.countActiveAdmissionsByNurse(nurse.getId())))
                .orElse(null);
    }

    private void validateAdmission(Admission admission) {
        if (admission == null) throw new IllegalArgumentException("Admission object cannot be null.");
        if (admission.getPatient() == null) throw new IllegalArgumentException("Patient is required for admission.");
        if (admission.getAdmissionDate() == null) throw new IllegalArgumentException("Admission date is required.");
        if (admission.getDischargeDate() != null && admission.getDischargeDate().before(admission.getAdmissionDate())) {
            throw new IllegalArgumentException("Discharge date cannot be before admission date.");
        }
        if (admission.getWard() == null || admission.getWard().trim().isEmpty()) throw new IllegalArgumentException("Ward is required.");
    }
}