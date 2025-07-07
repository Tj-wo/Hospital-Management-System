package org.pahappa.controller.doctor;

import org.pahappa.model.Admission;
import org.pahappa.model.Patient;
import org.pahappa.service.admission.AdmissionService;
import org.pahappa.service.patient.PatientService;
import org.pahappa.exception.HospitalServiceException;
import org.pahappa.exception.ValidationException;
import org.pahappa.exception.ResourceNotFoundException;

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
    private AdmissionService admissionService; // [95]
    @Inject
    private PatientService patientService; // [95]

    private List<Admission> admissions; // [96]
    private List<Patient> allPatients; // [96]
    private Admission newAdmission; // [96]
    private Admission selectedAdmission; // [96]

    @PostConstruct
    public void init() {
        loadAdmissions(); // [96]
        allPatients = patientService.getAllPatients(); // [96]
        prepareNewAdmission(); // [96]
    }

    private void loadAdmissions() {
        admissions = admissionService.getAllAdmissions(); // [96]
    }

    public void prepareNewAdmission() {
        newAdmission = new Admission(); // [97]
        newAdmission.setAdmissionDate(new java.sql.Date(new Date().getTime())); // [97]
    }

    public void admitPatient() {
        try {
            admissionService.admitPatient(newAdmission); // [97]
            loadAdmissions(); // [97]
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Patient admitted successfully."); // [97]
            PrimeFaces.current().executeScript("PF('admissionDialog').hide()"); // [97]
            prepareNewAdmission(); // [97]
        } catch (ValidationException ve) {
            addMessage(FacesMessage.SEVERITY_WARN, "Admission Failed", ve.getMessage());
        } catch (HospitalServiceException hse) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Admission Failed", hse.getMessage());
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support."); // [97]
        }
    }

    public void dischargePatient() {
        if (selectedAdmission != null) { // [98]
            try {
                admissionService.dischargePatient(selectedAdmission.getId()); // [98]
                loadAdmissions(); // [98]
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Patient discharged."); // [98]
            } catch (ValidationException | ResourceNotFoundException e) {
                addMessage(FacesMessage.SEVERITY_WARN, "Discharge Failed", e.getMessage());
            } catch (HospitalServiceException hse) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Discharge Failed", hse.getMessage());
            } catch (Exception e) {
                addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support."); // [98]
            }
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail)); // [99]
    }

    public List<Admission> getAdmissions() { return admissions; } // [99]
    public List<Patient> getAllPatients() { return allPatients; } // [99]
    public Admission getNewAdmission() { return newAdmission; } // [99]
    public void setNewAdmission(Admission newAdmission) { this.newAdmission = newAdmission; } // [99]
    public Admission getSelectedAdmission() { return selectedAdmission; } // [99]
    public void setSelectedAdmission(Admission selectedAdmission) { this.selectedAdmission = selectedAdmission; } // [99]
}