package org.pahappa.dao;

import org.hibernate.Session;
import org.pahappa.model.MedicalRecord;
import org.pahappa.service.HibernateUtil;

import java.util.List;

// DAO class for MedicalRecord entity, handling database operations
public class MedicalRecordDao extends BaseDao<MedicalRecord, Long> {

    // Constructor to initialize BaseDao with MedicalRecord class
    public MedicalRecordDao() {
        super(MedicalRecord.class);
    }

    // Get all medical records for a specific patient
    public List<MedicalRecord> getByPatientId(Long patientId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from MedicalRecord where patient.id = :patientId", MedicalRecord.class)
                    .setParameter("patientId", patientId)
                    .list();
        }
    }
}