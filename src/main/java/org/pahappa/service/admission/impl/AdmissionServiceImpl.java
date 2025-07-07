package org.pahappa.service.admission.impl;

import org.pahappa.dao.AdmissionDao;
import org.pahappa.model.Admission;
import org.pahappa.model.Staff;
import org.pahappa.model.User; // Ensure User model is imported
import org.pahappa.service.audit.AuditService;
import org.pahappa.service.audit.impl.AuditServiceImpl;
import org.pahappa.service.admission.AdmissionService;
import org.pahappa.service.staff.StaffService;
import org.pahappa.utils.Role;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.sql.Date;
import java.util.Comparator;
import java.util.List;


import org.pahappa.controller.LoginBean;

@ApplicationScoped
public class AdmissionServiceImpl implements AdmissionService {

    private final AdmissionDao admissionDao = new AdmissionDao();

    @Inject
    private AuditService auditService;

    @Inject
    private StaffService staffService;

    // Inject the LoginBean
    @Inject
    private LoginBean loginBean;

    // Helper method to get the current username from the LoginBean
    private String getCurrentUser() {
        if (loginBean != null && loginBean.getLoggedInUser() != null) {
            return loginBean.getLoggedInUser().getUsername();
        }
        return "system"; // Fallback for background tasks or unauthenticated operations
    }

    // Helper method to get the current user ID from the LoginBean
    private String getCurrentUserId() {
        if (loginBean != null && loginBean.getLoggedInUser() != null && loginBean.getLoggedInUser().getId() != null) {
            return loginBean.getLoggedInUser().getId().toString();
        }
        return "0"; // Default ID for system or unauthenticated actions
    }


    @Override
    public void admitPatient(Admission admission) {
        Staff assignedNurse = findAvailableNurse();
        if (assignedNurse == null) {
            System.err.println("Warning: No available nurses to assign during admission.");
        }
        admission.setNurse(assignedNurse);
        validateAdmission(admission);
        admissionDao.save(admission);
        String details = "Patient: " + admission.getPatient().getFirstName() + " " + admission.getPatient().getLastName() +
                ", Admission Date: " + admission.getAdmissionDate() +
                (admission.getNurse() != null ? ", Assigned Nurse: " + admission.getNurse().getFirstName() + " " + admission.getNurse().getLastName() : "");
        // Corrected logCreate call: userId, username, details
        auditService.logCreate(admission, getCurrentUserId(), getCurrentUser(), details);
    }

    @Override
    public void updateAdmission(Admission admission) {
        Admission original = admissionDao.getById(admission.getId());
        if (original == null) { // Defensive check
            throw new IllegalArgumentException("Original Admission not found for ID: " + admission.getId());
        }
        if (admission.getId() == null) {
            throw new IllegalArgumentException("Admission ID is required for an update.");
        }
        validateAdmission(admission);
        admissionDao.update(admission);
        String details = "Admission ID: " + admission.getId() +
                ", Patient: " + admission.getPatient().getFirstName() + " " + admission.getPatient().getLastName();
        // Corrected logUpdate call: original, updated, userId, username, details
        auditService.logUpdate(original, admission, getCurrentUserId(), getCurrentUser(), details);
    }

    @Override
    public void dischargePatient(Long admissionId) {
        if (admissionId == null) {
            throw new IllegalArgumentException("Admission ID is required to discharge patient.");
        }
        Admission admission = admissionDao.getById(admissionId);
        if (admission != null && admission.getDischargeDate() == null) {
            Admission original = admissionDao.getById(admissionId); // Get the original state for logging
            admission.setDischargeDate(new Date(System.currentTimeMillis()));
            admissionDao.update(admission);
            String details = "Discharged Patient: " + admission.getPatient().getFirstName() + " " + admission.getPatient().getLastName() +
                    ", Admission ID: " + admission.getId() +
                    ", Discharge Date: " + admission.getDischargeDate();
            // Corrected logUpdate call: original, updated, userId, username, details
            auditService.logUpdate(original, admission, getCurrentUserId(), getCurrentUser(), details);
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