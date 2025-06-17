package org.pahappa.com.model;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "admissions")
public class Admission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private Date admissionDate;

    @Column
    private Date dischargeDate;

    @Column(nullable = false)
    private String reason;

    public Admission() {}

    public Admission(Patient patient, Date admissionDate, String reason) {
        this.patient = patient;
        this.admissionDate = admissionDate;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Date getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(Date admissionDate) { this.admissionDate = admissionDate; }
    public Date getDischargeDate() { return dischargeDate; }
    public void setDischargeDate(Date dischargeDate) { this.dischargeDate = dischargeDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    @Override
    public String toString() {
        return "Admission{id=" + id + ", patient=" + patient.getFirstName() + ", admissionDate=" + admissionDate + "}";
    }
}