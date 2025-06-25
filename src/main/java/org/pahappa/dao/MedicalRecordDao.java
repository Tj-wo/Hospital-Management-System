package org.pahappa.dao;

import org.hibernate.Session;
import org.pahappa.model.MedicalRecord;
import org.pahappa.service.HibernateUtil;

import java.util.List;

public class MedicalRecordDao extends BaseDao<MedicalRecord, Long> {

    public MedicalRecordDao() {
        super(MedicalRecord.class);
    }

    public List<MedicalRecord> getByPatientId(Long patientId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from MedicalRecord where patient.id = :patientId order by recordDate desc", MedicalRecord.class)
                    .setParameter("patientId", patientId)
                    .list();
        }
    }

    public List<MedicalRecord> getByDoctorId(Long doctorId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from MedicalRecord where doctor.id = :doctorId order by recordDate desc", MedicalRecord.class)
                    .setParameter("doctorId", doctorId)
                    .list();
        }
    }
}