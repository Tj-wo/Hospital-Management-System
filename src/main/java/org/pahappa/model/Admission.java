package org.pahappa.model;

import javax.persistence.*;
import java.sql.Date;
import java.util.Objects;

// Entity class representing a hospital admission in the database
@Entity
@Table(name = "admissions")
public class Admission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique identifier for the admission

    @ManyToOne
    private Patient patient; // Patient admitted

    private Date admissionDate; // Date of admission
    private Date dischargeDate; // Date of discharge (optional)
    private String reason; // Reason for admission

    // Default constructor required by Hibernate
    public Admission() {
    }

    // Constructor for creating a new admission
    public Admission(Patient patient, Date admissionDate, Date dischargeDate, String reason) {
        this.patient = patient;
        this.admissionDate = admissionDate;
        this.dischargeDate = dischargeDate;
        this.reason = reason;
    }

    // Getters and Setters
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

    public Date getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(Date admissionDate) {
        this.admissionDate = admissionDate;
    }

    public Date getDischargeDate() {
        return dischargeDate;
    }

    public void setDischargeDate(Date dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "Admission{" +
                "id=" + id +
                ", patient=" + (patient != null ? patient.getFullName() : "N/A") +
                ", admissionDate=" + Objects.toString(admissionDate, "N/A") +
                ", dischargeDate=" + Objects.toString(dischargeDate, "N/A") +
                ", reason='" + Objects.toString(reason, "N/A") + '\'' +
                '}';
    }
}