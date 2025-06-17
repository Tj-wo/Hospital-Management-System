<<<<<<< Updated upstream
=======
package org.pahappa.com.service;

import org.pahappa.com.dao.PatientDao;
import org.pahappa.com.model.Patient;
import java.util.List;

public class PatientService {
    private final PatientDao patientDao = new PatientDao();

    public void registerPatient(Patient patient) {
        patientDao.save(patient);
    }

    public Patient getPatient(Long id) {
        return patientDao.getById(id);
    }

    public List<Patient> getAllPatients() {
        return patientDao.getAll();
    }

    public void updatePatient(Patient patient) {
        patientDao.update(patient);
    }

    public void deletePatient(Long id) {
        patientDao.delete(id);
    }

    public void testConnection() {
        List<Patient> patients = getAllPatients();
        System.out.println("Connected to DB. Found " + patients.size() + " patients.");
        for (Patient p : patients) {
            System.out.println(p);
        }
    }
<<<<<<< Updated upstream
}
>>>>>>> Stashed changes
=======
}
>>>>>>> Stashed changes
