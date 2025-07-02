package org.pahappa.model;

import org.hibernate.envers.Audited;
import org.pahappa.utils.Role;
import javax.persistence.*;

@Entity
@Audited
@Table(name = "users")
public class User extends BaseModel {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToOne
    @JoinColumn(name = "patient_id", unique = true)
    private Patient patient;

    @OneToOne
    @JoinColumn(name = "staff_id", unique = true)
    private Staff staff;

    public User() {}

    // Getters and Setters for User-specific fields
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Staff getStaff() { return staff; }
    public void setStaff(Staff staff) { this.staff = staff; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
}