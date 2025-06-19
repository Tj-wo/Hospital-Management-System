package org.pahappa.model;

import javax.persistence.*;
import java.sql.Timestamp;

// Entity class representing an appointment
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Staff doctor;

    @Column(nullable = false)
    private Timestamp appointmentDate;

    @Column(nullable = false)
    private String reason;

    // No-arg constructor for Hibernate
    public Appointment() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Staff getDoctor() {
        return doctor;
    }

    public void setDoctor(Staff doctor) {
        this.doctor = doctor;
    }

    public Timestamp getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Timestamp appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", patient=" + (patient != null ? patient.getFullName() : "N/A") +
                ", doctor=" + (doctor != null ? doctor.getFullName() : "N/A") +
                ", appointmentDate=" + appointmentDate +
                ", reason='" + reason + '\'' +
                '}';
    }
}