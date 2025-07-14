package org.pahappa.model;


import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "users")
public class User extends BaseModel {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private org.pahappa.model.Role role;

    @OneToOne
    @JoinColumn(name = "patient_id", unique = true)
    private Patient patient;

    @OneToOne
    @JoinColumn(name = "staff_id", unique = true)
    private Staff staff;


    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "date_deactivated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateDeactivated;

    public User() {}

    // Getters and Setters for User-specific fields
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public org.pahappa.model.Role getRole() { return role; }
    public void setRole(org.pahappa.model.Role role) { this.role = role; }
    public Staff getStaff() { return staff; }
    public void setStaff(Staff staff) { this.staff = staff; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getDateDeactivated() {
        return dateDeactivated;
    }

    public void setDateDeactivated(Date dateDeactivated) {
        this.dateDeactivated = dateDeactivated;
    }
}