package org.pahappa.controller.patient;

import org.pahappa.model.Appointment;
import org.pahappa.service.AppointmentService;
import org.primefaces.PrimeFaces;

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

    private List<Appointment> appointments;
    private Appointment newAppointment;
    private Appointment selectedAppointment;

    @Inject
    private AppointmentService appointmentService;

    @Inject
    private org.pahappa.controller.auth.LoginBean loginBean;

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
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Failed", e.getMessage());
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
            appointmentService.cancelAppointment(selectedAppointment);
            addMessage(FacesMessage.SEVERITY_INFO, "Cancelled", "Appointment cancelled.");
            loadAppointments();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    private void addMessage(FacesMessage.Severity severity, String title, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, title, detail));
    }

    // Getters and setters
    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
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
