package org.pahappa.dao;

import org.hibernate.Session;
import org.pahappa.model.Role;
import org.pahappa.model.Staff;
import org.pahappa.service.HibernateUtil;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StaffDao extends BaseDao<Staff, Long> {
    public StaffDao() {
        super(Staff.class);
    }

    public long countByRole(Role role) {
        try (Session session =  HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(s.id) FROM Staff s WHERE s.role = :role AND s.deleted = false";
            return session.createQuery(hql, Long.class)
                    .setParameter("role", role)
                    .getSingleResult();
        }
    }
}