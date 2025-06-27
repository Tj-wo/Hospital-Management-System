package org.pahappa.dao;

import org.hibernate.Session;
import org.pahappa.model.Admission;
import org.pahappa.service.HibernateUtil;
import java.util.List;

public class AdmissionDao extends BaseDao<Admission, Long> {

    public AdmissionDao() {
        super(Admission.class);
    }

    public long countActiveAdmissionsByNurse(Long nurseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(a) FROM Admission a WHERE a.nurse.id = :nurseId AND a.dischargeDate IS NULL AND a.deleted = false";
            return session.createQuery(hql, Long.class)
                    .setParameter("nurseId", nurseId)
                    .getSingleResult();
        }
    }

    public List<Admission> findByPatientId(Long patientId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Admission WHERE patient.id = :patientId AND deleted = false";
            return session.createQuery(hql, Admission.class)
                    .setParameter("patientId", patientId)
                    .list();
        }
    }

    public List<Admission> findByNurseId(Long nurseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Admission WHERE nurse.id = :nurseId AND deleted = false";
            return session.createQuery(hql, Admission.class)
                    .setParameter("nurseId", nurseId)
                    .list();
        }
    }
}