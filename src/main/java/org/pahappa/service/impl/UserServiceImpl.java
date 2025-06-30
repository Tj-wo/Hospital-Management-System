package org.pahappa.service.impl;

import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.dao.UserDao;
import org.pahappa.model.Patient;
import org.pahappa.model.User;
import org.pahappa.service.PatientService;
import org.pahappa.service.UserService;
import org.pahappa.utils.Role;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class UserServiceImpl implements UserService {

    private final UserDao userDao = new UserDao();

    @Inject
    private PatientService patientService;

    @Override
    public User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        User user = userDao.findByUsername(username);
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public void registerPatient(Patient patient, String password) {
        if (patient == null || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient data and password are required for registration.");
        }

        String username = patient.getEmail();
        if (userDao.findByUsername(username) != null) {
            throw new IllegalArgumentException("An account with the email '" + username + "' already exists.");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setRole(Role.PATIENT);
        user.setPatient(patient);
        patient.setUser(user);

        patientService.addPatient(patient);
    }
}
