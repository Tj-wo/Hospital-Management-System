package org.pahappa.dao;

import org.hibernate.Session;
import org.pahappa.model.User;
import org.pahappa.service.HibernateUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.NoResultException;


@ApplicationScoped
public class UserDao extends BaseDao<User, Long> {
    public UserDao() {
        super(User.class);
    }

    public User findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM User WHERE username = :username AND deleted = false";
            return session.createQuery(hql, User.class)
                    .setParameter("username", username)
                    .uniqueResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}