package org.pahappa.service;

import org.pahappa.model.Patient;
import org.pahappa.model.User;

public interface UserService {
    User login(String username, String password);
    void registerPatient(Patient patient, String password);
}