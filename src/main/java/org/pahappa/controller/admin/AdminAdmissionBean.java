package org.pahappa.controller.admin;

import org.pahappa.model.Admission;
import org.pahappa.service.admission.AdmissionService;
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


    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters and Setters
    public List<Admission> getAdmissions() { return admissions; }
    public Admission getSelectedAdmission() { return selectedAdmission; }
    public void setSelectedAdmission(Admission admission) { this.selectedAdmission = admission; }
}