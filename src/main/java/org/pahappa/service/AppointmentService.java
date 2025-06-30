package org.pahappa.service;

import org.pahappa.model.Appointment;
import org.pahappa.utils.AppointmentStatus;

import java.util.List;

public interface AppointmentService {
    List<Appointment> getAllAppointments();
    Appointment getAppointmentById(Long appointmentId);
    void scheduleAppointment(Appointment appointment);
    void updateAppointment(Appointment appointment);
    void cancelAppointment(Long appointmentId, boolean cancelledByPatient);
    List<Appointment> getAppointmentsForDoctor(long doctorId);
    List<Appointment> getAppointmentsForPatient(long patientId);
    void updateAppointmentStatus(Long appointmentId, AppointmentStatus newStatus);
}