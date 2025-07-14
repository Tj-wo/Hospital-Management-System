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

    public User findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Your existing HQL is fine, but using Criteria API is more type-safe and consistent
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<User> query = builder.createQuery(User.class);
            Root<User> root = query.from(User.class);
            query.select(root).where(
                    builder.and(
                            builder.equal(root.get("username"), username),
                            // Also ensure the user account itself is active
                            builder.equal(root.get("isActive"), true)
                    )
            );
            return session.createQuery(query).uniqueResultOptional().orElse(null);
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<User> findDeactivated() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<User> query = builder.createQuery(User.class);
            Root<User> root = query.from(User.class);

            // This query specifically looks for users where isActive is false
            query.select(root).where(builder.equal(root.get("isActive"), false))
                    .orderBy(builder.desc(root.get("dateDeactivated")));
            return session.createQuery(query).list();
        } catch (Exception e) {
            // In a real application, use a logger instead of printStackTrace
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}