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

    public void prepareNewAdmission() {
        newAdmission = new Admission(); 
        newAdmission.setAdmissionDate(new java.sql.Date(new Date().getTime())); 
    }

    public void admitPatient() {
        try {
            admissionService.admitPatient(newAdmission); 
            loadAdmissions(); 
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Patient admitted successfully."); 
            PrimeFaces.current().executeScript("PF('admissionDialog').hide()"); 
            prepareNewAdmission(); 
        } catch (ValidationException ve) {
            addMessage(FacesMessage.SEVERITY_WARN, "Admission Failed", ve.getMessage());
        } catch (HospitalServiceException hse) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Admission Failed", hse.getMessage());
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support."); 
        }
    }

    public void dischargePatient() {
        if (selectedAdmission != null) { 
            try {
                admissionService.dischargePatient(selectedAdmission.getId()); 
                loadAdmissions(); 
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Patient discharged."); 
            } catch (ValidationException | ResourceNotFoundException e) {
                addMessage(FacesMessage.SEVERITY_WARN, "Discharge Failed", e.getMessage());
            } catch (HospitalServiceException hse) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Discharge Failed", hse.getMessage());
            } catch (Exception e) {
                addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support."); 
            }
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail)); 
    }

    public List<Admission> getAdmissions() { return admissions; } 
    public List<Patient> getAllPatients() { return allPatients; } 
    public Admission getNewAdmission() { return newAdmission; } 
    public void setNewAdmission(Admission newAdmission) { this.newAdmission = newAdmission; } 
    public Admission getSelectedAdmission() { return selectedAdmission; } 
    public void setSelectedAdmission(Admission selectedAdmission) { this.selectedAdmission = selectedAdmission; } 
}