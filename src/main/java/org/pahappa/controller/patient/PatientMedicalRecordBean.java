package org.pahappa.controller.patient;

import org.pahappa.model.MedicalRecord;
import org.pahappa.controller.LoginBean;
import org.pahappa.service.MedicalRecordService;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("patientMedicalRecordBean")
@ViewScoped
public class PatientMedicalRecordBean implements Serializable {

    @Inject
    private MedicalRecordService medicalRecordService;

    @Inject
    private LoginBean loginBean;

    private List<MedicalRecord> records;

    @PostConstruct
    public void init() {
        if (isPatientLoggedIn()) {
            long patientId = loginBean.getLoggedInUser().getPatient().getId();
            records = medicalRecordService.getRecordsForPatient(patientId);
        }
    }

    private boolean isPatientLoggedIn() {
        return loginBean != null && loginBean.isLoggedIn() &&
                loginBean.getLoggedInUser().getPatient() != null &&
                loginBean.getLoggedInUser().getRole() == org.pahappa.utils.Role.PATIENT;
    }

    // --- Getter ---
    public List<MedicalRecord> getRecords() {
        return records;
    }
}