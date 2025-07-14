package org.pahappa.service.audit.impl;

import org.pahappa.dao.AuditLogDao;
import org.pahappa.model.AuditLog;
import org.pahappa.model.AuditLog.ActionType;
import org.pahappa.model.BaseModel;
import org.pahappa.service.audit.AuditService;

import javax.annotation.PostConstruct; // <-- ADD THIS IMPORT
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AuditServiceImpl implements AuditService {

    @Inject
    private AuditLogDao auditLogDao;

    // ADD THIS CONSTRUCTOR
    public AuditServiceImpl() {
    }

    // ADD THIS @POSTCONSTRUCT METHOD
    @PostConstruct
    public void init() {
        System.out.println("### AuditServiceImpl: @PostConstruct called. auditLogDao is: " + auditLogDao);
        if (auditLogDao == null) {
            System.err.println("### CRITICAL CDI ERROR: auditLogDao is NULL after @Inject in @PostConstruct!");
        }
    }

    // Helper method to convert String userId/entityId to Long safely
    private Long parseIdToLong(String idString) {
        if (idString == null || idString.trim().isEmpty() || "N/A".equals(idString)) {
            return null;
        }
        try {
            return Long.parseLong(idString);
        } catch (NumberFormatException e) {

            System.err.println("Warning: Could not parse ID '" + idString + "' to Long. " + e.getMessage());
            return null;
        }
    }


    @Override
    public void logCreate(BaseModel entity, String userId, String username, String details) {
        validateArguments(entity, userId, username);
        Long userIdLong = parseIdToLong(userId);
        Long entityIdLong = parseIdToLong(entity.getId() != null ? entity.getId().toString() : null);

        AuditLog log = new AuditLog(
                new Date(),
                username,
                userIdLong,
                ActionType.CREATE,
                entity.getClass().getSimpleName(),
                entityIdLong,
                null,
                entity.toString(),
                null,
                details
        );
        // Add diagnostic print here
        System.out.println("### AuditServiceImpl.logCreate: Attempting to save log. auditLogDao is: " + auditLogDao);
        if (auditLogDao != null) {
            auditLogDao.save(log);
        } else {
            System.err.println("### FATAL: auditLogDao is NULL during logCreate! Cannot save log.");

        }
    }

    @Override
    public void logUpdate(BaseModel original, BaseModel updated, String userId, String username, String details) {
        validateArguments(original, userId, username);
        if (updated == null) {
            throw new IllegalArgumentException("Updated entity cannot be null");
        }
        Long userIdLong = parseIdToLong(userId);
        Long entityIdLong = parseIdToLong(original.getId() != null ? original.getId().toString() : null);

        AuditLog log = new AuditLog(
                new Date(),
                username,
                userIdLong,
                ActionType.UPDATE,
                original.getClass().getSimpleName(),
                entityIdLong,
                original.toString(),
                updated.toString(),
                null,
                details
        );
        System.out.println("### AuditServiceImpl.logUpdate: Attempting to save log. auditLogDao is: " + auditLogDao);
        if (auditLogDao != null) {
            auditLogDao.save(log);
        } else {
            System.err.println("### FATAL: auditLogDao is NULL during logUpdate! Cannot save log.");
        }
    }

    @Override
    public void logDelete(BaseModel entity, String userId, String username, String details) {
        validateArguments(entity, userId, username);
        Long userIdLong = parseIdToLong(userId);
        Long entityIdLong = parseIdToLong(entity.getId() != null ? entity.getId().toString() : null);

        AuditLog log = new AuditLog(
                new Date(),
                username,
                userIdLong,
                ActionType.DELETE,
                entity.getClass().getSimpleName(),
                entityIdLong,
                entity.toString(),
                null,
                null,
                details
        );
        System.out.println("### AuditServiceImpl.logDelete: Attempting to save log. auditLogDao is: " + auditLogDao);
        if (auditLogDao != null) {
            auditLogDao.save(log);
        } else {
            System.err.println("### FATAL: auditLogDao is NULL during logDelete! Cannot save log.");
        }
    }

    @Override
    public void logLogin(String userId, String username, String details) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty for login log");
        }
        Long userIdLong = parseIdToLong(userId);

        AuditLog log = new AuditLog(
                new Date(),
                username,
                userIdLong,
                ActionType.LOGIN,
                "User",
                userIdLong,
                null,
                null,
                null,
                details
        );
        // THIS IS LINE 124 IN THE ORIGINAL CODE
        System.out.println("### AuditServiceImpl.logLogin (Line 124): Attempting to save log. auditLogDao is: " + auditLogDao);
        if (auditLogDao != null) { // ADDED THIS NULL CHECK
            auditLogDao.save(log); // Uncommented this line, but with a null check
        } else {
            System.err.println("### FATAL: auditLogDao is NULL during logLogin! Cannot save log.");
            // You can re-enable throwing an exception here if you want it to fail explicitly
            // throw new RuntimeException("AuditLogDao was not injected correctly for logLogin.");
        }
    }

    @Override
    public List<AuditLog> getRecentLogs(int count) {
        System.out.println("### AuditServiceImpl.getRecentLogs: Calling auditLogDao.getAll(). auditLogDao is: " + auditLogDao);
        if (auditLogDao == null) {
            System.err.println("### FATAL: auditLogDao is NULL during getRecentLogs! Cannot retrieve logs.");
            return Collections.emptyList();
        }
        List<AuditLog> allLogs = auditLogDao.getAll();
        return allLogs.stream()
                .sorted(Comparator.comparing(AuditLog::getDateCreated, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLog> getAuditLogsByEntity(String entityType, String entityId) {
        Long entityIdLong = parseIdToLong(entityId); // Convert string ID to Long

        System.out.println("### AuditServiceImpl.getAuditLogsByEntity: Calling auditLogDao.getAll(). auditLogDao is: " + auditLogDao);
        if (auditLogDao == null) {
            return Collections.emptyList();
        }
        List<AuditLog> allLogs = auditLogDao.getAll();
        List<AuditLog> filteredLogs = allLogs.stream()
                .filter(log -> log.getEntityType() != null && log.getEntityType().equals(entityType) &&
                        (entityIdLong == null || (log.getEntityId() != null && log.getEntityId().equals(entityIdLong)))) // Compare Longs
                .sorted(Comparator.comparing(AuditLog::getDateCreated, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
        return filteredLogs;
    }

    @Override
    public List<AuditLog> getAuditLogsByUser(String userId) {
        Long userIdLong = parseIdToLong(userId); // Convert string ID to Long

        System.out.println("### AuditServiceImpl.getAuditLogsByUser: Calling auditLogDao.getAll(). auditLogDao is: " + auditLogDao);
        if (auditLogDao == null) {
            System.err.println("### FATAL: auditLogDao is NULL during getAuditLogsByUser! Cannot retrieve logs.");
            return Collections.emptyList();
        }
        List<AuditLog> allLogs = auditLogDao.getAll();
        List<AuditLog> filteredLogs = allLogs.stream()
                .filter(log -> (userIdLong == null || (log.getUserId() != null && log.getUserId().equals(userIdLong)))) // Compare Longs
                .sorted(Comparator.comparing(AuditLog::getDateCreated, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
        return filteredLogs;
    }

    @Override
    public List<AuditLog> getAuditLogsByAction(ActionType action) {
        System.out.println("### AuditServiceImpl.getAuditLogsByAction: Calling auditLogDao.getAll(). auditLogDao is: " + auditLogDao);
        if (auditLogDao == null) {
            System.err.println("### FATAL: auditLogDao is NULL during getAuditLogsByAction! Cannot retrieve logs.");
            return Collections.emptyList();
        }
        List<AuditLog> allLogs = auditLogDao.getAll();
        List<AuditLog> filteredLogs = allLogs.stream()
                .filter(log -> log.getAction() == action)
                .sorted(Comparator.comparing(AuditLog::getDateCreated, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
        return filteredLogs;
    }

    @Override
    public List<AuditLog> getAuditLogsByDateRange(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Both start and end dates are required for date range filter.");
        }

        System.out.println("### AuditServiceImpl.getAuditLogsByDateRange: Calling auditLogDao.getAll(). auditLogDao is: " + auditLogDao);
        if (auditLogDao == null) {
            System.err.println("### FATAL: auditLogDao is NULL during getAuditLogsByDateRange! Cannot retrieve logs.");
            return Collections.emptyList();
        }
        List<AuditLog> allLogs = auditLogDao.getAll();
        List<AuditLog> filteredLogs = allLogs.stream()
                .filter(log -> {
                    Date created = log.getDateCreated();
                    return created != null && !created.before(startDate) && !created.after(endDate);
                })
                .sorted(Comparator.comparing(AuditLog::getDateCreated, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
        return filteredLogs;
    }

    @Override
    public List<AuditLog> getFilteredLogs(Date startDate, Date endDate, ActionType actionType, String entityType) {
        System.out.println("### AuditServiceImpl.getFilteredLogs: Calling auditLogDao.getAll(). auditLogDao is: " + auditLogDao);
        if (auditLogDao == null) {
            System.err.println("### FATAL: auditLogDao is NULL during getFilteredLogs! Cannot retrieve logs.");
            return Collections.emptyList();
        }
        List<AuditLog> allLogs = auditLogDao.getAll();

        List<AuditLog> filtered = allLogs.stream()
                .filter(log -> {
                    boolean match = true;

                    // Date range filter
                    if (startDate != null && log.getDateCreated() != null && log.getDateCreated().before(startDate)) {
                        match = false;
                    }
                    if (endDate != null && log.getDateCreated() != null && log.getDateCreated().after(endDate)) {
                        match = false;
                    }

                    // Action type filter
                    if (actionType != null && log.getAction() != actionType) {
                        match = false;
                    }

                    // Entity type filter
                    if (entityType != null && !entityType.trim().isEmpty() && log.getEntityType() != null && !log.getEntityType().equalsIgnoreCase(entityType)) {
                        match = false;
                    }

                    return match;
                })
                .sorted(Comparator.comparing(AuditLog::getDateCreated, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        return filtered;
    }


    private void validateArguments(BaseModel entity, String userId, String username) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
    }
}