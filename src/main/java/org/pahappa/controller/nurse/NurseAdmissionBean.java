package org.pahappa.controller.nurse;

import org.pahappa.model.Admission;
import org.pahappa.controller.LoginBean;
import org.pahappa.service.admission.AdmissionService;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("nurseAdmissionBean")
@ViewScoped
public class NurseAdmissionBean implements Serializable {

    @Inject
    private AdmissionService admissionService;

    @Inject
    private LoginBean loginBean;

    private List<Admission> assignedAdmissions;


    @PostConstruct
    public void init() {
        if (isNurseLoggedIn()) {
            long nurseId = loginBean.getLoggedInUser().getStaff().getId();
            assignedAdmissions = admissionService.getAdmissionsForNurse(nurseId);
        }
    }

    private boolean isNurseLoggedIn() {
        return loginBean != null && loginBean.isLoggedIn() &&
                loginBean.getLoggedInUser().getStaff() != null &&
                loginBean.getLoggedInUser().getRole() != null &&
                loginBean.getLoggedInUser().getRole().getName().equalsIgnoreCase("NURSE");
    }


    public List<Admission> getAdmissions() {
        return assignedAdmissions;
    }


    // --- Getter ---
    public List<Admission> getAssignedAdmissions() {
        return assignedAdmissions;
    }
}