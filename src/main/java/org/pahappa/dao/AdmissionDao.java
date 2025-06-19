package org.pahappa.dao;

import org.pahappa.model.Admission;

// DAO class for Admission entity, handling database operations
public class AdmissionDao extends BaseDao<Admission, Long> {

    // Constructor to initialize BaseDao with Admission class
    public AdmissionDao() {
        super(Admission.class);
    }
}