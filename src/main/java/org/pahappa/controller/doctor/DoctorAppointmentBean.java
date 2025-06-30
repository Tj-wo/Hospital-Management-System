package org.pahappa.controller.doctor;

import org.pahappa.model.Appointment;
import org.pahappa.controller.LoginBean;
import org.pahappa.service.AppointmentService;
import org.pahappa.utils.AppointmentStatus;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("doctorAppointmentBean")
@ViewScoped
public class DoctorAppointmentBean implements Serializable {

    @Inject
    private AppointmentService appointmentService;

    @Inject
    private LoginBean loginBean;

    private List<Appointment> appointments;
    private Appointment selectedAppointment;
    private AppointmentStatus newStatus;

    @PostConstruct
    public void init() {
        loadAppointments();
    }

    private void loadAppointments() {
        if (loginBean.isLoggedIn() && loginBean.getLoggedInUser().getStaff() != null) {
            long doctorId = loginBean.getLoggedInUser().getStaff().getId();
            appointments = appointmentService.getAppointmentsForDoctor(doctorId);
        }
    }

    /**
     * Action method to update the status of the selected appointment.
     */
    public void updateStatus() {
        if (selectedAppointment != null && newStatus != null) {
            try {
                appointmentService.updateAppointmentStatus(selectedAppointment.getId(), newStatus);
                loadAppointments(); // Refresh the list to show the new status
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment status updated to " + newStatus);
            } catch (Exception e) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not update status: " + e.getMessage());
            }
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Invalid Selection", "Please select an appointment and a new status.");
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters and Setters
    public List<Appointment> getAppointments() { return appointments; }
    public Appointment getSelectedAppointment() { return selectedAppointment; }
    public void setSelectedAppointment(Appointment appointment) { this.selectedAppointment = appointment; }
    public AppointmentStatus getNewStatus() { return newStatus; }
    public void setNewStatus(AppointmentStatus status) { this.newStatus = status; }

    // Helper to provide all possible statuses to the dropdown in the view
    public AppointmentStatus[] getAppointmentStatuses() {
        return AppointmentStatus.values();
    }
}