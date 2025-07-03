package org.pahappa.model;


import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "medical_records")
public class MedicalRecord extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Staff doctor;

    @Temporal(TemporalType.DATE) //
    @Column(name = "record_date", nullable = false)
    private Date recordDate;

    @Column(nullable = false)
    private String diagnosis;

    @Column(nullable = false)
    private String treatment;

    public MedicalRecord() {
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

    public Date getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    @Override
    public String toString() {
        return "MedicalRecord{" +
                ", patient=" + patient.getFullName() +
                ", doctor=" + doctor.getFullName() +
                ", recordDate=" + recordDate +
                ", diagnosis='" + diagnosis + '\'' +
                ", treatment='" + treatment + '\'' +
                '}';
    }
}
