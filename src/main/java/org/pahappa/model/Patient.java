package org.pahappa.model;


import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "patients")
public class Patient extends BaseModel {

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private User user;

    public Patient() {}

    // Getters and setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getFullName() { return firstName + " " + lastName; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }


    @Override
    public String toString() {
        return "Patient{" +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", deleted=" + deleted +
                '}';
    }
}

