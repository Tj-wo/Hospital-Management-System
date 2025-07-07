package org.pahappa.dao;
import org.pahappa.model.AuditLog;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuditLogDao extends BaseDao<AuditLog, Long> {

        public AuditLogDao() {
            super(AuditLog.class);
        }
}
