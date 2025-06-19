package org.pahappa.dao;

import org.pahappa.model.Patient;

public class PatientDao extends BaseDao<Patient, Long> {
    public PatientDao() {
        super(Patient.class);
    }
}