package org.pahappa.service.user;

import org.pahappa.model.Patient;
import org.pahappa.model.User;
import org.pahappa.exception.HospitalServiceException;
import java.util.List;

public interface UserService {

    User login(String username, String password);
    void registerPatient(Patient patient, String password) throws HospitalServiceException;
    List<User> findDeactivatedUsers();
    void reactivateUserAndProfile(Long userId) throws HospitalServiceException;
    void deactivateUserAndProfile(Long userId) throws HospitalServiceException;
}