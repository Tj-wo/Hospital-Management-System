package org.pahappa.dao;

import org.pahappa.model.Patient;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PatientDao extends BaseDao<Patient, Long> {
    public PatientDao() {
        super(Patient.class);
    }
}