package org.pahappa.model;


import javax.persistence.*;
import java.sql.Date;
import java.util.Objects;

@Entity
@Table(name = "admissions")
public class Admission extends BaseModel {

    @ManyToOne
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "nurse_id")
    private Staff nurse; // The nurse assigned to this admission

    private Date admissionDate;
    private Date dischargeDate;
    private String reason;
    private String ward;
    private String wardNumber;

    public Admission() {}

    // Getters and Setters
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Staff getNurse() { return nurse; }
    public void setNurse(Staff nurse) { this.nurse = nurse; }
    public Date getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(Date admissionDate) { this.admissionDate = admissionDate; }
    public Date getDischargeDate() { return dischargeDate; }
    public void setDischargeDate(Date dischargeDate) { this.dischargeDate = dischargeDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
    public String getWardNumber() { return wardNumber; }
    public void setWardNumber(String wardNumber) { this.wardNumber = wardNumber; }

    @Override
    public String toString() {
        return "Admission{" +
                ", patient=" + (patient != null ? patient.getFullName() : "N/A") +
                ", nurse=" + (nurse != null ? nurse.getFullName() : "N/A") +
                ", ward='" + ward + '\'' +
                ", wardNumber='" + wardNumber + '\'' +
                ", admissionDate=" + Objects.toString(admissionDate, "N/A") +
                ", dischargeDate=" + Objects.toString(dischargeDate, "N/A") +
                ", reason='" + Objects.toString(reason, "N/A") + '\'' +
                '}';
    }
}