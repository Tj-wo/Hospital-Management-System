package org.pahappa.dao;

import org.hibernate.Session;
import org.pahappa.model.RolePermission;
import org.pahappa.service.HibernateUtil;
import org.pahappa.utils.PermissionType;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class RolePermissionDao extends BaseDao<RolePermission, Long> {

    public RolePermissionDao() {
        super(RolePermission.class);
    }

    public List<RolePermission> findByRoleId(Long roleId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM RolePermission rp JOIN FETCH rp.role WHERE rp.role.id = :roleId AND rp.deleted = false";
            return session.createQuery(hql, RolePermission.class)
                    .setParameter("roleId", roleId)
                    .list();
        }
    }

    public RolePermission findByRoleIdAndPermissionType(Long roleId, PermissionType permissionType) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // ADDED: JOIN FETCH rp.role to prevent LazyInitializationException in the service layer when logging the revocation.
            String hql = "FROM RolePermission rp JOIN FETCH rp.role WHERE rp.role.id = :roleId AND rp.permission = :permissionType AND rp.deleted = false";
            return session.createQuery(hql, RolePermission.class)
                    .setParameter("roleId", roleId)
                    .setParameter("permissionType", permissionType)
                    .uniqueResultOptional()
                    .orElse(null);
        }
    }

    public RolePermission findByRoleIdAndPermissionTypeIncludingDeleted(Long roleId, PermissionType permissionType) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // This query finds the permission regardless of its 'deleted' status.
            // ADDED: JOIN FETCH rp.role to prevent LazyInitializationException in the service layer.
            String hql = "FROM RolePermission rp JOIN FETCH rp.role WHERE rp.role.id = :roleId AND rp.permission = :permissionType";
            return session.createQuery(hql, RolePermission.class)
                    .setParameter("roleId", roleId)
                    .setParameter("permissionType", permissionType)
                    .uniqueResultOptional()
                    .orElse(null);
        }
    }
}