package org.pahappa.model;

import org.pahappa.utils.AppointmentStatus;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "appointments")
public class Appointment extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Staff doctor;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "appointmentDate", nullable = false)
    private Date appointmentDate;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private boolean deleted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;


    public Appointment() {
        this.status = AppointmentStatus.SCHEDULED;
    }

    // Getters and setters
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

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "patient=" + (patient != null ? patient.getFullName() : "N/A") +
                ", doctor=" + (doctor != null ? doctor.getFullName() : "N/A") +
                ", appointmentDate=" + appointmentDate +
                ", status=" + status +
                ", reason='" + reason + '\'' +
                '}';
    }
}
