package org.pahappa.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pahappa.model.Staff;
import org.pahappa.service.HibernateUtil;
import java.util.List;

public class StaffDao {
    public void save(Staff staff) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.save(staff);
        tx.commit();
        session.close();
    }

    public Staff getById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Staff staff = (Staff) session.get(Staff.class, id);
        session.close();
        return staff;
    }

    public List<Staff> getAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Staff> staff = session.createQuery("from Staff").list();
        session.close();
        return staff;
    }

    public List<Staff> getDoctorsBySpecialty(String specialty) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Staff> staff = session.createQuery("from Staff where role = 'DOCTOR' and specialty = :specialty")
                .setParameter("specialty", specialty)
                .list();
        session.close();
        return staff;
    }

    public void update(Staff staff) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.update(staff);
        tx.commit();
        session.close();
    }

    public void delete(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        Staff staff = (Staff) session.get(Staff.class, id);
        if (staff != null) session.delete(staff);
        tx.commit();
        session.close();
    }
}