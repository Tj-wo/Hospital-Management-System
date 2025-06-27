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
            String hql = "FROM MedicalRecord WHERE patient.id = :patientId AND deleted = false ORDER BY recordDate DESC";
            return session.createQuery(hql, MedicalRecord.class)
                    .setParameter("patientId", patientId)
                    .list();
        }
    }

    public List<MedicalRecord> getByDoctorId(Long doctorId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM MedicalRecord WHERE doctor.id = :doctorId AND deleted = false ORDER BY recordDate DESC";
            return session.createQuery(hql, MedicalRecord.class)
                    .setParameter("doctorId", doctorId)
                    .list();
        }
    }
}