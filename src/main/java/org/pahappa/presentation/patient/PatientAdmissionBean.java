package org.pahappa.presentation.patient;

import org.pahappa.model.Admission;
import org.pahappa.presentation.LoginBean;
import org.pahappa.service.AdmissionService;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("patientAdmissionBean")
@ViewScoped
public class PatientAdmissionBean implements Serializable {

    @Inject
    private AdmissionService admissionService;

    @Inject
    private LoginBean loginBean;

    private List<Admission> admissions;

    @PostConstruct
    public void init() {
        if (isPatientLoggedIn()) {
            long patientId = loginBean.getLoggedInUser().getPatient().getId();
            admissions = admissionService.getAdmissionsForPatient(patientId);
        }
    }

    private boolean isPatientLoggedIn() {
        return loginBean != null && loginBean.isLoggedIn() &&
                loginBean.getLoggedInUser().getPatient() != null &&
                loginBean.getLoggedInUser().getRole() == org.pahappa.utils.Role.PATIENT;
    }

    // --- Getter ---
    public List<Admission> getAdmissions() {
        return admissions;
    }
}