package org.pahappa.service.user.impl;

import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.dao.UserDao;
import org.pahappa.model.Patient;
import org.pahappa.model.User;
import org.pahappa.service.audit.AuditService;
import org.pahappa.service.patient.PatientService;
import org.pahappa.service.user.UserService;
import org.pahappa.utils.Role;
import org.pahappa.exception.HospitalServiceException;
import org.pahappa.exception.ValidationException;
import org.pahappa.exception.DuplicateEntryException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.pahappa.controller.LoginBean;

@ApplicationScoped
public class UserServiceImpl implements UserService {

    @Inject
    private UserDao userDao; 

    @Inject
    private AuditService auditService; 

    @Inject
    private PatientService patientService; 

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
            user.setRole(Role.PATIENT); 
            user.setPatient(patient); 
            patient.setUser(user); 

            patientService.addPatient(patient); // This will save patient and cascade to user

            String details = "New Patient Registered: " + patient.getFirstName() + " " + patient.getLastName() + ", Username: " + user.getUsername();
            auditService.logCreate(user, getCurrentUserId(), getCurrentUser(), details); 
        } catch (ValidationException  e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to register patient: " + e.getMessage(), e);
        }
    }
}