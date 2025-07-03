package org.pahappa.controller.receptionist;

import org.pahappa.model.Appointment;
import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
import org.pahappa.service.appointment.AppointmentService;
import org.pahappa.service.patient.PatientService;
import org.pahappa.service.staff.StaffService;
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
    private Appointment selectedAppointment;

    @PostConstruct
    public void init() {
        loadAppointments();
        allPatients = patientService.getAllPatients();
        allDoctors = staffService.getStaffByRole(Role.DOCTOR);
    }

    private void loadAppointments() {
        allAppointments = appointmentService.getAllAppointments();
    }

    public void scheduleAppointment() {
        try {
            appointmentService.scheduleAppointment(newAppointment);
            loadAppointments();
            newAppointment = new Appointment();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment scheduled.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Scheduling Failed", e.getMessage());
        }
    }

    public void updateAppointment() {
        if (selectedAppointment != null) {
            try {
                appointmentService.updateAppointment(selectedAppointment);
                loadAppointments();
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment rescheduled successfully.");
            } catch (Exception e) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Update Failed", e.getMessage());
            }
        }
    }

    public void cancelAppointment() {
        if (selectedAppointment != null) {
            try {
                appointmentService.cancelAppointment(selectedAppointment.getId(), false);
                loadAppointments();
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment has been cancelled.");
            } catch (Exception e) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Cancellation Failed", e.getMessage());
            }
        }
    }

    public void selectAppointment(Appointment appointment) {
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

    public List<Appointment> getAllAppointments() { return allAppointments; }
    public List<Patient> getAllPatients() { return allPatients; }
    public List<Staff> getAllDoctors() { return allDoctors; }
    public Appointment getNewAppointment() { return newAppointment; }
    public void setNewAppointment(Appointment newAppointment) { this.newAppointment = newAppointment; }
    public Appointment getSelectedAppointment() { return selectedAppointment; }
    public void setSelectedAppointment(Appointment selectedAppointment) { this.selectedAppointment = selectedAppointment; }
}
