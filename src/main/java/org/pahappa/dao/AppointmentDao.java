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
            String hql = "FROM Appointment WHERE patient.id = :patientId AND deleted = false ORDER BY appointmentDate ASC";
            return session.createQuery(hql, Appointment.class)
                    .setParameter("patientId", patientId)
                    .list();
        }
    }

    public List<Appointment> findByDoctorId(Long doctorId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Appointment WHERE doctor.id = :doctorId AND deleted = false ORDER BY appointmentDate ASC";
            return session.createQuery(hql, Appointment.class)
                    .setParameter("doctorId", doctorId)
                    .list();
        }
    }
}