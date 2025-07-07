package org.pahappa.controller.doctor;

import org.pahappa.model.Appointment;
import org.pahappa.controller.LoginBean;
import org.pahappa.service.appointment.AppointmentService;
import org.pahappa.utils.AppointmentStatus;
import org.pahappa.exception.HospitalServiceException;
import org.pahappa.exception.ValidationException;
import org.pahappa.exception.ResourceNotFoundException;

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
    private AppointmentService appointmentService; // [100]
    @Inject
    private LoginBean loginBean; // [100]

    private List<Appointment> appointments; // [100]
    private Appointment selectedAppointment; // [100]
    private AppointmentStatus newStatus; // [100]

    @PostConstruct
    public void init() {
        loadAppointments(); // [100]
    }

    private void loadAppointments() {
        if (loginBean.isLoggedIn() && loginBean.getLoggedInUser().getStaff() != null) { // [100]
            long doctorId = loginBean.getLoggedInUser().getStaff().getId(); // [101]
            appointments = appointmentService.getAppointmentsForDoctor(doctorId); // [101]
        }
    }

    public void updateStatus() {
        if (selectedAppointment != null && newStatus != null) { // [101]
            try {
                appointmentService.updateAppointmentStatus(selectedAppointment.getId(), newStatus); // [101]
                loadAppointments(); // [101]
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment status updated to " + newStatus); // [101]
            } catch (ValidationException | ResourceNotFoundException e) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not update status: " + e.getMessage()); // [102]
            } catch (HospitalServiceException hse) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Operation Failed", hse.getMessage());
            } catch (Exception e) {
                addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support."); // [102]
            }
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Invalid Selection", "Please select an appointment and a new status."); // [102]
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail)); // [102]
    }

    public List<Appointment> getAppointments() { return appointments; } // [103]
    public Appointment getSelectedAppointment() { return selectedAppointment; } // [103]
    public void setSelectedAppointment(Appointment appointment) { this.selectedAppointment = appointment; } // [103]
    public AppointmentStatus getNewStatus() { return newStatus; } // [103]
    public void setNewStatus(AppointmentStatus status) { this.newStatus = status; } // [103]

    public AppointmentStatus[] getAppointmentStatuses() {
        return AppointmentStatus.values(); // [103]
    }
}