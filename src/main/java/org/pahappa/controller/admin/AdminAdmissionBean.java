package org.pahappa.controller.admin;

import org.pahappa.model.Admission;
import org.pahappa.service.AdmissionService;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("adminAdmissionBean")
@ViewScoped
public class AdminAdmissionBean implements Serializable {

    @Inject
    private AdmissionService admissionService;

    private List<Admission> admissions;
    private Admission selectedAdmission;

    @PostConstruct
    public void init() {
        loadAdmissions();
    }

    private void loadAdmissions() {
        admissions = admissionService.getAllAdmissions();
    }

    public void dischargePatient() {
        if (selectedAdmission != null) {
            try {
                admissionService.dischargePatient(selectedAdmission.getId());
                loadAdmissions(); // Refresh
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Patient has been discharged.");
            } catch (Exception e) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to discharge patient: " + e.getMessage());
            }
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters and Setters
    public List<Admission> getAdmissions() { return admissions; }
    public Admission getSelectedAdmission() { return selectedAdmission; }
    public void setSelectedAdmission(Admission admission) { this.selectedAdmission = admission; }
}