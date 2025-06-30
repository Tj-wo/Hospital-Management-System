
package org.pahappa.service;

import org.pahappa.model.Staff;
import org.pahappa.utils.Role;

import java.util.List;

public interface StaffService {
    void addStaff(Staff staff, String password);
    void updateStaff(Staff staff);
    void deleteStaff(Long id);
    Staff getStaff(Long id);
    List<Staff> getAllStaff();
    List<Staff> getStaffByRole(Role role);
}
