package org.pahappa.controller.receptionist;

import org.pahappa.model.Patient;
import org.pahappa.service.UserService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named("receptionistPatientBean")
@ViewScoped
public class ReceptionistPatientBean implements Serializable {

    @Inject
    private UserService userService;

    private Patient newPatient;
    private String password;

    @PostConstruct
    public void init() {
        prepareNewPatient();
    }

    public void prepareNewPatient() {
        newPatient = new Patient();
        password = null;
    }

    public void registerPatient() {
        try {
            // The UserService's registerPatient method creates both the patient and their user account
            userService.registerPatient(newPatient, password);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            "Patient '" + newPatient.getFullName() + "' registered successfully."));

            // Reset the form for the next entry
            prepareNewPatient();

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration Failed", e.getMessage()));
        }
    }

    // --- Getters and Setters ---
    public Patient getNewPatient() {
        return newPatient;
    }

    public void setNewPatient(Patient newPatient) {
        this.newPatient = newPatient;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}