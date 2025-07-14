package org.pahappa.service.user.impl;

import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.dao.UserDao;
import org.pahappa.exception.ResourceNotFoundException;
import org.pahappa.model.Patient;
import org.pahappa.model.User;
import org.pahappa.service.audit.AuditService;
import org.pahappa.service.patient.PatientService;
import org.pahappa.service.role.RoleService;
import org.pahappa.service.staff.StaffService;
import org.pahappa.service.user.UserService;
import org.pahappa.model.Role;
import org.pahappa.exception.HospitalServiceException;
import org.pahappa.exception.ValidationException;
import org.pahappa.exception.DuplicateEntryException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.pahappa.controller.LoginBean;
import java.util.List;

@ApplicationScoped
public class UserServiceImpl implements UserService {

    @Inject
    private UserDao userDao;

    @Inject
    private AuditService auditService;

    @Inject
    private PatientService patientService;

    @Inject
    private RoleService roleService;

    @Inject
    private StaffService staffService;

    @Inject
    private LoginBean loginBean;

    // ... (getCurrentUser and getCurrentUserId methods remain the same) ...
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
    public User login(String username, String password) {
        // This method is fine as it doesn't throw checked exceptions
        if (username == null || password == null) {
            return null;
        }
        User user = userDao.findByUsername(username);
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            String details = "User: " + user.getUsername() + " logged in successfully.";
            auditService.logLogin(user.getId().toString(), user.getUsername(), details);
            return user;
        }
        String failedLoginDetails = "Failed login attempt for user: " + username;
        auditService.logLogin("0", username, failedLoginDetails);
        return null;
    }

    @Override
    public void registerPatient(Patient patient, String password) throws HospitalServiceException {
        // This method is already correct
        try {
            if (patient == null || password == null || password.trim().isEmpty()) {
                throw new ValidationException("Patient data and password are required for registration.");
            }
            String username = patient.getEmail();
            if (userDao.findByUsername(username) != null) {
                throw new DuplicateEntryException("An account with the email '" + username + "' already exists.");
            }
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            User user = new User();
            user.setUsername(username);
            user.setPassword(hashedPassword);

            Role patientRole = roleService.getRoleByName("PATIENT");
            if (patientRole == null) {
                throw new ResourceNotFoundException("Role 'PATIENT' not found in the system. Cannot register patient.");
            }
            user.setRole(patientRole);

            user.setPatient(patient);
            patient.setUser(user);
            patientService.addPatient(patient);
            String details = "New Patient Registered: " + patient.getFirstName() + " " + patient.getLastName() + ", Username: " + user.getUsername();
            auditService.logCreate(user, getCurrentUserId(), getCurrentUser(), details);
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to register patient: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findDeactivatedUsers() {
        // This method is fine as it doesn't throw checked exceptions
        return userDao.findDeactivated();
    }

    @Override
    public void reactivateUserAndProfile(Long userId) throws HospitalServiceException {
        try {
            User user = userDao.getById(userId);
            if (user == null || user.isActive()) {
                throw new ResourceNotFoundException("Deactivated user not found with ID: " + userId);
            }

            user.setActive(true);
            user.setDateDeactivated(null);
            userDao.update(user);

            if (user.getPatient() != null) {
                patientService.restorePatient(user.getPatient().getId());
            } else if (user.getStaff() != null) {
                staffService.restoreStaff(user.getStaff().getId());
            }
        } catch (ResourceNotFoundException e) {
            // Re-throw the specific exception to be handled by the controller
            throw e;
        } catch (RuntimeException e) {
            // Wrap any unexpected runtime errors in our main service exception
            throw new HospitalServiceException("An unexpected error occurred while reactivating user.", e);
        }
    }

    @Override
    public void deactivateUserAndProfile(Long userId) throws HospitalServiceException {
        try {
            User user = userDao.getById(userId);
            if (user == null || !user.isActive()) {
                throw new ResourceNotFoundException("Active user not found with ID: " + userId);
            }

            user.setActive(false);
            user.setDateDeactivated(new java.util.Date());
            userDao.update(user);

            if (user.getPatient() != null) {
                patientService.softDeletePatient(user.getPatient().getId());
            } else if (user.getStaff() != null) {
                staffService.softDeleteStaff(user.getStaff().getId());
            }
        } catch (ResourceNotFoundException e) {
            // Re-throw the specific exception
            throw e;
        } catch (RuntimeException e) {
            // Wrap unexpected runtime errors
            throw new HospitalServiceException("An unexpected error occurred while deactivating user.", e);
        }
    }
}