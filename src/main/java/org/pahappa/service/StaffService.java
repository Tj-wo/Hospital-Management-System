package org.pahappa.service;

import org.pahappa.dao.StaffDao;
import org.pahappa.model.Staff;
import java.util.List;
import java.util.Scanner;

public class StaffService {
    private final StaffDao staffDao = new StaffDao();
    private static final Scanner scanner = new Scanner(System.in);

    public void registerStaff(Staff staff) {
        staffDao.save(staff);
    }

    public Staff getStaff(Long id) {
        return staffDao.getById(id);
    }

    public List<Staff> getAllStaff() {
        return staffDao.getAll();
    }

    public List<Staff> getDoctorsBySpecialty(String specialty) {
        return staffDao.getDoctorsBySpecialty(specialty);
    }

    public void updateStaff(Staff staff) {
        staffDao.update(staff);
    }

    public void deleteStaff(Long id) {
        staffDao.delete(id);
    }

    public void addStaffInteractive() {
        try {
            String firstName = getStringInput("Enter First Name: ");
            if (firstName.isEmpty()) {
                System.out.println("First Name is required!");
                return;
            }

            String lastName = getStringInput("Enter Last Name: ");
            if (lastName.isEmpty()) {
                System.out.println("Last Name is required!");
                return;
            }

            String role = getStringInput("Enter Role: ");
            if (role.isEmpty()) {
                System.out.println("Role is required!");
                return;
            }

            String specialty = getStringInput("Enter Specialty (optional): ");
            String email = getStringInput("Enter Email: ");
            if (email.isEmpty()) {
                System.out.println("Email is required!");
                return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                System.out.println("Invalid email format!");
                return;
            }

            Staff staff = new Staff(firstName, lastName, role, specialty, email);
            registerStaff(staff);
            System.out.println("Staff added successfully!");
        } catch (Exception e) {
            System.out.println("Error adding staff: " + e.getMessage());
        }
    }

    public void viewStaff() {
        System.out.println("Staff:");
        getAllStaff().forEach(System.out::println);
        if (getAllStaff().isEmpty()) {
            System.out.println("No staff found.");
        }
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}