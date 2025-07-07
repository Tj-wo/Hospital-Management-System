package org.pahappa.controller.receptionist;

import org.pahappa.model.Patient;
import org.pahappa.service.user.UserService;
import org.pahappa.exception.HospitalServiceException; // Added import
import org.pahappa.exception.ValidationException; // Added import
import org.pahappa.exception.DuplicateEntryException; // Added import

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
        } catch (ValidationException ve) { // Specific catch for validation errors
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Registration Failed", ve.getMessage()));
        } catch (HospitalServiceException hse) { // Specific catch for service-layer errors
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration Failed", hse.getMessage()));
        } catch (Exception e) { // Generic fallback for unexpected errors
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support."));
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