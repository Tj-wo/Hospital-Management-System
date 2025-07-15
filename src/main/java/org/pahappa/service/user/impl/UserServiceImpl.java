package org.pahappa.service.user.impl;

import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.dao.UserDao;
import org.pahappa.exception.ResourceNotFoundException;
import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
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

import java.util.Date;
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
        return userDao.findDeactivated();
    }

    /**
     * CORRECTED: Reactivates a user.
     * This now correctly fetches the user even if they were previously soft-deleted by the old logic.
     * It also ensures the associated profile is not deleted.
     */
    @Override
    public void reactivateUserAndProfile(Long userId) throws HospitalServiceException {
        try {
            // Use getByIdIncludingDeleted to find the user even if the old logic soft-deleted them.
            User user = userDao.getByIdIncludingDeleted(userId);
            if (user == null) {
                throw new ResourceNotFoundException("User not found with ID: " + userId);
            }
            if (user.isActive()) {
                throw new ValidationException("User is already active.");
            }

            user.setActive(true);
            user.setDateDeactivated(null);
            user.setDeleted(false); // Explicitly mark as not deleted
            userDao.update(user);

            // Also ensure the associated profile is not deleted
            if (user.getPatient() != null && user.getPatient().isDeleted()) {
                patientService.restorePatient(user.getPatient().getId());
            } else if (user.getStaff() != null && user.getStaff().isDeleted()) {
                staffService.restoreStaff(user.getStaff().getId());
            }

            String details = "User account reactivated for: " + user.getUsername();
            auditService.logUpdate(user, user, getCurrentUserId(), getCurrentUser(), details);

        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("An unexpected error occurred while reactivating user.", e);
        }
    }

    /**
     * CORRECTED: Deactivates a user by only setting their `isActive` flag to false.
     * It no longer soft-deletes the associated Patient or Staff profile.
     */
    @Override
    public void deactivateUserAndProfile(Long userId) throws HospitalServiceException {
        try {
            User user = userDao.getById(userId);
            if (user == null) {
                throw new ResourceNotFoundException("Active user not found with ID: " + userId);
            }
            if (!user.isActive()) {
                throw new ValidationException("User is already deactivated.");
            }

            user.setActive(false);
            user.setDateDeactivated(new Date());
            userDao.update(user);

            String details = "User account deactivated for: " + user.getUsername();
            auditService.logUpdate(user, user, getCurrentUserId(), getCurrentUser(), details);

        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("An unexpected error occurred while deactivating user.", e);
        }
    }

    @Override
    public int cleanupLegacyDeletedUsers() throws HospitalServiceException {
        try {
            List<User> legacyUsers = userDao.findLegacyDeletedUsers();
            if (legacyUsers.isEmpty()) {
                return 0; // Nothing to fix
            }

            for (User user : legacyUsers) {
                user.setActive(false);       // Mark as deactivated
                user.setDeleted(false);      // Mark as NOT deleted (so it appears in the list)
                if (user.getDateDeactivated() == null) {
                    user.setDateDeactivated(user.getDateUpdated() != null ? user.getDateUpdated() : new Date());
                }
                userDao.update(user);
            }

            String details = String.format("Cleaned up %d legacy soft-deleted user accounts.", legacyUsers.size());
            auditService.logUpdate(null, null, getCurrentUserId(), getCurrentUser(), details);

            return legacyUsers.size();
        } catch (Exception e) {
            throw new HospitalServiceException("An error occurred during legacy user cleanup.", e);
        }
    }
}