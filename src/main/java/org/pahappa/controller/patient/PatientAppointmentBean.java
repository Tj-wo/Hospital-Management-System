package org.pahappa.controller.patient;

import org.pahappa.controller.LoginBean;
import org.pahappa.model.Appointment;
import org.pahappa.model.Role;
import org.pahappa.model.Staff;
import org.pahappa.service.role.RoleService;
import org.pahappa.service.staff.StaffService;
import org.pahappa.service.appointment.AppointmentService;
import org.primefaces.PrimeFaces;
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

@Named("patientAppointmentBean")
@ViewScoped
public class PatientAppointmentBean implements Serializable {

    private List<Appointment> appointments;
    private Appointment newAppointment;
    private Appointment selectedAppointment;
    private List<Staff> availableDoctors;


    @Inject
    private AppointmentService appointmentService;
    @Inject
    private LoginBean loginBean;
    @Inject
    private StaffService staffService;
    @Inject
    private RoleService roleService;


    @PostConstruct
    public void init() {
        newAppointment = new Appointment();
        loadAppointments();
        loadAvailableDoctors();
    }

    public void loadAppointments() {
        if (loginBean.isLoggedIn() && loginBean.getLoggedInUser().getPatient() != null) {
            appointments = appointmentService.getAppointmentsByPatient(
                    loginBean.getLoggedInUser().getPatient()
            );
        }
    }

    private void loadAvailableDoctors() {
        Role doctorRole = roleService.getRoleByName("DOCTOR");
        if (doctorRole != null) {
            this.availableDoctors = staffService.getStaffByRole(doctorRole);
        } else {
            this.availableDoctors = Collections.emptyList();
        }
    }

    public void prepareNewAppointment() {
        newAppointment = new Appointment();
    }

    public void saveAppointment() {
        boolean success = false;
        try {
            newAppointment.setPatient(loginBean.getLoggedInUser().getPatient());

            // Get current user details for auditing
            String currentUserId = loginBean.getLoggedInUser().getId().toString();
            String currentUsername = loginBean.getLoggedInUser().getUsername();

            if (newAppointment.getId() == null) {
                appointmentService.scheduleAppointment(newAppointment, currentUserId, currentUsername);
                addMessage(FacesMessage.SEVERITY_INFO, "Booked", "Appointment scheduled.");
            } else {
                appointmentService.updateAppointment(newAppointment);
                addMessage(FacesMessage.SEVERITY_INFO, "Updated", "Appointment rescheduled.");
            }
            loadAppointments();
            newAppointment = new Appointment();
            success = true;
        } catch (ValidationException | ResourceNotFoundException e) {
            addMessage(FacesMessage.SEVERITY_WARN, "Booking Failed", e.getMessage());
        } catch (HospitalServiceException hse) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Booking Failed", hse.getMessage());
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred.");
        }
        PrimeFaces.current().ajax().addCallbackParam("bookingSuccess", success);
    }

    public void selectForEdit(Appointment appt) {
        // This is a direct assignment, which is simpler and less error-prone
        // than creating a new object and copying properties.
        this.newAppointment = appt;
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
        } catch (ValidationException | ResourceNotFoundException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Cancellation Failed", e.getMessage());
        } catch (HospitalServiceException hse) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Cancellation Failed", hse.getMessage());
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support.");
        }
    }

    public Date getToday() {
        return new Date();
    }

    private void addMessage(FacesMessage.Severity severity, String title, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, title, detail));
    }

    // --- Getters and Setters ---

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

    public List<Staff> getAvailableDoctors() {
        return availableDoctors;
    }
}