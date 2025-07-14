package org.pahappa.controller.receptionist;

import org.pahappa.controller.LoginBean;
import org.pahappa.model.Appointment;
import org.pahappa.model.Patient;
import org.pahappa.model.Role;
import org.pahappa.model.Staff;
import org.pahappa.service.appointment.AppointmentService;
import org.pahappa.service.patient.PatientService;
import org.pahappa.service.staff.StaffService;
import org.pahappa.service.role.RoleService;
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
import java.util.Collections;
import java.util.Date;
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
    @Inject
    private RoleService roleService;
    @Inject
    private LoginBean loginBean;

    private List allAppointments;
    private List allPatients;
    private List allDoctors;
    private Appointment newAppointment = new Appointment();
    private Appointment selectedAppointment;

    @PostConstruct
    public void init() {
        loadAppointments();
        allPatients = patientService.getAllPatients();
        try {
            // Fetch the Role object for "DOCTOR"
            Role doctorRole = roleService.getRoleByName("DOCTOR");
            if (doctorRole != null) {
                allDoctors = staffService.getStaffByRole(doctorRole);
            } else {
                addMessage(FacesMessage.SEVERITY_WARN, "Configuration Warning", "The 'DOCTOR' role was not found in the system. No doctors will be available.");
                allDoctors = Collections.emptyList();
            }
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "Error initializing doctors: " + e.getMessage());
            allDoctors = Collections.emptyList();
        }
    }

    private void loadAppointments() {
        allAppointments = appointmentService.getAllAppointments();
    }

    public void scheduleAppointment() {
        try {
            String currentUserId = loginBean.getLoggedInUser().getId().toString();
            String currentUsername = loginBean.getLoggedInUser().getUsername();
            appointmentService.scheduleAppointment(selectedAppointment, currentUserId, currentUsername);
            loadAppointments();
            newAppointment = new Appointment();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment scheduled.");
        } catch (ValidationException ve) {
            addMessage(FacesMessage.SEVERITY_WARN, "Scheduling Failed", ve.getMessage());
        } catch (HospitalServiceException hse) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Scheduling Failed", hse.getMessage());
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support.");
        }
    }

    public void updateAppointment() {
        if (selectedAppointment != null) {
            try {
                appointmentService.updateAppointment(selectedAppointment);
                loadAppointments();
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment rescheduled successfully.");
            } catch (ValidationException | ResourceNotFoundException e) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Update Failed", e.getMessage());
            } catch (HospitalServiceException hse) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Update Failed", hse.getMessage());
            } catch (Exception e) {
                addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support.");
            }
        }
    }

    public void cancelAppointment() {
        if (selectedAppointment != null) {
            try {
                appointmentService.cancelAppointment(selectedAppointment.getId(), false);
                loadAppointments();
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment has been cancelled.");
            } catch (ValidationException | ResourceNotFoundException e) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Cancellation Failed", e.getMessage());
            } catch (HospitalServiceException hse) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Cancellation Failed", hse.getMessage());
            } catch (Exception e) {
                addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support.");
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

    public Date getToday() {
        return new Date();
    }

    public List<Appointment> getAllAppointments() { return allAppointments; }
    public List<Patient> getAllPatients() { return allPatients; }
    public List<Staff> getAllDoctors() { return allDoctors; }
    public Appointment getNewAppointment() { return newAppointment; }
    public void setNewAppointment(Appointment newAppointment) { this.newAppointment = newAppointment; }
    public Appointment getSelectedAppointment() { return selectedAppointment; }
    public void setSelectedAppointment(Appointment selectedAppointment) { this.selectedAppointment = selectedAppointment; }
}