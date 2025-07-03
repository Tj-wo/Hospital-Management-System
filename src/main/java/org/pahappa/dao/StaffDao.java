package org.pahappa.dao;

import org.pahappa.model.Staff;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StaffDao extends BaseDao<Staff, Long> {
    public StaffDao() {
        super(Staff.class);
    }
}