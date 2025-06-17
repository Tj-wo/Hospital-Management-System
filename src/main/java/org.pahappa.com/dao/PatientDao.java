<<<<<<< Updated upstream
=======
package org.pahappa.com.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pahappa.com.model.Patient;
import org.pahappa.com.service.HibernateUtil;
import java.util.List;

public class PatientDao {
    public void save(Patient patient) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.save(patient);
        tx.commit();
        session.close();
    }

    public Patient getById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Patient patient = (Patient) session.get(Patient.class, id);
        session.close();
        return patient;
    }

    public List<Patient> getAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Patient> patients = session.createQuery("from Patient").list();
        session.close();
        return patients;
    }

    public void update(Patient patient) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.update(patient);
        tx.commit();
        session.close();
    }

    public void delete(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        Patient patient = (Patient) session.get(Patient.class, id);
        if (patient != null) session.delete(patient);
        tx.commit();
        session.close();
    }
<<<<<<< Updated upstream
}
>>>>>>> Stashed changes
=======
}
>>>>>>> Stashed changes
