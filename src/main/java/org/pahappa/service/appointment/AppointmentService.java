package org.pahappa.service.appointment;

import org.pahappa.exception.HospitalServiceException;
import org.pahappa.model.Appointment;
import org.pahappa.model.Patient;
import org.pahappa.utils.AppointmentStatus;

import java.util.List;

public interface AppointmentService {
    List getAllAppointments();
    Appointment getAppointmentById(Long appointmentId);
    void scheduleAppointment(Appointment appointment) throws HospitalServiceException;
    void updateAppointment(Appointment appointment) throws HospitalServiceException;
    void cancelAppointment(Long appointmentId, boolean cancelledByPatient) throws HospitalServiceException;
    List getAppointmentsForDoctor(long doctorId);
    List getAppointmentsForPatient(long patientId);
    void updateAppointmentStatus(Long appointmentId, AppointmentStatus newStatus) throws HospitalServiceException;
    List getAppointmentsByPatient(Patient patient);
    long countAppointments();

}