package org.pahappa.controller;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

import org.pahappa.service.PatientService;
import org.pahappa.service.StaffService;
import org.pahappa.service.AppointmentService;

@Named("dashboardBean")
@SessionScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private long totalPatients;
    private long totalDoctors;
    private long totalAppointments;

    @Inject
    private PatientService patientService;

    @Inject
    private StaffService doctorService;

    @Inject
    private AppointmentService appointmentService;

    @PostConstruct
    public void init() {
        totalPatients = patientService.countPatients();
        totalDoctors = StaffService.countDoctors();
        totalAppointments = appointmentService.countAppointments();
    }

    // Getters and setters
    public long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public long getTotalDoctors() {
        return totalDoctors;
    }

    public void setTotalDoctors(long totalDoctors) {
        this.totalDoctors = totalDoctors;
    }

    public long getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }
}
