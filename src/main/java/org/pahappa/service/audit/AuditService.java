package org.pahappa.service.audit;

import org.pahappa.model.AuditLog;
import org.pahappa.model.BaseModel;

import java.util.Date;
import java.util.List;

public interface AuditService {
    void logCreate(BaseModel entity, String userId, String username, String details);
    void logUpdate(BaseModel original, BaseModel updated, String userId, String username, String details);
    void logDelete(BaseModel entity, String userId, String username, String details);
    void logLogin(String userId, String username, String details);

    List<AuditLog> getAuditLogsByEntity(String entityType, String entityId);
    List<AuditLog> getAuditLogsByUser(String userId);
    List<AuditLog> getAuditLogsByAction(AuditLog.ActionType action);
    List<AuditLog> getAuditLogsByDateRange(Date startDate, Date endDate);
    List<AuditLog> getFilteredLogs(Date startDate, Date endDate, AuditLog.ActionType actionType, String entityType);
    List<AuditLog> getRecentLogs(int i);
}