package org.pahappa.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pahappa.model.Appointment;
import org.pahappa.service.HibernateUtil;
import java.time.LocalDateTime;
import java.util.List;

public class AppointmentDao {
    public void save(Appointment appointment) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.save(appointment);
        tx.commit();
        session.close();
    }

    public Appointment getById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Appointment appointment = (Appointment) session.get(Appointment.class, id);
        session.close();
        return appointment;
    }

    public List<Appointment> getAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Appointment> appointments = session.createQuery("from Appointment").list();
        session.close();
        return appointments;
    }

    public List<Appointment> getByDoctorId(Long doctorId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Appointment> appointments = session.createQuery("from Appointment where doctor.id = :doctorId")
                .setParameter("doctorId", doctorId)
                .list();
        session.close();
        return appointments;
    }

    public void update(Appointment appointment) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.update(appointment);
        tx.commit();
        session.close();
    }

    public void delete(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        Appointment appointment = (Appointment) session.get(Appointment.class, id);
        if (appointment != null) session.delete(appointment);
        tx.commit();
        session.close();
    }

    public long countByDoctorAndDate(Long doctorId, LocalDateTime date) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Long count = (Long) session.createQuery("select count(*) from Appointment where doctor.id = :doctorId and appointmentDate = :date")
                .setParameter("doctorId", doctorId)
                .setParameter("date", date)
                .uniqueResult();
        session.close();
        return count != null ? count : 0;
    }
}