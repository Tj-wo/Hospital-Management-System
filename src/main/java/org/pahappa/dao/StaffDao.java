package org.pahappa.dao;

import org.pahappa.model.Staff;

public class StaffDao extends BaseDao<Staff, Long> {
    public StaffDao() {
        super(Staff.class);
    }
}