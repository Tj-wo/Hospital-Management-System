package org.pahappa.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pahappa.service.HibernateUtil;

import java.io.Serializable;
import java.util.List;

// Base class for all DAOs, providing common database operations like save, get, update, delete
public abstract class BaseDao<T, ID extends Serializable> implements GenericDao<T, ID> {
    private final Class<T> entityClass;

    // Constructor to set the entity class (e.g., Patient.class, Staff.class)
    protected BaseDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public void save(T entity) {
        // Open a session to interact with the database
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.save(entity); // Save the entity to the database
                tx.commit(); // Commit the transaction
            } catch (Exception e) {
                tx.rollback(); // Undo changes if error occurs
                throw new RuntimeException("Failed to save " + entityClass.getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public T getById(ID id) {
        // Open a session to retrieve an entity by its ID
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(entityClass, id); // Fetch entity from database
        }
    }

    @Override
    public List<T> getAll() {
        // Open a session to retrieve all entities of this type
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from " + entityClass.getSimpleName(), entityClass).list();
        }
    }

    @Override
    public void update(T entity) {
        // Open a session to update an entity
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.update(entity); // Update the entity in the database
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw new RuntimeException("Failed to update " + entityClass.getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void delete(ID id) {
        // Open a session to delete an entity by its ID
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                T entity = session.get(entityClass, id);
                if (entity != null) {
                    session.delete(entity); // Delete the entity
                }
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw new RuntimeException("Failed to delete " + entityClass.getSimpleName() + ": " + e.getMessage());
            }
        }
    }
}