package org.pahappa.dao;

import org.hibernate.Session;
import org.pahappa.model.Appointment;
import org.pahappa.service.HibernateUtil;

import javax.enterprise.context.ApplicationScoped;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class AppointmentDao extends BaseDao<Appointment, Long> {

    public AppointmentDao() {
        super(Appointment.class);
    }

    @Override
    public Appointment getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Appointment a " +
                    "JOIN FETCH a.patient " +
                    "JOIN FETCH a.doctor " +
                    "WHERE a.id = :id AND a.deleted = false";
            return session.createQuery(hql, Appointment.class)
                    .setParameter("id", id)
                    .uniqueResultOptional()
                    .orElse(null);
        }
    }

    @Override
    public List<Appointment> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Appointment a " +
                    "JOIN FETCH a.patient " +
                    "JOIN FETCH a.doctor " +
                    "WHERE a.deleted = false " +
                    "ORDER BY a.appointmentDate ASC";
            return session.createQuery(hql, Appointment.class)
                    .list();
        }
    }

    public List<Appointment> findByPatientId(Long patientId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Appointment a " +
                    "JOIN FETCH a.patient " +
                    "JOIN FETCH a.doctor " +
                    "WHERE a.patient.id = :patientId AND a.deleted = false " +
                    "ORDER BY a.appointmentDate ASC";
            return session.createQuery(hql, Appointment.class)
                    .setParameter("patientId", patientId)
                    .list();
        }
    }

    public List<Appointment> findByDoctorId(Long doctorId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Appointment a " +
                    "JOIN FETCH a.patient " +
                    "JOIN FETCH a.doctor " +
                    "WHERE a.doctor.id = :doctorId AND a.deleted = false " +
                    "ORDER BY a.appointmentDate ASC";
            return session.createQuery(hql, Appointment.class)
                    .setParameter("doctorId", doctorId)
                    .list();
        }
    }

    public List<Appointment> findFutureScheduledAppointmentsByDoctor(Long doctorId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Appointment a " +
                    "WHERE a.doctor.id = :doctorId " +
                    "AND a.appointmentDate > :now " +
                    "AND a.status = 'SCHEDULED' " +
                    "AND a.deleted = false";
            return session.createQuery(hql, Appointment.class)
                    .setParameter("doctorId", doctorId)
                    .setParameter("now", new Date()) // Compare against the current time
                    .list();
        }
    }
}
