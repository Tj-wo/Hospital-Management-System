package org.pahappa.dao;

import org.pahappa.model.Staff;

// DAO class for Staff entity, handling database operations
public class StaffDao extends BaseDao<Staff, Long> {

    // Constructor to initialize BaseDao with Staff class
    public StaffDao() {
        super(Staff.class);
    }
}