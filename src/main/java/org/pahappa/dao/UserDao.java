package org.pahappa.dao;

import org.hibernate.Session;
import org.pahappa.model.User;
import org.pahappa.service.HibernateUtil;

import javax.persistence.NoResultException;

public class UserDao extends BaseDao<User, Long> {
    public UserDao() {
        super(User.class);
    }

    public User findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from User where username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Return null if no user is found
        }
    }
}