package org.pahappa.controller;

import org.pahappa.model.Admission;
import org.pahappa.model.Appointment;
import org.pahappa.model.MedicalRecord;
import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
import org.pahappa.service.admission.AdmissionService;
import org.pahappa.service.appointment.AppointmentService;
import org.pahappa.service.medicalRecord.MedicalRecordService;
import org.pahappa.service.patient.PatientService;
import org.pahappa.service.staff.StaffService;
import org.pahappa.utils.AppointmentStatus;
import org.pahappa.utils.Role;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named("dashboardBean")
@SessionScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // Admin Stats (Existing & NEW)
    private long totalPatients;
    private long totalAppointments;
    private long totalStaff;
    private long totalDoctors;
    private long totalNurses;
    private long totalReceptionists;
    private long activeAdmissionsCount;
    private long dischargedAdmissionsCount;
    private Map<String, Long> appointmentStatusCounts; // Changed to Map<String, Long>
    private long totalMedicalRecords;

    // Doctor Stats (Existing & NEW)
    private List<Appointment> todaysAppointments;
    private long totalPatientsSeenByDoctor;
    private Map<String, Long> doctorAppointmentStatusCounts; // Changed to Map<String, Long>

    // Patient Stats (Existing & NEW)
    private List<Appointment> upcomingAppointments;
    private long totalPatientAdmissions;
    private long totalPatientMedicalRecords;

    // Nurse Stats (Existing & NEW)
    private long myActivePatientCount;
    private long totalAdmissionsManagedByNurse;

    // Receptionist Stats (Existing & NEW)
    private long allTodaysAppointmentsCount;
    private long newPatientsToday;
    private long newPatientsThisWeek;


    @Inject private PatientService patientService;
    @Inject private StaffService staffService;
    @Inject private AppointmentService appointmentService;
    @Inject private AdmissionService admissionService;
    @Inject private MedicalRecordService medicalRecordService;
    @Inject private LoginBean loginBean;

    @PostConstruct
    public void init() {
        if (loginBean.getLoggedInUser() == null) {
            resetFields();
            return;
        }
        resetFields();
        switch (loginBean.getLoggedInUser().getRole()) {
            case ADMIN:         loadAdminData();        break;
            case DOCTOR:        loadDoctorData();       break;
            case PATIENT:       loadPatientData();      break;
            case NURSE:         loadNurseData();        break;
            case RECEPTIONIST:  loadReceptionistData(); break;
        }
    }

    private void loadAdminData() {
        totalPatients = patientService.countPatients();
        totalAppointments = appointmentService.countAppointments();
        totalStaff = staffService.getAllStaff().size();

        // NEW Admin Stats Calculations
        totalDoctors = staffService.countDoctors();
        totalNurses = staffService.countNurses();
        totalReceptionists = staffService.countReceptionists();

        List<Admission> allAdmissions = admissionService.getAllAdmissions(); // Get all admissions
        activeAdmissionsCount = allAdmissions.stream()
                .filter(a -> a.getDischargeDate() == null) // Filter for active (dischargeDate is null)
                .count();
        dischargedAdmissionsCount = allAdmissions.stream()
                .filter(a -> a.getDischargeDate() != null) // Filter for discharged (dischargeDate is not null)
                .count();


        appointmentStatusCounts = appointmentService.getAllAppointments().stream() // Get all appointments
                .collect(Collectors.groupingBy(appt -> appt.getStatus().name(), Collectors.counting())); // Group by status string name

        totalMedicalRecords = medicalRecordService.getAllMedicalRecords().size(); // Count all medical records
    }

    private void loadDoctorData() {
        Staff doctor = loginBean.getLoggedInUser().getStaff();
        if (doctor == null) return;

        todaysAppointments = filterForToday(appointmentService.getAppointmentsForDoctor(doctor.getId()));

        List<Appointment> doctorAppointments = appointmentService.getAppointmentsForDoctor(doctor.getId()); // Get doctor's appointments
        // --- CORRECTED FIX FOR DOCTOR APPOINTMENT STATUS COUNTS ---
        doctorAppointmentStatusCounts = doctorAppointments.stream()
                .collect(Collectors.groupingBy(appt -> appt.getStatus().name(), Collectors.counting())); // Group by status string name
        // --- END CORRECTED FIX ---
    }

    private void loadPatientData() {
        Patient patient = loginBean.getLoggedInUser().getPatient();
        if (patient == null) return;

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        upcomingAppointments = appointmentService.getAppointmentsByPatient(patient).stream()
                .filter(a -> a.getAppointmentDate() != null && a.getAppointmentDate().after(now) && a.getStatus() == AppointmentStatus.SCHEDULED)
                .collect(Collectors.toList());

        // NEW Patient Stats Calculations
        totalPatientAdmissions = admissionService.getAdmissionsForPatient(patient.getId()).size(); // Count patient's admissions
        totalPatientMedicalRecords = medicalRecordService.getRecordsForPatient(patient.getId()).size(); // Count patient's medical records
    }

    private void loadNurseData() {
        Staff nurse = loginBean.getLoggedInUser().getStaff();
        if (nurse == null) return;

        myActivePatientCount = admissionService.getAdmissionsForNurse(nurse.getId()).stream()
                .filter(a -> a.getDischargeDate() == null).count();

        // NEW Nurse Stats Calculations
        totalAdmissionsManagedByNurse = admissionService.getAdmissionsForNurse(nurse.getId()).size(); // Total admissions ever assigned
    }

    private void loadReceptionistData() {
        allTodaysAppointmentsCount = filterForToday(appointmentService.getAllAppointments()).size();

        // NEW Receptionist Stats Calculations
        List<Patient> allPatients = patientService.getAllPatients(); // Get all patients
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1); // Monday
        Timestamp startOfToday = Timestamp.valueOf(LocalDateTime.of(today, LocalTime.MIN));
        Timestamp endOfToday = Timestamp.valueOf(LocalDateTime.of(today, LocalTime.MAX));
        Timestamp startOfThisWeek = Timestamp.valueOf(LocalDateTime.of(startOfWeek, LocalTime.MIN));

        newPatientsToday = allPatients.stream()
                .filter(p -> p.getDateCreated() != null && !p.getDateCreated().before(startOfToday) && !p.getDateCreated().after(endOfToday)) // Filter by dateCreated
                .count();

        newPatientsThisWeek = allPatients.stream()
                .filter(p -> p.getDateCreated() != null && !p.getDateCreated().before(startOfThisWeek) && !p.getDateCreated().after(endOfToday))
                .count();
    }

    private List<Appointment> filterForToday(List<Appointment> appointments) {
        if(appointments == null) return Collections.emptyList();
        LocalDate today = LocalDate.now();
        Timestamp startOfDay = Timestamp.valueOf(LocalDateTime.of(today, LocalTime.MIN));
        Timestamp endOfDay = Timestamp.valueOf(LocalDateTime.of(today, LocalTime.MAX));
        return appointments.stream()
                .filter(a -> a.getAppointmentDate() != null && !a.getAppointmentDate().before(startOfDay) && !a.getAppointmentDate().after(endOfDay))
                .collect(Collectors.toList());
    }

    private void resetFields() {
        totalPatients = 0;
        totalAppointments = 0;
        totalStaff = 0;
        myActivePatientCount = 0;
        allTodaysAppointmentsCount = 0;
        todaysAppointments = Collections.emptyList();
        upcomingAppointments = Collections.emptyList();

        // NEW: Reset new fields
        totalDoctors = 0;
        totalNurses = 0;
        totalReceptionists = 0;
        activeAdmissionsCount = 0;
        dischargedAdmissionsCount = 0;
        appointmentStatusCounts = Collections.emptyMap();
        totalMedicalRecords = 0;
        totalPatientsSeenByDoctor = 0;
        doctorAppointmentStatusCounts = Collections.emptyMap();
        totalPatientAdmissions = 0;
        totalPatientMedicalRecords = 0;
        newPatientsToday = 0;
        newPatientsThisWeek = 0;
    }

    // --- Getters for all fields (Existing & NEW) ---
    public long getTotalPatients() { return totalPatients; }
    public long getTotalAppointments() { return totalAppointments; }
    public long getTotalStaff() { return totalStaff; }
    public List<Appointment> getTodaysAppointments() { return todaysAppointments; }
    public List<Appointment> getUpcomingAppointments() { return upcomingAppointments; }
    public long getMyActivePatientCount() { return myActivePatientCount; }
    public long getAllTodaysAppointmentsCount() { return allTodaysAppointmentsCount; }

    // NEW Getters
    public long getTotalDoctors() { return totalDoctors; }
    public long getTotalNurses() { return totalNurses; }
    public long getTotalReceptionists() { return totalReceptionists; }
    public long getActiveAdmissionsCount() { return activeAdmissionsCount; }
    public long getDischargedAdmissionsCount() { return dischargedAdmissionsCount; }
    public Map<String, Long> getAppointmentStatusCounts() { return appointmentStatusCounts; } // Return Map<String, Long>
    public long getTotalMedicalRecords() { return totalMedicalRecords; }
    public long getTotalPatientsSeenByDoctor() { return totalPatientsSeenByDoctor; }
    public Map<String, Long> getDoctorAppointmentStatusCounts() { return doctorAppointmentStatusCounts; } // Return Map<String, Long>
    public long getTotalPatientAdmissions() { return totalPatientAdmissions; }
    public long getTotalPatientMedicalRecords() { return totalPatientMedicalRecords; }
    public long getNewPatientsToday() { return newPatientsToday; }
    public long getNewPatientsThisWeek() { return newPatientsThisWeek; }

    // No setters for these stats as they are calculated within the bean.
}