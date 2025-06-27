package org.pahappa.presentation;

import org.pahappa.model.Patient;
import org.pahappa.service.UserService;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named("patientRegistrationBean")
@RequestScoped
public class PatientRegistrationBean implements Serializable {

    @Inject
    private UserService userService;

    private Patient newPatient = new Patient();
    private String password;

    public String register() {
        try {
            userService.registerPatient(newPatient, password);

            // Use flash scope to keep the message after redirect
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            addMessage(FacesMessage.SEVERITY_INFO, "Registration Successful", "You can now log in with your credentials.");

            // Redirect to the login page
            return "login?faces-redirect=true";

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Registration Failed", e.getMessage());
            // Stay on the same page to show the error
            return null;
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
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