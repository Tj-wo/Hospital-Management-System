package org.pahappa.dao;

import org.hibernate.Session;
import org.pahappa.model.Appointment;
import org.pahappa.service.HibernateUtil;
import org.pahappa.utils.AppointmentStatus;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String, Long> getMonthlyAppointmentCreations(int months) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime cutOffDate = LocalDateTime.now().minusMonths(months);

            String hql = "SELECT year(a.dateCreated), month(a.dateCreated), count(a.id) " +
                    "FROM Appointment a " +
                    "WHERE a.dateCreated >= :cutOffDate AND a.deleted = false " +
                    "GROUP BY year(a.dateCreated), month(a.dateCreated) " +
                    "ORDER BY year(a.dateCreated), month(a.dateCreated)";

            List<Object[]> results = session.createQuery(hql, Object[].class)
                    .setParameter("cutOffDate", Date.from(cutOffDate.atZone(ZoneId.systemDefault()).toInstant()))
                    .list();

            Map<String, Long> counts = new LinkedHashMap<>();
            for (Object[] row : results) {
                String yearMonth = String.format("%d-%02d", row[0], row[1]);
                counts.put(yearMonth, (Long) row[2]);
            }
            return counts;
        }
    }

    public Map<String, Long> getDoctorPerformance(int days) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime cutOffDate = LocalDateTime.now().minusDays(days);

            String hql = "SELECT s.firstName, s.lastName, COUNT(a.id) " +
                    "FROM Appointment a " +
                    "JOIN a.doctor s " +
                    "WHERE a.appointmentDate >= :cutOffDate AND a.status = :status AND a.deleted = false " +
                    "GROUP BY s.id, s.firstName, s.lastName " +
                    "ORDER BY COUNT(a.id) DESC";

            List<Object[]> results = session.createQuery(hql, Object[].class)
                    .setParameter("cutOffDate", Date.from(cutOffDate.atZone(ZoneId.systemDefault()).toInstant()))
                    .setParameter("status", AppointmentStatus.COMPLETED)
                    .setMaxResults(10)
                    .list();

            Map<String, Long> performanceData = new LinkedHashMap<>();
            for (Object[] result : results) {
                String doctorName = result[0] + " " + result[1];
                performanceData.put(doctorName, (Long) result[2]);
            }
            return performanceData;
        }
    }

    public Map<LocalDate, Long> getDailyAppointmentCountForDoctor(Long doctorId, int days) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days);

            String hql = "SELECT date(a.appointmentDate), COUNT(a.id) " +
                    "FROM Appointment a " +
                    "WHERE a.doctor.id = :doctorId AND a.deleted = false " +
                    "AND date(a.appointmentDate) > :startDate " +
                    "GROUP BY date(a.appointmentDate) " +
                    "ORDER BY date(a.appointmentDate) ASC";

            List<Object[]> results = session.createQuery(hql, Object[].class)
                    .setParameter("doctorId", doctorId)
                    .setParameter("startDate", java.sql.Date.valueOf(startDate))
                    .list();

            Map<LocalDate, Long> dailyCounts = new LinkedHashMap<>();
            for (Object[] result : results) {
                Date dateResult = (Date) result[0];
                dailyCounts.put(new java.sql.Date(dateResult.getTime()).toLocalDate(), (Long) result[1]);
            }
            return dailyCounts;
        }
    }
}
