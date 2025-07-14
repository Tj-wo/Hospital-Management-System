package org.pahappa.service.admission.impl;

import org.pahappa.dao.AdmissionDao;
import org.pahappa.model.Admission;
import org.pahappa.model.Staff;
import org.pahappa.service.audit.AuditService;
import org.pahappa.service.admission.AdmissionService;
import org.pahappa.service.role.RoleService;
import org.pahappa.service.staff.StaffService;
import org.pahappa.model.Role;
import org.pahappa.exception.HospitalServiceException;
import org.pahappa.exception.ValidationException;
import org.pahappa.exception.ResourceNotFoundException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.sql.Date;
import java.util.Comparator;
import java.util.List;
import org.pahappa.controller.LoginBean;

@ApplicationScoped
public class AdmissionServiceImpl implements AdmissionService {

    @Inject
    private AdmissionDao admissionDao;

    @Inject
    private AuditService auditService;

    @Inject
    private StaffService staffService;

    @Inject
    private RoleService roleService;

    @Inject
    private LoginBean loginBean;

    private String getCurrentUser() {
        if (loginBean != null && loginBean.getLoggedInUser() != null) {
            return loginBean.getLoggedInUser().getUsername();
        }
        return "system";
    }

    private String getCurrentUserId() {
        if (loginBean != null && loginBean.getLoggedInUser() != null && loginBean.getLoggedInUser().getId() != null) {
            return loginBean.getLoggedInUser().getId().toString();
        }
        return "0";
    }

    @Override
    public void admitPatient(Admission admission) throws HospitalServiceException {
        try {
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
            auditService.logCreate(admission, getCurrentUserId(), getCurrentUser(), details);
        } catch (ValidationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to admit patient: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateAdmission(Admission admission) throws HospitalServiceException {
        try {
            Admission original = admissionDao.getById(admission.getId());
            if (original == null) {
                throw new ResourceNotFoundException("Admission not found for ID: " + admission.getId());
            }
            if (admission.getId() == null) {
                throw new ValidationException("Admission ID is required for an update.");
            }
            validateAdmission(admission);
            admissionDao.update(admission);

            String details = "Admission ID: " + admission.getId() +
                    ", Patient: " + admission.getPatient().getFirstName() + " " + admission.getPatient().getLastName();
            auditService.logUpdate(original, admission, getCurrentUserId(), getCurrentUser(), details);
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to update admission: " + e.getMessage(), e);
        }
    }

    @Override
    public void dischargePatient(Long admissionId) throws HospitalServiceException {
        try {
            if (admissionId == null) {
                throw new ValidationException("Admission ID is required to discharge patient.");
            }
            Admission admission = admissionDao.getById(admissionId);
            if (admission == null) {
                throw new ResourceNotFoundException("Admission not found with ID: " + admissionId);
            }
            if (admission.getDischargeDate() != null) {
                throw new ValidationException("Patient is already discharged for admission ID: " + admissionId);
            }

            Admission original = admissionDao.getById(admissionId);
            admission.setDischargeDate(new Date(System.currentTimeMillis()));
            admissionDao.update(admission);

            String details = "Discharged Patient: " + admission.getPatient().getFirstName() + " " + admission.getPatient().getLastName() +
                    ", Admission ID: " + admission.getId() +
                    ", Discharge Date: " + admission.getDischargeDate();
            auditService.logUpdate(original, admission, getCurrentUserId(), getCurrentUser(), details);
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to discharge patient: " + e.getMessage(), e);
        }
    }

    @Override
    public Admission getAdmissionById(Long id) {
        return admissionDao.getById(id);
    }

    @Override
    public List getAllAdmissions() {
        return admissionDao.getAll();
    }

    @Override
    public List getAdmissionsForPatient(Long patientId) {
        return admissionDao.findByPatientId(patientId);
    }

    @Override
    public List getAdmissionsForNurse(Long nurseId) {
        return admissionDao.findByNurseId(nurseId);
    }

    private Staff findAvailableNurse() {
        Role nurseRole = roleService.getRoleByName("NURSE"); // Fetch NURSE Role entity [53-55]
        if (nurseRole == null) {
            System.err.println("Warning: 'NURSE' role not found in the system. Cannot assign nurse.");
            return null;
        }
        return admissionDao.findLeastBusyNurse(nurseRole);
    }


    private void validateAdmission(Admission admission) throws ValidationException {
        if (admission == null) throw new ValidationException("Admission object cannot be null.");
        if (admission.getPatient() == null) throw new ValidationException("Patient is required for admission.");
        if (admission.getAdmissionDate() == null) throw new ValidationException("Admission date is required.");
        if (admission.getDischargeDate() != null && admission.getDischargeDate().before(admission.getAdmissionDate())) {
            throw new ValidationException("Discharge date cannot be before admission date.");
        }
        if (admission.getWard() == null || admission.getWard().trim().isEmpty()) throw new ValidationException("Ward is required.");
    }
}