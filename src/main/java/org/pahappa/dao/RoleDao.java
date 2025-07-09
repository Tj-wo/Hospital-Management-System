package org.pahappa.dao;

import org.hibernate.Session;
import org.pahappa.dao.BaseDao;
import org.pahappa.model.Role;
import org.pahappa.service.HibernateUtil;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.NoResultException;

@ApplicationScoped
public class RoleDao extends BaseDao<Role, Long> {

    public RoleDao() {
        super(Role.class);
    }

    public Role findByName(String roleName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Role WHERE name = :roleName AND deleted = false";
            return session.createQuery(hql, Role.class)
                    .setParameter("roleName", roleName)
                    .uniqueResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}