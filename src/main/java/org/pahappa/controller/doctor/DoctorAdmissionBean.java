package org.pahappa.controller.doctor;

import org.pahappa.model.Admission;
import org.pahappa.model.Patient;
import org.pahappa.service.admission.AdmissionService;
import org.pahappa.service.patient.PatientService;
import org.primefaces.PrimeFaces;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Named("doctorAdmissionBean")
@ViewScoped
public class DoctorAdmissionBean implements Serializable {

    @Inject
    private AdmissionService admissionService;

    @Inject
    private PatientService patientService;

    private List<Admission> admissions;
    private List<Patient> allPatients;

    private Admission newAdmission;
    private Admission selectedAdmission;

    @PostConstruct
    public void init() {
        loadAdmissions();
        allPatients = patientService.getAllPatients();
        prepareNewAdmission();
    }

    private void loadAdmissions() {
        admissions = admissionService.getAllAdmissions();
    }

    // Prepares a clean admission object for the dialog
    public void prepareNewAdmission() {
        newAdmission = new Admission();
        // Pre-populate the admission date to today for better UX
        newAdmission.setAdmissionDate(new java.sql.Date(new Date().getTime()));
    }

    public void admitPatient() {
        try {
            admissionService.admitPatient(newAdmission);
            loadAdmissions(); // Refresh list
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Patient admitted successfully.");
            PrimeFaces.current().executeScript("PF('admissionDialog').hide()");
            prepareNewAdmission(); // Reset form
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Admission Failed", e.getMessage());
        }
    }

    public void dischargePatient() {
        if (selectedAdmission != null) {
            try {
                admissionService.dischargePatient(selectedAdmission.getId());
                loadAdmissions(); // Refresh list to show the discharge date
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Patient discharged.");
            } catch (Exception e) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Discharge Failed", e.getMessage());
            }
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters and Setters
    public List<Admission> getAdmissions() { return admissions; }
    public List<Patient> getAllPatients() { return allPatients; }
    public Admission getNewAdmission() { return newAdmission; }
    public void setNewAdmission(Admission newAdmission) { this.newAdmission = newAdmission; }
    public Admission getSelectedAdmission() { return selectedAdmission; }
    public void setSelectedAdmission(Admission selectedAdmission) { this.selectedAdmission = selectedAdmission; }
}