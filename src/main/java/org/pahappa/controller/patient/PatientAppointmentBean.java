package org.pahappa.controller.patient;

import org.pahappa.model.Appointment;
import org.pahappa.model.Staff;
import org.pahappa.controller.LoginBean;
import org.pahappa.service.AppointmentService;
import org.pahappa.service.StaffService;
import org.pahappa.utils.Role;
import org.primefaces.PrimeFaces;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("patientAppointmentBean")
@ViewScoped
public class PatientAppointmentBean implements Serializable {

    @Inject
    private AppointmentService appointmentService;

    @Inject
    private StaffService staffService;

    @Inject
    private LoginBean loginBean;

    private List<Appointment> appointments;
    private List<Staff> doctors;
    private Appointment newAppointment = new Appointment();
    private Appointment selectedAppointment;

    @PostConstruct
    public void init() {
        if (isPatientLoggedIn()) {
            loadAppointments();
            this.doctors = staffService.getStaffByRole(Role.DOCTOR);
        }
    }

    private void loadAppointments() {
        long patientId = loginBean.getLoggedInUser().getPatient().getId();
        appointments = appointmentService.getAppointmentsForPatient(patientId);
    }

    public void bookAppointment() {
        boolean success = false;
        try {
            newAppointment.setPatient(loginBean.getLoggedInUser().getPatient());
            appointmentService.scheduleAppointment(newAppointment);

            loadAppointments();
            newAppointment = new Appointment(); // Reset the form

            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Your appointment has been booked.");
            success = true;

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Booking Failed", e.getMessage());
        }

        // Use a callback parameter to conditionally close the dialog from the view.
        // This ensures the dialog stays open if there's a validation error.
        PrimeFaces.current().ajax().addCallbackParam("bookingSuccess", success);
    }

    public void cancelAppointment() {
        if (selectedAppointment != null) {
            try {
                appointmentService.cancelAppointment(selectedAppointment.getId(), true);
                loadAppointments();
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Your appointment has been cancelled.");
            } catch (Exception e) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Cancellation Failed", e.getMessage());
            }
        }
    }

    private boolean isPatientLoggedIn() {
        return loginBean != null && loginBean.isLoggedIn() &&
                loginBean.getLoggedInUser().getPatient() != null &&
                loginBean.getLoggedInUser().getRole() == Role.PATIENT;
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // --- Getters and Setters ---
    public List<Appointment> getAppointments() { return appointments; }
    public Appointment getNewAppointment() { return newAppointment; }
    public void setNewAppointment(Appointment newAppointment) { this.newAppointment = newAppointment; }
    public List<Staff> getDoctors() { return doctors; }
    public Appointment getSelectedAppointment() { return selectedAppointment; }
    public void setSelectedAppointment(Appointment selectedAppointment) { this.selectedAppointment = selectedAppointment; }
}