package org.pahappa.controller;

import org.pahappa.model.Appointment;
import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
import org.pahappa.service.AppointmentService;
import org.pahappa.service.PatientService;
import org.pahappa.service.StaffService;
import org.pahappa.utils.AppointmentStatus;
import org.pahappa.utils.Role;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Named("dashboardBean")
@SessionScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private long totalPatients;
    private long totalAppointments;
    private long totalStaff;

    private List<Appointment> todaysAppointments;
    private List<Appointment> upcomingAppointments;

    @Inject
    private PatientService patientService;

    @Inject
    private StaffService staffService;

    @Inject
    private AppointmentService appointmentService;

    @Inject
    private LoginBean loginBean;

    @PostConstruct
    public void init() {
        if (loginBean.getLoggedInUser() == null) return;

        switch (loginBean.getLoggedInUser().getRole()) {
            case ADMIN:
                totalPatients = patientService.getAllPatients().size();
                totalAppointments = appointmentService.getAllAppointments().size();
                totalStaff = staffService.getAllStaff().size();
                break;

            case DOCTOR:
                Staff doctor = loginBean.getLoggedInUser().getStaff();
                LocalDate today = LocalDate.now();
                Timestamp startOfDay = Timestamp.valueOf(LocalDateTime.of(today, LocalTime.MIN));
                Timestamp endOfDay = Timestamp.valueOf(LocalDateTime.of(today, LocalTime.MAX));

                todaysAppointments = appointmentService.getAppointmentsForDoctor(doctor.getId()).stream()
                        .filter(a -> a.getAppointmentDate() != null
                                && !a.getAppointmentDate().before(startOfDay)
                                && !a.getAppointmentDate().after(endOfDay)
                                && a.getStatus() == AppointmentStatus.SCHEDULED)
                        .collect(Collectors.toList());
                break;

            case PATIENT:
                Patient patient = loginBean.getLoggedInUser().getPatient();
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());

                upcomingAppointments = appointmentService.getAppointmentsByPatient(patient).stream()
                        .filter(a -> a.getAppointmentDate() != null
                                && a.getAppointmentDate().after(now)
                                && a.getStatus() == AppointmentStatus.SCHEDULED)
                        .collect(Collectors.toList());
                break;
        }
    }

    // Getters
    public long getTotalPatients() {
        return totalPatients;
    }

    public long getTotalAppointments() {
        return totalAppointments;
    }

    public long getTotalStaff() {
        return totalStaff;
    }

    public List<Appointment> getTodaysAppointments() {
        return todaysAppointments;
    }

    public List<Appointment> getUpcomingAppointments() {
        return upcomingAppointments;
    }
}
