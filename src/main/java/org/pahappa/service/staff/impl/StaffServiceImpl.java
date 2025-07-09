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
import org.pahappa.exception.HospitalServiceException;
import org.pahappa.exception.ValidationException;
import org.pahappa.exception.ResourceNotFoundException;
import org.pahappa.exception.DuplicateEntryException;

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
    private StaffDao staffDao; 

    @Inject
    private UserDao userDao; 

    @Inject
    private AuditService auditService; 

    @Inject
    private LoginBean loginBean; 

    private String getCurrentUser() {
        if (loginBean != null && loginBean.getLoggedInUser() != null) {
            return loginBean.getLoggedInUser().getUsername(); 
        }
        return "system";
    }

    private String getCurrentUserId() {
        if (loginBean != null && loginBean.getLoggedInUser() != null && loginBean.getLoggedInUser().getId() != null) {
            return loginBean.getLoggedInUser().getId().toString(); 
        }
        return "0";
    }

    @Override
    public List getAllStaff() {
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
    public List getSoftDeletedStaff() {
        List<Staff> allStaff = staffDao.getAllDeleted();
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
    public void addStaff(Staff staff, String password) throws HospitalServiceException {
        try {
            validateStaff(staff); 
            if (password == null || password.trim().isEmpty()) {
                throw new ValidationException("A password is required to create a staff user account."); 
            }

            String username = staff.getEmail();
            if (userDao.findByUsername(username) != null) {
                throw new DuplicateEntryException("An account with the email '" + username + "' already exists."); 
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
        } catch (ValidationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to add staff: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateStaff(Staff staff) throws HospitalServiceException {
        try {
            Staff original = staffDao.getById(staff.getId()); 
            if (original == null) {
                throw new ResourceNotFoundException("Original Staff not found for ID: " + staff.getId()); 
            }
            if (staff.getId() == null) {
                throw new ValidationException("Staff ID is required for an update."); 
            }
            if (!original.getEmail().equalsIgnoreCase(staff.getEmail()) &&
                    staffDao.getAll().stream().anyMatch(s -> s.getEmail().equalsIgnoreCase(staff.getEmail()) && !s.isDeleted() && !s.getId().equals(staff.getId()))) {
                throw new DuplicateEntryException("Staff with email '" + staff.getEmail() + "' already exists.");
            }

            validateStaff(staff);
            staffDao.update(staff); 

            String details = "Staff ID: " + staff.getId() + ", New Name: " + staff.getFirstName() + " " + staff.getLastName();
            auditService.logUpdate(original, staff, getCurrentUserId(), getCurrentUser(), details); 
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to update staff: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteStaff(Long id) throws HospitalServiceException {
        softDeleteStaff(id); 
    }

    @Override
    public void softDeleteStaff(Long id) throws HospitalServiceException {
        try {
            if (id == null || id <= 0) {
                throw new ValidationException("Invalid ID for soft deletion."); 
            }
            Staff staff = staffDao.getById(id); 
            if (staff == null) {
                throw new ResourceNotFoundException("Staff not found with ID: " + id);
            }
            if (staff.isDeleted()) {
                throw new ValidationException("Staff is already soft-deleted.");
            }

            staff.setDeleted(true); 
            staffDao.update(staff); 

            String details = "Soft Deleted Staff ID: " + staff.getId() + ", Name: " + staff.getFirstName() + " " + staff.getLastName();
            auditService.logDelete(staff, getCurrentUserId(), getCurrentUser(), details); 
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to soft-delete staff: " + e.getMessage(), e);
        }
    }

    @Override
    public void restoreStaff(Long id) throws HospitalServiceException {
        try {
            if (id == null || id <= 0) {
                throw new ValidationException("Invalid ID for restoration."); 
            }
            Staff staff = staffDao.getById(id); 
            if (staff == null) {
                throw new ResourceNotFoundException("Staff not found with ID: " + id);
            }
            if (!staff.isDeleted()) {
                throw new ValidationException("Staff is not soft-deleted and cannot be restored.");
            }

            staff.setDeleted(false); 
            staffDao.update(staff); 

            String details = "Restored Staff ID: " + staff.getId() + ", Name: " + staff.getFirstName() + " " + staff.getLastName();
            auditService.logUpdate(staff, staff, getCurrentUserId(), getCurrentUser(), details); 
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to restore staff: " + e.getMessage(), e);
        }
    }

    @Override
    public void permanentlyDeleteStaff(Long id) throws HospitalServiceException {
        try {
            if (id == null || id <= 0) {
                throw new ValidationException("Invalid ID for permanent deletion."); 
            }
            Staff staff = staffDao.getById(id); 
            if (staff == null) {
                throw new ResourceNotFoundException("Staff not found with ID: " + id);
            }
            staffDao.delete(id); // This performs a soft-delete based on BaseDao

            String details = "Permanently Deleted Staff ID: " + staff.getId() + ", Name: " + staff.getFirstName() + " " + staff.getLastName();
            auditService.logDelete(staff, getCurrentUserId(), getCurrentUser(), details); 
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to permanently delete staff: " + e.getMessage(), e);
        }
    }

    @Override
    public List getStaffByRole(Role role) {
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

    private void validateStaff(Staff staff) throws ValidationException { // Added throws
        if (staff == null) throw new ValidationException("Staff object cannot be null."); 
        if (staff.getFirstName() == null || staff.getFirstName().trim().isEmpty())
            throw new ValidationException("First Name is required."); 
        if (staff.getLastName() == null || staff.getLastName().trim().isEmpty())
            throw new ValidationException("Last Name is required."); 
        if (staff.getEmail() == null || !Pattern.matches(Constants.EMAIL_REGEX, staff.getEmail()))
            throw new ValidationException(Constants.ERROR_INVALID_EMAIL); 
        if (staff.getDateOfBirth() == null)
            throw new ValidationException("Date of Birth is required."); 
        if (staff.getDateOfBirth().after(new Date()))
            throw new ValidationException("Date of birth cannot be in the future."); 
        if (staff.getRole() == null)
            throw new ValidationException("Role is required."); 
        if (staff.getRole() == Role.DOCTOR && (staff.getSpecialty() == null || staff.getSpecialty().trim().isEmpty())) {
            throw new ValidationException("Specialty is required for doctors."); 
        }
        if (staff.getRole() != Role.DOCTOR && staff.getSpecialty() != null && !staff.getSpecialty().trim().isEmpty()) {
            throw new ValidationException("Specialty should only be set for doctors."); 
        }
    }
}