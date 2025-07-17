package org.pahappa.dao;

import org.pahappa.model.Patient;

import org.hibernate.Session;
import org.pahappa.service.HibernateUtil;
import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PatientDao extends BaseDao<Patient, Long> {
    public PatientDao() {
        super(Patient.class);
    }

    public Map<String, Long> getMonthlyPatientRegistrations(int months) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime cutOffDate = LocalDateTime.now().minusMonths(months);

            String hql = "SELECT year(p.dateCreated), month(p.dateCreated), count(p.id) " +
                    "FROM Patient p " +
                    "WHERE p.dateCreated >= :cutOffDate AND p.deleted = false " +
                    "GROUP BY year(p.dateCreated), month(p.dateCreated) " +
                    "ORDER BY year(p.dateCreated), month(p.dateCreated)";

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
}