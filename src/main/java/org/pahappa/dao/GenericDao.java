package org.pahappa.dao;

import java.io.Serializable;
import java.util.List;

public interface GenericDao<T, ID extends Serializable> {
    void save(T entity);
    T getById(ID id);
    List<T> getAll();
    void update(T entity);
    void delete(ID id);
}