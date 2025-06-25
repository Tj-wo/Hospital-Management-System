package org.pahappa.dao;

import org.hibernate.Session;
import org.pahappa.model.Admission;
import org.pahappa.model.User;
import org.pahappa.service.HibernateUtil;
import org.pahappa.utils.Role;

import java.util.List;

public class AdmissionDao extends BaseDao<Admission, Long> {

    public AdmissionDao() {
        super(Admission.class);
    }

    public long countActiveAdmissionsByNurse(Long nurseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select count(a) from Admission a where a.nurse.id = :nurseId and a.dischargeDate is null", Long.class)
                    .setParameter("nurseId", nurseId)
                    .getSingleResult();
        }
    }

    public List<Admission> findByPatientId(Long patientId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Admission where patient.id = :patientId", Admission.class)
                    .setParameter("patientId", patientId)
                    .list();
        }
    }

    public List<Admission> findByNurseId(Long nurseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Admission where nurse.id = :nurseId", Admission.class)
                    .setParameter("nurseId", nurseId)
                    .list();
        }
    }
}