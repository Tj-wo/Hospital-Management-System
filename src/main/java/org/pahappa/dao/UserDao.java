package org.pahappa.dao;

import org.hibernate.Session;
import org.pahappa.model.User;
import org.pahappa.service.HibernateUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.List;


@ApplicationScoped
public class UserDao extends BaseDao<User, Long> {
    public UserDao() {
        super(User.class);
    }

    /**
     * Finds a user who is both active and not soft-deleted.
     * A user whose profile was deleted should not be able to log in.
     */
    public User findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<User> query = builder.createQuery(User.class);
            Root<User> root = query.from(User.class);
            query.select(root).where(
                    builder.and(
                            builder.equal(root.get("username"), username),
                            builder.equal(root.get("isActive"), true),
                            builder.equal(root.get("deleted"), false) // Ensures soft-deleted users cannot log in
                    )
            );
            return session.createQuery(query).uniqueResultOptional().orElse(null);
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Finds users that are explicitly deactivated (isActive=false) but NOT soft-deleted.
     */
    public List<User> findDeactivated() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<User> query = builder.createQuery(User.class);
            Root<User> root = query.from(User.class);

            query.select(root).where(
                    builder.and(
                            builder.equal(root.get("isActive"), false),
                            builder.equal(root.get("deleted"), false) // This is the key for correct retrieval
                    )
            ).orderBy(builder.desc(root.get("dateDeactivated")));
            return session.createQuery(query).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * It looks for active users whose associated Patient or Staff profile has been soft-deleted.
     */
    public List<User> findLegacyDeletedUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // HQL is clearer for this type of join-based condition
            String hql = "SELECT u FROM User u " +
                    "LEFT JOIN u.patient p " +
                    "LEFT JOIN u.staff s " +
                    "WHERE u.isActive = true AND u.deleted = false " +
                    "AND (p.deleted = true OR s.deleted = true)";

            return session.createQuery(hql, User.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}