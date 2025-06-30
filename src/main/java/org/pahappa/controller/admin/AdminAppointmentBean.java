package org.pahappa.controller.admin;

import org.pahappa.model.Appointment;
import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
import org.pahappa.service.AppointmentService;
import org.pahappa.service.PatientService;
import org.pahappa.service.StaffService;
import org.pahappa.utils.AppointmentStatus;
import org.pahappa.utils.Role;
import org.primefaces.PrimeFaces;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Named("adminAppointmentBean")
@ViewScoped
public class AdminAppointmentBean implements Serializable {

    @Inject
    private AppointmentService appointmentService;
    @Inject
    private PatientService patientService;
    @Inject
    private StaffService staffService;

    private List<Appointment> appointments;
    private Appointment selectedAppointment;
    private Long selectedPatientId;
    private Long selectedDoctorId;

    private List<Patient> availablePatients;
    private List<Staff> availableDoctors;


    @PostConstruct
    public void init() {
        appointments = appointmentService.getAllAppointments();
        availablePatients = patientService.getAllPatients();
        availableDoctors = staffService.getStaffByRole(Role.DOCTOR);
        prepareNewAppointment();
    }

    public void prepareNewAppointment() {
        selectedAppointment = new Appointment();
        selectedPatientId = null;
        selectedDoctorId = null;
    }

    public void saveAppointment() {
        try {
            // Find the full objects from the selected IDs
            Patient patient = patientService.getPatient(selectedPatientId);
            Staff doctor = staffService.getStaff(selectedDoctorId);

            if (patient == null || doctor == null) {
                throw new IllegalArgumentException("Invalid patient or doctor selected.");
            }

            selectedAppointment.setPatient(patient);
            selectedAppointment.setDoctor(doctor);

            // Logic to differentiate between creating a new appointment and updating an old one
            if (selectedAppointment.getId() == null) {
                appointmentService.scheduleAppointment(selectedAppointment);
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment scheduled successfully.");
            } else {
                appointmentService.updateAppointment(selectedAppointment);
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment updated successfully.");
            }

            // Refresh data and hide dialog
            init(); // Re-fetch all data
            PrimeFaces.current().executeScript("PF('appointmentDialog').hide()");
            PrimeFaces.current().ajax().update("appointmentForm:messages", "appointmentForm:appointmentTable");

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void selectAppointmentForEdit(Appointment appointment) {
        // We create a defensive copy to avoid modifying the list directly
        this.selectedAppointment = appointmentService.getAppointmentById(appointment.getId());
        if (this.selectedAppointment != null) {
            this.selectedPatientId = this.selectedAppointment.getPatient().getId();
            this.selectedDoctorId = this.selectedAppointment.getDoctor().getId();
        }
    }

    // This was missing a closing brace in the original file
    public List<Staff> completeDoctor(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyList();
        }
        String queryLowerCase = query.toLowerCase();
        return availableDoctors.stream()
                .filter(d -> d.getFullName().toLowerCase().contains(queryLowerCase))
                .collect(Collectors.toList());
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }


    // --- Getters & Setters for JSF page access ---
    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) { this.appointments = appointments; }
    public Appointment getSelectedAppointment() { return selectedAppointment; }
    public void setSelectedAppointment(Appointment selectedAppointment) { this.selectedAppointment = selectedAppointment; }
    public Long getSelectedPatientId() { return selectedPatientId; }
    public void setSelectedPatientId(Long selectedPatientId) { this.selectedPatientId = selectedPatientId; }
    public Long getSelectedDoctorId() { return selectedDoctorId; }
    public void setSelectedDoctorId(Long selectedDoctorId) { this.selectedDoctorId = selectedDoctorId; }

    // --- Helper methods for populating dropdowns ---
    public List<Patient> getAvailablePatients() { return availablePatients; }
    public List<Staff> getAvailableDoctors() { return availableDoctors; }
    public AppointmentStatus[] getAppointmentStatuses() { return AppointmentStatus.values(); }
}