package org.pahappa.service.staff;

import org.pahappa.exception.HospitalServiceException;
import org.pahappa.model.Staff;
import org.pahappa.model.Role;
import java.util.Date;
import java.util.List;

public interface StaffService {
    void addStaff(Staff staff, String password) throws HospitalServiceException;
    void updateStaff(Staff staff) throws HospitalServiceException;
    void deleteStaff(Long id) throws HospitalServiceException;
    void softDeleteStaff(Long id) throws HospitalServiceException;
    void restoreStaff(Long id) throws HospitalServiceException;
    void permanentlyDeleteStaff(Long id) throws HospitalServiceException;
    Staff getStaff(Long id);
    List<Staff> getAllStaff();
    List<Staff> getSoftDeletedStaff();
    List<Staff> getStaffByRole(Role role);
    long countDoctors();
    long countNurses();
    long countReceptionists();
    List<Staff> findAvailableDoctorsForSlot(Date startTime);
}