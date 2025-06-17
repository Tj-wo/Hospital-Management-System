package org.pahappa.com.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "medical_records")
public class MedicalRecord {
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
    private String diagnosis;

    @Column
    private String prescription;

    @Column(nullable = false)
    private LocalDate recordDate;

    public MedicalRecord() {}

    public MedicalRecord(Patient patient, Staff doctor, String diagnosis, String prescription, LocalDate recordDate) {
        this.patient = patient;
        this.doctor = doctor;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
        this.recordDate = recordDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Staff getDoctor() { return doctor; }
    public void setDoctor(Staff doctor) { this.doctor = doctor; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getPrescription() { return prescription; }
    public void setPrescription(String prescription) { this.prescription = prescription; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    @Override
    public String toString() {
        return "MedicalRecord{id=" + id + ", patient=" + patient.getFirstName() + ", diagnosis=" + diagnosis + "}";
    }
}