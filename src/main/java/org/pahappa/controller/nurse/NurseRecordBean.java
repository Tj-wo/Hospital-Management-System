package org.pahappa.controller.nurse;

import org.pahappa.model.Admission;
import org.pahappa.model.MedicalRecord;
import org.pahappa.controller.LoginBean;
import org.pahappa.service.admission.AdmissionService;
import org.pahappa.service.medicalRecord.MedicalRecordService;
import org.pahappa.exception.HospitalServiceException; // Added import
import org.pahappa.exception.ValidationException; // Added import
import org.pahappa.exception.ResourceNotFoundException; // Added import

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Named("nurseRecordBean")
@ViewScoped
public class NurseRecordBean implements Serializable {

    @Inject
    private MedicalRecordService medicalRecordService;
    @Inject
    private AdmissionService admissionService;
    @Inject
    private LoginBean loginBean;

    private List assignedAdmissions; // For the patient dropdown
    private Long selectedAdmissionId; // To hold the value from the dropdown
    private List selectedPatientRecords; // Records for the selected patient
    private MedicalRecord recordToUpdate; // The specific record being edited

    @PostConstruct
    public void init() {
        if (isNurseLoggedIn()) {
            // Load only active admissions for patient selection
            long nurseId = loginBean.getLoggedInUser().getStaff().getId();
            assignedAdmissions = admissionService.getAdmissionsForNurse(nurseId).stream()
                    .filter(a -> a.getDischargeDate() == null)
                    .collect(Collectors.toList());
        }
    }

    public void onPatientChange() {
        if (selectedAdmissionId != null) {
            Admission selectedAdmission = admissionService.getAdmissionById(selectedAdmissionId);
            if (selectedAdmission != null) {
                selectedPatientRecords = medicalRecordService.getRecordsForPatient(selectedAdmission.getPatient().getId());
            }
        } else {
            selectedPatientRecords = null; // Clear the list if no patient is selected
        }
    }

    public void updateRecord() {
        if (recordToUpdate != null) {
            try {
                medicalRecordService.updateMedicalRecord(recordToUpdate);
                onPatientChange(); // Refresh the records list
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Medical record updated.");
            } catch (ValidationException | ResourceNotFoundException e) { // Specific catch for validation/not found errors
                addMessage(FacesMessage.SEVERITY_ERROR, "Update Failed", e.getMessage());
            } catch (HospitalServiceException hse) { // Specific catch for service-layer errors
                addMessage(FacesMessage.SEVERITY_ERROR, "Update Failed", hse.getMessage());
            } catch (Exception e) { // Generic fallback for unexpected errors
                addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support.");
            }
        }
    }

    public void selectRecordForEdit(MedicalRecord record) {
        this.recordToUpdate = record;
    }

    private boolean isNurseLoggedIn() {
        return loginBean != null && loginBean.isLoggedIn() &&
                loginBean.getLoggedInUser().getStaff() != null &&
                loginBean.getLoggedInUser().getRole() != null &&
                loginBean.getLoggedInUser().getRole().getName().equalsIgnoreCase("NURSE");
    }


    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters and Setters
    public List getAssignedAdmissions() { return assignedAdmissions; }
    public Long getSelectedAdmissionId() { return selectedAdmissionId; }
    public void setSelectedAdmissionId(Long selectedAdmissionId) { this.selectedAdmissionId = selectedAdmissionId; }
    public List getSelectedPatientRecords() { return selectedPatientRecords; }
    public MedicalRecord getRecordToUpdate() { return recordToUpdate; }
    public void setRecordToUpdate(MedicalRecord recordToUpdate) { this.recordToUpdate = recordToUpdate; }
}