package org.pahappa.controller;

import org.pahappa.model.Patient;
import org.pahappa.service.PatientService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@RequestScoped
public class PatientBean implements Serializable {

    @Inject
    private PatientService patientService;

    private List<Patient> patients;

    @PostConstruct
    public void init() {
        this.patients = patientService.getAllPatients();
    }

    // Getters and Setters
    public List<Patient> getPatients() { return patients; }
    public void setPatients(List<Patient> patients) { this.patients = patients; }
}