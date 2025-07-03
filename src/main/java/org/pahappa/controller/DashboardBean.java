package org.pahappa.controller;

import org.pahappa.model.Appointment;
import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
import org.pahappa.service.admission.AdmissionService;
import org.pahappa.service.appointment.AppointmentService;
import org.pahappa.service.patient.PatientService;
import org.pahappa.service.staff.StaffService;
import org.pahappa.utils.AppointmentStatus;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Named("dashboardBean")
@SessionScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // Admin Stats
    private long totalPatients;
    private long totalAppointments;
    private long totalStaff;

    // Doctor Stats
    private List<Appointment> todaysAppointments;

    // Patient Stats
    private List<Appointment> upcomingAppointments;

    // Nurse Stats
    private long myActivePatientCount;

    // Receptionist Stats
    private long allTodaysAppointmentsCount;


    @Inject private PatientService patientService;
    @Inject private StaffService staffService;
    @Inject private AppointmentService appointmentService;
    @Inject private AdmissionService admissionService;
    @Inject private LoginBean loginBean;

    @PostConstruct
    public void init() {
        if (loginBean.getLoggedInUser() == null) {
            resetFields();
            return;
        }
        resetFields();
        switch (loginBean.getLoggedInUser().getRole()) {
            case ADMIN:         loadAdminData();        break;
            case DOCTOR:        loadDoctorData();       break;
            case PATIENT:       loadPatientData();      break;
            case NURSE:         loadNurseData();        break;
            case RECEPTIONIST:  loadReceptionistData(); break;
        }
    }

    private void loadAdminData() {
        totalPatients = patientService.countPatients();
        totalAppointments = appointmentService.countAppointments();
        totalStaff = staffService.getAllStaff().size();
    }

    private void loadDoctorData() {
        Staff doctor = loginBean.getLoggedInUser().getStaff();
        if (doctor == null) return;
        todaysAppointments = filterForToday(appointmentService.getAppointmentsForDoctor(doctor.getId()));
    }

    private void loadPatientData() {
        Patient patient = loginBean.getLoggedInUser().getPatient();
        if (patient == null) return;
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        upcomingAppointments = appointmentService.getAppointmentsByPatient(patient).stream()
                .filter(a -> a.getAppointmentDate() != null && a.getAppointmentDate().after(now) && a.getStatus() == AppointmentStatus.SCHEDULED)
                .collect(Collectors.toList());
    }

    private void loadNurseData() {
        Staff nurse = loginBean.getLoggedInUser().getStaff();
        if (nurse == null) return;
        myActivePatientCount = admissionService.getAdmissionsForNurse(nurse.getId()).stream()
                .filter(a -> a.getDischargeDate() == null).count();
    }

    private void loadReceptionistData() {
        allTodaysAppointmentsCount = filterForToday(appointmentService.getAllAppointments()).size();
    }

    private List<Appointment> filterForToday(List<Appointment> appointments) {
        if(appointments == null) return Collections.emptyList();
        LocalDate today = LocalDate.now();
        Timestamp startOfDay = Timestamp.valueOf(LocalDateTime.of(today, LocalTime.MIN));
        Timestamp endOfDay = Timestamp.valueOf(LocalDateTime.of(today, LocalTime.MAX));
        return appointments.stream()
                .filter(a -> a.getAppointmentDate() != null && !a.getAppointmentDate().before(startOfDay) && !a.getAppointmentDate().after(endOfDay))
                .collect(Collectors.toList());
    }

    private void resetFields() {
        totalPatients = 0;
        totalAppointments = 0;
        totalStaff = 0;
        myActivePatientCount = 0;
        allTodaysAppointmentsCount = 0;
        todaysAppointments = Collections.emptyList();
        upcomingAppointments = Collections.emptyList();
    }

    // Getters
    public long getTotalPatients() { return totalPatients; }
    public long getTotalAppointments() { return totalAppointments; }
    public long getTotalStaff() { return totalStaff; }
    public List<Appointment> getTodaysAppointments() { return todaysAppointments; }
    public List<Appointment> getUpcomingAppointments() { return upcomingAppointments; }
    public long getMyActivePatientCount() { return myActivePatientCount; }
    public long getAllTodaysAppointmentsCount() { return allTodaysAppointmentsCount; }
}