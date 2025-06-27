package org.pahappa.presentation.doctor;

import org.pahappa.model.MedicalRecord;
import org.pahappa.model.Patient;
import org.pahappa.presentation.LoginBean;
import org.pahappa.service.MedicalRecordService;
import org.pahappa.service.PatientService;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
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

    private List<Patient> allPatients;
    private List<MedicalRecord> selectedPatientRecords;
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
            } catch (Exception e) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Save Failed", e.getMessage());
            }
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "No Patient Selected", "Please select a patient before adding a record.");
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters and Setters
    public List<Patient> getAllPatients() { return allPatients; }
    public List<MedicalRecord> getSelectedPatientRecords() { return selectedPatientRecords; }
    public Patient getSelectedPatient() { return selectedPatient; }
    public void setSelectedPatient(Patient selectedPatient) { this.selectedPatient = selectedPatient; }
    public MedicalRecord getNewRecord() { return newRecord; }
    public void setNewRecord(MedicalRecord newRecord) { this.newRecord = newRecord; }
}