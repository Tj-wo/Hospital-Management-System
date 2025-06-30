package org.pahappa.controller.receptionist;

import org.pahappa.model.Appointment;
import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
import org.pahappa.service.AppointmentService;
import org.pahappa.service.PatientService;
import org.pahappa.service.StaffService;
import org.pahappa.utils.Role;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("receptionistAppointmentBean")
@ViewScoped
public class ReceptionistAppointmentBean implements Serializable {

    @Inject
    private AppointmentService appointmentService;
    @Inject
    private PatientService patientService;
    @Inject
    private StaffService staffService;

    private List<Appointment> allAppointments;
    private List<Patient> allPatients;
    private List<Staff> allDoctors;

    private Appointment newAppointment = new Appointment();
    private Appointment selectedAppointment; // For editing or cancelling

    @PostConstruct
    public void init() {
        loadAppointments();
        allPatients = patientService.getAllPatients();
        allDoctors = staffService.getStaffByRole(Role.DOCTOR);
    }

    private void loadAppointments() {
        allAppointments = appointmentService.getAllAppointments();
    }

    /**
     * Action method to schedule a new appointment.
     */
    public void scheduleAppointment() {
        try {
            appointmentService.scheduleAppointment(newAppointment);
            loadAppointments(); // Refresh data table
            newAppointment = new Appointment(); // Reset form
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment scheduled.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Scheduling Failed", e.getMessage());
        }
    }

    /**
     * Action method to save changes to an existing appointment (reschedule).
     */
    public void updateAppointment() {
        if (selectedAppointment != null) {
            try {
                appointmentService.updateAppointment(selectedAppointment);
                loadAppointments(); // Refresh data table
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment rescheduled successfully.");
            } catch (Exception e) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Update Failed", e.getMessage());
            }
        }
    }

    /**
     * Action method to cancel an appointment.
     */
    public void cancelAppointment() {
        if (selectedAppointment != null) {
            try {
                appointmentService.cancelAppointment(selectedAppointment.getId(), false); // false = not by patient
                loadAppointments(); // Refresh data table
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment has been cancelled.");
            } catch (Exception e) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Cancellation Failed", e.getMessage());
            }
        }
    }

    /**
     * Helper method to set the selected appointment before opening a dialog.
     * This is useful for both editing and cancelling.
     */
    public void selectAppointment(Appointment appointment) {
        // We create a copy for editing so that changes are not reflected in the table until saved.
        this.selectedAppointment = new Appointment();
        this.selectedAppointment.setId(appointment.getId());
        this.selectedAppointment.setPatient(appointment.getPatient());
        this.selectedAppointment.setDoctor(appointment.getDoctor());
        this.selectedAppointment.setAppointmentDate(appointment.getAppointmentDate());
        this.selectedAppointment.setReason(appointment.getReason());
        this.selectedAppointment.setStatus(appointment.getStatus());
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // --- Getters and Setters ---
    public List<Appointment> getAllAppointments() { return allAppointments; }
    public List<Patient> getAllPatients() { return allPatients; }
    public List<Staff> getAllDoctors() { return allDoctors; }
    public Appointment getNewAppointment() { return newAppointment; }
    public void setNewAppointment(Appointment newAppointment) { this.newAppointment = newAppointment; }
    public Appointment getSelectedAppointment() { return selectedAppointment; }
    public void setSelectedAppointment(Appointment selectedAppointment) { this.selectedAppointment = selectedAppointment; }
}