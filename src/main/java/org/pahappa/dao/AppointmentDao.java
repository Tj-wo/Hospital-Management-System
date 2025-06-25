package org.pahappa.dao;

import org.hibernate.Session;
import org.pahappa.model.Appointment;
import org.pahappa.service.HibernateUtil;

import java.util.List;

public class AppointmentDao extends BaseDao<Appointment, Long> {

    public AppointmentDao() {
        super(Appointment.class);
    }

    public List<Appointment> findByPatientId(Long patientId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Appointment where patient.id = :patientId order by appointmentDate asc", Appointment.class)
                    .setParameter("patientId", patientId)
                    .list();
        }
    }

    public List<Appointment> findByDoctorId(Long doctorId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Appointment where doctor.id = :doctorId order by appointmentDate asc", Appointment.class)
                    .setParameter("doctorId", doctorId)
                    .list();
        }
    }
}