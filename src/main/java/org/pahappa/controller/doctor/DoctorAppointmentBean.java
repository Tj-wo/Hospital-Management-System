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

    public void updateStatus() {
        if (selectedAppointment != null && newStatus != null) { 
            try {
                appointmentService.updateAppointmentStatus(selectedAppointment.getId(), newStatus); 
                loadAppointments(); 
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment status updated to " + newStatus); 
            } catch (ValidationException | ResourceNotFoundException e) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not update status: " + e.getMessage()); 
            } catch (HospitalServiceException hse) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Operation Failed", hse.getMessage());
            } catch (Exception e) {
                addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support."); 
            }
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Invalid Selection", "Please select an appointment and a new status."); 
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail)); 
    }

    public List<Appointment> getAppointments() { return appointments; } 
    public Appointment getSelectedAppointment() { return selectedAppointment; } 
    public void setSelectedAppointment(Appointment appointment) { this.selectedAppointment = appointment; } 
    public AppointmentStatus getNewStatus() { return newStatus; } 
    public void setNewStatus(AppointmentStatus status) { this.newStatus = status; } 

    public AppointmentStatus[] getAppointmentStatuses() {
        return AppointmentStatus.values(); 
    }
}