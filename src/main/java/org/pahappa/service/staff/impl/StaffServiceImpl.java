package org.pahappa.service.staff.impl;

import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.dao.StaffDao;
import org.pahappa.dao.UserDao;
import org.pahappa.model.Staff;
import org.pahappa.model.User;
import org.pahappa.service.audit.AuditService;
import org.pahappa.service.staff.StaffService;
import org.pahappa.utils.Constants;
import org.pahappa.utils.Role;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.pahappa.controller.LoginBean;


@ApplicationScoped
public class StaffServiceImpl implements StaffService {

    @Inject
    private  StaffDao staffDao;

    @Inject
    private  UserDao userDao;

    @Inject
    private AuditService auditService;

    // Inject the LoginBean
    @Inject
    private LoginBean loginBean;

    // Helper method to get the current username from the LoginBean
    private String getCurrentUser() {
        if (loginBean != null && loginBean.getLoggedInUser() != null) {
            return loginBean.getLoggedInUser().getUsername();
        }
        return "system"; // Fallback for background tasks or unauthenticated operations
    }

    // Helper method to get the current user ID from the LoginBean
    private String getCurrentUserId() {
        if (loginBean != null && loginBean.getLoggedInUser() != null && loginBean.getLoggedInUser().getId() != null) {
            return loginBean.getLoggedInUser().getId().toString();
        }
        return "0"; // Default ID for system or unauthenticated actions
    }


    @Override
    public List<Staff> getAllStaff() {
        List<Staff> allStaff = staffDao.getAll();
        List<Staff> activeStaff = new ArrayList<>();
        for (Staff staff : allStaff) {
            if (!staff.isDeleted()) {
                activeStaff.add(staff);
            }
        }
        return activeStaff;
    }

    @Override
    public List<Staff> getSoftDeletedStaff() {
        List<Staff> allStaff = staffDao.getAll();
        List<Staff> deletedStaff = new ArrayList<>();
        for (Staff staff : allStaff) {
            if (staff.isDeleted()) {
                deletedStaff.add(staff);
            }
        }
        return deletedStaff;
    }

    @Override
    public Staff getStaff(Long id) {
        return staffDao.getById(id);
    }

    @Override
    public void addStaff(Staff staff, String password) {
        validateStaff(staff);
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("A password is required to create a staff user account.");
        }

        String username = staff.getEmail();
        if (userDao.findByUsername(username) != null) {
            throw new IllegalArgumentException("An account with the email '" + username + "' already exists.");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setRole(staff.getRole());
        user.setStaff(staff);
        staff.setUser(user);
        staff.setDeleted(false);

        staffDao.save(staff);
        String details = "Name: " + staff.getFirstName() + " " + staff.getLastName() + ", Role: " + staff.getRole().name();

        auditService.logCreate(staff, getCurrentUserId(), getCurrentUser(), details);
    }

    @Override
    public void updateStaff(Staff staff) {
        Staff original = staffDao.getById(staff.getId());
        if (original == null) { // Defensive check
            throw new IllegalArgumentException("Original Staff not found for ID: " + staff.getId());
        }
        if (staff.getId() == null) {
            throw new IllegalArgumentException("Staff ID is required for an update.");
        }
        validateStaff(staff);
        staffDao.update(staff);
        String details = "Staff ID: " + staff.getId() + ", New Name: " + staff.getFirstName() + " " + staff.getLastName();
        auditService.logUpdate(original, staff, getCurrentUserId(), getCurrentUser(), details);
    }

    @Override
    public void deleteStaff(Long id) {
        softDeleteStaff(id);
    }

    @Override
    public void softDeleteStaff(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid ID for soft deletion.");
        }
        Staff staff = staffDao.getById(id);
        if (staff != null) {
            staff.setDeleted(true);
            staffDao.update(staff);
            String details = "Soft Deleted Staff ID: " + staff.getId() + ", Name: " + staff.getFirstName() + " " + staff.getLastName();
            // Corrected logDelete call: entity, userId, username, details
            auditService.logDelete(staff, getCurrentUserId(), getCurrentUser(), details);
        }
    }

    @Override
    public void restoreStaff(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid ID for restoration.");
        }
        Staff staff = staffDao.getByIdIncludingDeleted(id);
        if (staff != null && staff.isDeleted()) {
            staff.setDeleted(false);
            staffDao.update(staff);
            String details = "Restored Staff ID: " + staff.getId() + ", Name: " + staff.getFirstName() + " " + staff.getLastName();
            auditService.logUpdate(staff, staff, getCurrentUserId(), getCurrentUser(), details);
        }
    }

    @Override
    public void permanentlyDeleteStaff(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid ID for permanent deletion.");
        }
        Staff staff = staffDao.getByIdIncludingDeleted(id);
        staffDao.delete(id);
        String details = "Permanently Deleted Staff ID: " + staff.getId() + ", Name: " + staff.getFirstName() + " " + staff.getLastName();
        // Corrected logDelete call: entity, userId, username, details
        auditService.logDelete(staff, getCurrentUserId(), getCurrentUser(), details);
    }

    @Override
    public List<Staff> getStaffByRole(Role role) {
        List<Staff> allStaff = getAllStaff();
        List<Staff> staffByRole = new ArrayList<>();
        for (Staff staff : allStaff) {
            if (staff.getRole() == role) {
                staffByRole.add(staff);
            }
        }
        return staffByRole;
    }

    @Override
    public long countDoctors() {
        return getStaffByRole(Role.DOCTOR).size();
    }

    @Override
    public long countNurses() {
        return getStaffByRole(Role.NURSE).size();
    }

    @Override
    public long countReceptionists() {
        return getStaffByRole(Role.RECEPTIONIST).size();
    }

    private void validateStaff(Staff staff) {
        if (staff == null) throw new IllegalArgumentException("Staff object cannot be null.");
        if (staff.getFirstName() == null || staff.getFirstName().trim().isEmpty())
            throw new IllegalArgumentException("First Name is required.");
        if (staff.getLastName() == null || staff.getLastName().trim().isEmpty())
            throw new IllegalArgumentException("Last Name is required.");
        if (staff.getEmail() == null || !Pattern.matches(Constants.EMAIL_REGEX, staff.getEmail()))
            throw new IllegalArgumentException(Constants.ERROR_INVALID_EMAIL);
        if (staff.getDateOfBirth() == null)
            throw new IllegalArgumentException("Date of Birth is required.");
        if (staff.getDateOfBirth().after(new Date()))
            throw new IllegalArgumentException("Date of birth cannot be in the future.");
        if (staff.getRole() == null)
            throw new IllegalArgumentException("Role is required.");
        if (staff.getRole() == Role.DOCTOR && (staff.getSpecialty() == null || staff.getSpecialty().trim().isEmpty())) {
            throw new IllegalArgumentException("Specialty is required for doctors.");
        }
        if (staff.getRole() != Role.DOCTOR && staff.getSpecialty() != null && !staff.getSpecialty().trim().isEmpty()) {
            throw new IllegalArgumentException("Specialty should only be set for doctors.");
        }
    }
}