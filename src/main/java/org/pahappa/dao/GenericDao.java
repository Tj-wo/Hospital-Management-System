package org.pahappa.dao;

import org.pahappa.model.BaseModel;
import java.io.Serializable;
import java.util.List;

public interface GenericDao<T extends BaseModel, ID extends Serializable> {
    void save(T entity);
    T getById(ID id);
    List<T> getAll();
    void update(T entity);
    void delete(ID id);
}