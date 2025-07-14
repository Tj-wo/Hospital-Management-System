package org.pahappa.controller.doctor;

import org.pahappa.model.MedicalRecord;
import org.pahappa.model.Patient;
import org.pahappa.controller.LoginBean;
import org.pahappa.service.medicalRecord.MedicalRecordService;
import org.pahappa.service.patient.PatientService;
import org.pahappa.exception.HospitalServiceException; // Added import
import org.pahappa.exception.ValidationException; // Added import

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Named("doctorMedicalRecordBean")
@ViewScoped
public class DoctorMedicalRecordBean implements Serializable {

    @Inject
    private MedicalRecordService medicalRecordService;
    @Inject
    private PatientService patientService;
    @Inject
    private LoginBean loginBean;

    private List allPatients;
    private List selectedPatientRecords;
    private Patient selectedPatient;
    private MedicalRecord newRecord = new MedicalRecord();

    @PostConstruct
    public void init() {
        allPatients = patientService.getAllPatients();
    }

    /**
     * This method is called via an AJAX event when the doctor selects a patient
     * from a dropdown. It loads the medical records for that specific patient.
     */
    public void onPatientChange() {
        if (selectedPatient != null) {
            selectedPatientRecords = medicalRecordService.getRecordsForPatient(selectedPatient.getId());
        } else {
            selectedPatientRecords = null;
        }
    }

    /**
     * Action method to create and save a new medical record for the selected patient.
     */
    public void addRecord() {
        if (selectedPatient != null && loginBean.isLoggedIn()) {
            try {
                // Set the patient and the currently logged-in doctor on the new record
                newRecord.setPatient(selectedPatient);
                newRecord.setDoctor(loginBean.getLoggedInUser().getStaff());

                medicalRecordService.saveMedicalRecord(newRecord);

                // Refresh the records list and reset the form
                onPatientChange();
                newRecord = new MedicalRecord();
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Medical record added for " + selectedPatient.getFullName());
            } catch (ValidationException ve) { // Specific catch for validation errors
                addMessage(FacesMessage.SEVERITY_WARN, "Save Failed", ve.getMessage());
            } catch (HospitalServiceException hse) { // Specific catch for service-layer errors
                addMessage(FacesMessage.SEVERITY_ERROR, "Save Failed", hse.getMessage());
            } catch (Exception e) { // Generic fallback for unexpected errors
                addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support.");
            }
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "No Patient Selected", "Please select a patient before adding a record.");
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public Date getToday() {
        return new Date();
    }

    // Getters and Setters
    public List getAllPatients() { return allPatients; }
    public List getSelectedPatientRecords() { return selectedPatientRecords; }
    public Patient getSelectedPatient() { return selectedPatient; }
    public void setSelectedPatient(Patient selectedPatient) { this.selectedPatient = selectedPatient; }
    public MedicalRecord getNewRecord() { return newRecord; }
    public void setNewRecord(MedicalRecord newRecord) { this.newRecord = newRecord; }
}