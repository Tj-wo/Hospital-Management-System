package org.pahappa.model;

import org.pahappa.utils.Role;
import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "staff")
public class Staff extends BaseModel {

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String specialty;

    @OneToOne(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
    private User user;

    public Staff() {}

    // Getters and setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getFullName() { return firstName + " " + lastName; }

    @Override
    public String toString() {
        return "Staff{" +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", role=" + role +
                ", specialty='" + (specialty != null ? specialty : "N/A") + '\'' +
                '}';
    }
}