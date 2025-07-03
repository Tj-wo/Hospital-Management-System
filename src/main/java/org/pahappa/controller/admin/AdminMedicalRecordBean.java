package org.pahappa.controller.admin;

import org.pahappa.model.MedicalRecord;
import org.pahappa.service.medicalRecord.MedicalRecordService;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("adminMedicalRecordBean")
@ViewScoped
public class AdminMedicalRecordBean implements Serializable {

    @Inject
    private MedicalRecordService medicalRecordService;

    private List<MedicalRecord> medicalRecords;
    private MedicalRecord selectedRecord;

    @PostConstruct
    public void init() {
        loadRecords();
    }

    private void loadRecords() {
        medicalRecords = medicalRecordService.getAllMedicalRecords();
    }


    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters and Setters
    public List<MedicalRecord> getMedicalRecords() { return medicalRecords; }
    public MedicalRecord getSelectedRecord() { return selectedRecord; }
    public void setSelectedRecord(MedicalRecord record) { this.selectedRecord = record; }
}