package org.pahappa.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pahappa.model.Admission;
import org.pahappa.service.HibernateUtil;
import java.util.List;

public class AdmissionDao {
    public void save(Admission admission) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.save(admission);
        tx.commit();
        session.close();
    }

    public Admission getById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Admission admission = (Admission) session.get(Admission.class, id);
        session.close();
        return admission;
    }

    public List<Admission> getAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Admission> admissions = session.createQuery("from Admission").list();
        session.close();
        return admissions;
    }

    public List<Admission> getByPatientId(Long patientId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Admission> admissions = session.createQuery("from Admission where patient.id = :patientId")
                .setParameter("patientId", patientId)
                .list();
        session.close();
        return admissions;
    }

    public void update(Admission admission) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.update(admission);
        tx.commit();
        session.close();
    }

    public void delete(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        Admission admission = (Admission) session.get(Admission.class, id);
        if (admission != null) session.delete(admission);
        tx.commit();
        session.close();
    }
}