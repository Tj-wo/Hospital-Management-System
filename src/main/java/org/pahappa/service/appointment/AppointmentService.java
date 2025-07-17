package org.pahappa.service.appointment;

import org.pahappa.exception.HospitalServiceException;
import org.pahappa.model.Appointment;
import org.pahappa.model.Patient;
import org.pahappa.utils.AppointmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AppointmentService {
    List <Appointment> getAllAppointments();
    Appointment getAppointmentById(Long appointmentId);
    // The service method now declares what it needs
    void scheduleAppointment(Appointment appointment, String userId, String username) throws HospitalServiceException;
    void updateAppointment(Appointment appointment) throws HospitalServiceException;
    void cancelAppointment(Long appointmentId, boolean cancelledByPatient) throws HospitalServiceException;
    List <Appointment> getAppointmentsForDoctor(long doctorId);
    List <Appointment> getAppointmentsForPatient(long patientId);
    void updateAppointmentStatus(Long appointmentId, AppointmentStatus newStatus) throws HospitalServiceException;
    List <Appointment> getAppointmentsByPatient(Patient patient);
    long countAppointments();
    void handleDeactivatedDoctorAppointments(long doctorId) throws HospitalServiceException;
    Map<String, Long> getMonthlyAppointmentCreations(int months);
    Map<String, Long> getDoctorPerformance(int days);
    Map<LocalDate, Long> getDailyAppointmentCountForDoctor(Long doctorId, int days);

}