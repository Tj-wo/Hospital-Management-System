package org.pahappa.presentation.admin;

import org.pahappa.model.Patient;
import org.pahappa.service.PatientService;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("adminPatientBean")
@ViewScoped
public class AdminPatientBean implements Serializable {

    @Inject
    private PatientService patientService;

    private List<Patient> patients;
    private Patient selectedPatient;

    @PostConstruct
    public void init() {
        loadPatients();
    }

    private void loadPatients() {
        patients = patientService.getAllPatients();
    }

    public void deletePatient() {
        if (selectedPatient != null) {
            try {
                // The service layer handles the soft delete
                patientService.deletePatient(selectedPatient.getId());
                loadPatients(); // Refresh the list
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Patient record deleted successfully.");
            } catch (Exception e) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not delete patient: " + e.getMessage());
            }
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters and Setters
    public List<Patient> getPatients() { return patients; }
    public Patient getSelectedPatient() { return selectedPatient; }
    public void setSelectedPatient(Patient selectedPatient) { this.selectedPatient = selectedPatient; }
}