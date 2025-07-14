package org.pahappa.dao;
import org.pahappa.model.AuditLog;

import javax.enterprise.context.ApplicationScoped;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.pahappa.service.HibernateUtil;

@ApplicationScoped
public class AuditLogDao extends BaseDao<AuditLog, Long> {

    public AuditLogDao() {
            super(AuditLog.class);
        }


    public List<AuditLog> findWithFilters(Date startDate, Date endDate, AuditLog.ActionType actionType, String entityType) {
        StringBuilder queryStr = new StringBuilder("FROM AuditLog a WHERE 1=1");
        Map<String, Object> parameters = new HashMap<>();

        if (startDate != null) {
            queryStr.append(" AND a.dateCreated >= :startDate");
            parameters.put("startDate", startDate);
        }
        if (endDate != null) {
            // To include the whole day, we should check for less than the start of the next day
            queryStr.append(" AND a.dateCreated < :endDate");
            parameters.put("endDate", endDate);
        }
        if (actionType != null) {
            queryStr.append(" AND a.action = :actionType");
            parameters.put("actionType", actionType);
        }
        if (entityType != null && !entityType.trim().isEmpty()) {
            queryStr.append(" AND a.entityType = :entityType");
            parameters.put("entityType", entityType);
        }
        queryStr.append(" ORDER BY a.dateCreated DESC");

        // Execute the query
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<AuditLog> query = session.createQuery(queryStr.toString(), AuditLog.class);
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
            return query.list();
        }
    }
}
