package org.pahappa.com.model;

import jakarta.persistence.*;

@Entity
@Table(name = "staff")
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String role;

    @Column
    private String specialty;

    @Column(nullable = false, unique = true)
    private String email;

    public Staff() {}

    public Staff(String firstName, String lastName, String role, String specialty, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.specialty = specialty;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "Staff{id=" + id + ", name=" + firstName + " " + lastName + ", role=" + role + ", specialty=" + specialty + "}";
    }
}