package org.pahappa.controller.admin;

import org.pahappa.model.Patient;
import org.pahappa.service.patient.PatientService;
import org.pahappa.exception.HospitalServiceException;
import org.pahappa.exception.ValidationException;
import org.pahappa.exception.ResourceNotFoundException;

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
    private String globalFilter;

    @PostConstruct
    public void init() {

        loadActivePatients();
    }

    private void loadActivePatients() {
        patients = patientService.getAllPatients();
    }

    public void softDeletePatient() {
        if (selectedPatient == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Warning", "No patient selected for soft deletion.");
            return;
        }
        try {
            patientService.softDeletePatient(selectedPatient.getId());
            // After deleting, just reload the list of active patients.
            loadActivePatients();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Patient '" + selectedPatient.getFullName() + "' was soft-deleted.");
            selectedPatient = null;
        } catch (ValidationException | ResourceNotFoundException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not soft-delete patient: " + e.getMessage());
        } catch (HospitalServiceException hse) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Operation Failed", hse.getMessage());
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support.");
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // --- Getters and Setters ---
    public List<Patient> getPatients() { return patients; }
    public Patient getSelectedPatient() { return selectedPatient; }
    public void setSelectedPatient(Patient selectedPatient) { this.selectedPatient = selectedPatient; }
    public String getGlobalFilter() { return globalFilter; }
    public void setGlobalFilter(String globalFilter) { this.globalFilter = globalFilter; }
}