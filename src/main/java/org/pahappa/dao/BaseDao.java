package org.pahappa.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pahappa.model.BaseModel;
import org.pahappa.service.HibernateUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;

public abstract class BaseDao<T extends BaseModel, ID extends Serializable> implements GenericDao<T, ID> {
    private final Class<T> entityClass;

    protected BaseDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public void save(T entity) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.saveOrUpdate(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to save/update " + entityClass.getSimpleName(), e);
        }
    }

    /* @Override
    public T getById(ID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> query = builder.createQuery(entityClass);
            Root<T> root = query.from(entityClass);
            query.select(root).where(
                    builder.and(
                            builder.equal(root.get("id"), id),
                            builder.equal(root.get("deleted"), false),
                            //builder.equal(root.get("deleted"), true)
                    )
            );
            return session.createQuery(query).uniqueResultOptional().orElse(null);
        }
    }*/

    @Override
    public T getById(ID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(entityClass, id);
        }
    }

    @Override
    public List<T> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> query = builder.createQuery(entityClass);
            Root<T> root = query.from(entityClass);
            query.select(root); // Remove WHERE clause to include all records
            return session.createQuery(query).list();
        }
    }

    @Override
    public void update(T entity) {
        this.save(entity);
    }

    @Override
    public void delete(ID id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            T entity = session.get(entityClass, id); // Use session.get() to bypass getById filter
            if (entity != null) {
                entity.setDeleted(true);
                session.update(entity);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to soft-delete " + entityClass.getSimpleName(), e);
        }
    }

    public List<T> getAllDeleted() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> query = builder.createQuery(entityClass);
            Root<T> root = query.from(entityClass);
            query.select(root).where(builder.equal(root.get("deleted"), true));
            return session.createQuery(query).list();
        }
    }
}