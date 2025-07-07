package org.pahappa.controller.patient;

import org.pahappa.model.Appointment;
import org.pahappa.service.appointment.AppointmentService;
import org.primefaces.PrimeFaces;
import org.pahappa.exception.HospitalServiceException; // Added import
import org.pahappa.exception.ValidationException; // Added import
import org.pahappa.exception.AppointmentConflictException; // Added import
import org.pahappa.exception.ResourceNotFoundException; // Added import

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named("patientAppointmentBean")
@RequestScoped
public class PatientAppointmentBean implements Serializable {

    private List appointments;
    private Appointment newAppointment;
    private Appointment selectedAppointment;

    @Inject
    private AppointmentService appointmentService;
    @Inject
    private org.pahappa.controller.LoginBean loginBean;

    @PostConstruct
    public void init() {
        newAppointment = new Appointment();
        loadAppointments();
    }

    public void loadAppointments() {
        appointments = appointmentService.getAppointmentsByPatient(
                loginBean.getLoggedInUser().getPatient()
        );
    }

    public void prepareNewAppointment() {
        newAppointment = new Appointment();
    }

    public void saveAppointment() {
        boolean success = false;
        try {
            newAppointment.setPatient(loginBean.getLoggedInUser().getPatient());
            if (newAppointment.getId() == null) {
                appointmentService.scheduleAppointment(newAppointment);
                addMessage(FacesMessage.SEVERITY_INFO, "Booked", "Appointment scheduled.");
            } else {
                appointmentService.updateAppointment(newAppointment);
                addMessage(FacesMessage.SEVERITY_INFO, "Updated", "Appointment rescheduled.");
            }
            loadAppointments();
            newAppointment = new Appointment();
            success = true;
        } catch (ValidationException ve) { // Specific catch for validation errors
            addMessage(FacesMessage.SEVERITY_WARN, "Booking Failed", ve.getMessage());
        } catch (ResourceNotFoundException rnfe) { // Specific catch for resource not found errors
            addMessage(FacesMessage.SEVERITY_ERROR, "Booking Failed", rnfe.getMessage());
        } catch (HospitalServiceException hse) { // Specific catch for service-layer errors
            addMessage(FacesMessage.SEVERITY_ERROR, "Booking Failed", hse.getMessage());
        } catch (Exception e) { // Generic fallback for unexpected errors
            addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support.");
        }
        PrimeFaces.current().ajax().addCallbackParam("bookingSuccess", success);
    }

    public void selectForEdit(Appointment appt) {
        newAppointment = new Appointment();
        newAppointment.setId(appt.getId());
        newAppointment.setPatient(appt.getPatient());
        newAppointment.setDoctor(appt.getDoctor());
        newAppointment.setAppointmentDate(appt.getAppointmentDate());
        newAppointment.setReason(appt.getReason());
    }

    public void cancelAppointment() {
        try {
            if (selectedAppointment != null && selectedAppointment.getId() != null) {
                appointmentService.cancelAppointment(selectedAppointment.getId(), true);
                addMessage(FacesMessage.SEVERITY_INFO, "Cancelled", "Appointment cancelled.");
                loadAppointments();
            } else {
                addMessage(FacesMessage.SEVERITY_WARN, "Warning", "No appointment selected to cancel.");
            }
        } catch (ValidationException | ResourceNotFoundException e) { // Specific catch for validation/not found errors
            addMessage(FacesMessage.SEVERITY_ERROR, "Cancellation Failed", e.getMessage());
        } catch (HospitalServiceException hse) { // Specific catch for service-layer errors
            addMessage(FacesMessage.SEVERITY_ERROR, "Cancellation Failed", hse.getMessage());
        } catch (Exception e) { // Generic fallback for unexpected errors
            addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support.");
        }
    }

    private void addMessage(FacesMessage.Severity severity, String title, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, title, detail));
    }

    public List getAppointments() {
        return appointments;
    }

    public void setAppointments(List appointments) {
        this.appointments = appointments;
    }

    public Appointment getNewAppointment() {
        return newAppointment;
    }

    public void setNewAppointment(Appointment newAppointment) {
        this.newAppointment = newAppointment;
    }

    public Appointment getSelectedAppointment() {
        return selectedAppointment;
    }

    public void setSelectedAppointment(Appointment selectedAppointment) {
        this.selectedAppointment = selectedAppointment;
    }
}