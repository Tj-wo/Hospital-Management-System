package org.pahappa.service.admission.impl;

import org.pahappa.dao.AdmissionDao;
import org.pahappa.model.Admission;
import org.pahappa.model.Staff;
import org.pahappa.service.admission.AdmissionService;
import org.pahappa.service.staff.StaffService;
import org.pahappa.utils.Role;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.sql.Date;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class AdmissionServiceImpl implements AdmissionService {

    private final AdmissionDao admissionDao = new AdmissionDao();

    @Inject
    private StaffService staffService;

    @Override
    public void admitPatient(Admission admission) {
        Staff assignedNurse = findAvailableNurse();
        if (assignedNurse == null) {
            System.err.println("Warning: No available nurses to assign during admission.");
        }
        admission.setNurse(assignedNurse);
        validateAdmission(admission);
        admissionDao.save(admission);
    }

    @Override
    public void updateAdmission(Admission admission) {
        if (admission.getId() == null) {
            throw new IllegalArgumentException("Admission ID is required for an update.");
        }
        validateAdmission(admission);
        admissionDao.update(admission);
    }

    @Override
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

    @Override
    public Admission getAdmissionById(Long id) {
        return admissionDao.getById(id);
    }

    @Override
    public List<Admission> getAllAdmissions() {
        return admissionDao.getAll();
    }

    @Override
    public List<Admission> getAdmissionsForPatient(Long patientId) {
        return admissionDao.findByPatientId(patientId);
    }

    @Override
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