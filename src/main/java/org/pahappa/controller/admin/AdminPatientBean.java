package org.pahappa.controller.admin;

import org.pahappa.model.Patient;
import org.pahappa.service.patient.PatientService;
import org.primefaces.PrimeFaces;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Named("adminPatientBean")
@ViewScoped
public class AdminPatientBean implements Serializable {

    @Inject
    private PatientService patientService;

    private List<Patient> patients;
    private List<Patient> softDeletedPatients;
    private Patient selectedPatient;
    private Patient selectedSoftDeletedPatient;

    @PostConstruct
    public void init() {
        loadPatients();
        softDeletedPatients = patientService.getAllPatients().stream()
                .filter(Patient::isDeleted)
                .collect(Collectors.toList());
        System.out.println("Soft deleted patients count: " + softDeletedPatients.size());//debug
    }

    private void loadPatients() {
        patients = patientService.getAllPatients().stream()
                .filter(patient -> !patient.isDeleted())
                .collect(Collectors.toList());
    }

    public void softDeletePatient() {
        if (selectedPatient == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Warning", "No patient selected for soft deletion.");
            return;
        }
        try {
            patientService.softDeletePatient(selectedPatient.getId());
            patients.remove(selectedPatient);
            softDeletedPatients.add(selectedPatient);
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Patient '" + selectedPatient.getFirstName() + " " + selectedPatient.getLastName() + "' was soft-deleted.");
            selectedPatient = null;
            PrimeFaces.current().ajax().update("patientForm:messages", "patientForm:patientTable", "patientForm:softDeletedPatientTable");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not soft-delete patient: " + e.getMessage());
        }
    }

    public void restorePatient() {
        if (selectedSoftDeletedPatient == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Warning", "No patient selected for restoration.");
            return;
        }
        try {
            patientService.restorePatient(selectedSoftDeletedPatient.getId());
            softDeletedPatients.remove(selectedSoftDeletedPatient);
            patients.add(selectedSoftDeletedPatient);
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Patient '" + selectedSoftDeletedPatient.getFirstName() + " " + selectedSoftDeletedPatient.getLastName() + "' was restored.");
            selectedSoftDeletedPatient = null;
            PrimeFaces.current().ajax().update("patientForm:messages", "patientForm:patientTable", "patientForm:softDeletedPatientTable");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not restore patient: " + e.getMessage());
        }
    }

    public void permanentlyDeletePatient() {
        if (selectedSoftDeletedPatient == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Warning", "No patient selected for permanent deletion.");
            return;
        }
        try {
            patientService.permanentlyDeletePatient(selectedSoftDeletedPatient.getId());
            softDeletedPatients.remove(selectedSoftDeletedPatient);
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Patient '" + selectedSoftDeletedPatient.getFirstName() + " " + selectedSoftDeletedPatient.getLastName() + "' was permanently deleted.");
            selectedSoftDeletedPatient = null;
            PrimeFaces.current().ajax().update("patientForm:messages", "patientForm:softDeletedPatientTable");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not permanently delete patient: " + e.getMessage());
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters and Setters
    public List<Patient> getPatients() { return patients; }
    public List<Patient> getSoftDeletedPatients() { return softDeletedPatients; }
    public Patient getSelectedPatient() { return selectedPatient; }
    public void setSelectedPatient(Patient selectedPatient) { this.selectedPatient = selectedPatient; }
    public Patient getSelectedSoftDeletedPatient() { return selectedSoftDeletedPatient; }
    public void setSelectedSoftDeletedPatient(Patient selectedSoftDeletedPatient) { this.selectedSoftDeletedPatient = selectedSoftDeletedPatient; }

}