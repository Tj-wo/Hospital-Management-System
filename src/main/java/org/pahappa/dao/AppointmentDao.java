package org.pahappa.dao;

import org.pahappa.model.Appointment;

// DAO class for Appointment entity, handling database operations
public class AppointmentDao extends BaseDao<Appointment, Long> {

    // Constructor to initialize BaseDao with Appointment class
    public AppointmentDao() {
        super(Appointment.class);
    }
}