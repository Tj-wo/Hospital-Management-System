package org.pahappa.service.staff;

import org.pahappa.model.Staff;
import org.pahappa.utils.Role;

import java.util.List;

public interface StaffService {
    void addStaff(Staff staff, String password);
    void updateStaff(Staff staff);
    void deleteStaff(Long id); // Keep existing method, but it will now be soft-delete
    void softDeleteStaff(Long id); // Explicit soft-delete method
    void restoreStaff(Long id);
    void permanentlyDeleteStaff(Long id);
    Staff getStaff(Long id);
    List<Staff> getAllStaff();
    List<Staff> getSoftDeletedStaff();
    List<Staff> getStaffByRole(Role role);
    long countDoctors();
    long countNurses();
    long countReceptionists();
}