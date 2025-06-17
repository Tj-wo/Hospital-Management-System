package org.pahappa.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pahappa.model.MedicalRecord;
import org.pahappa.service.HibernateUtil;
import java.util.List;

public class MedicalRecordDao {
    public void save(MedicalRecord record) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.save(record);
        tx.commit();
        session.close();
    }

    public MedicalRecord getById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        MedicalRecord record = (MedicalRecord) session.get(MedicalRecord.class, id);
        session.close();
        return record;
    }

    public List<MedicalRecord> getAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<MedicalRecord> records = session.createQuery("from MedicalRecord").list();
        session.close();
        return records;
    }

    public List<MedicalRecord> getByPatientId(Long patientId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<MedicalRecord> records = session.createQuery("from MedicalRecord where patient.id = :patientId")
                .setParameter("patientId", patientId)
                .list();
        session.close();
        return records;
    }

    public void update(MedicalRecord record) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.update(record);
        tx.commit();
        session.close();
    }

    public void delete(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        MedicalRecord record = (MedicalRecord) session.get(MedicalRecord.class, id);
        if (record != null) session.delete(record);
        tx.commit();
        session.close();
    }
}