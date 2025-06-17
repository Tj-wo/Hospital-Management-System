package org.pahappa.com.model;

import javax.persistence.*;
<<<<<<< Updated upstream
import java.sql.Date;
=======
import java.time.LocalDate;
>>>>>>> Stashed changes

@Entity
@Table(name = "patients")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
<<<<<<< Updated upstream
    private Date dateOfBirth;
=======
    private LocalDate dateOfBirth;
>>>>>>> Stashed changes

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String phone;

    @Column
    private String address;

    public Patient() {}

<<<<<<< Updated upstream
    public Patient(String firstName, String lastName, Date dateOfBirth, String email, String phone, String address) {
=======
    public Patient(String firstName, String lastName, LocalDate dateOfBirth, String email, String phone, String address) {
>>>>>>> Stashed changes
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
<<<<<<< Updated upstream
    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }
=======
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
>>>>>>> Stashed changes
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public String toString() {
        return "Patient{id=" + id + ", name=" + firstName + " " + lastName + ", email=" + email + "}";
    }
}