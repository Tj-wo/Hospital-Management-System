package org.pahappa.service.staff.impl;

import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.dao.StaffDao;
import org.pahappa.dao.UserDao;
import org.pahappa.model.Staff;
import org.pahappa.model.User;
import org.pahappa.service.staff.StaffService;
import org.pahappa.utils.Constants;
import org.pahappa.utils.Role;

import javax.enterprise.context.ApplicationScoped;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ApplicationScoped
public class StaffServiceImpl implements StaffService {

    private final StaffDao staffDao = new StaffDao();
    private final UserDao userDao = new UserDao();

    @Override
    public void addStaff(Staff staff, String password) {
        validateStaff(staff);
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("A password is required to create a staff user account.");
        }

        String username = staff.getEmail();
        if (userDao.findByUsername(username) != null) {
            throw new IllegalArgumentException("An account with the email '" + username + "' already exists.");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setRole(staff.getRole());
        user.setStaff(staff);
        staff.setUser(user);

        staffDao.save(staff);
    }

    @Override
    public void updateStaff(Staff staff) {
        if (staff.getId() == null) {
            throw new IllegalArgumentException("Staff ID is required for an update.");
        }
        validateStaff(staff);
        staffDao.update(staff);
    }

    @Override
    public void deleteStaff(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid ID for deletion.");
        }
        staffDao.delete(id);
    }

    @Override
    public Staff getStaff(Long id) {
        return staffDao.getById(id);
    }

    @Override
    public List<Staff> getAllStaff() {
        return staffDao.getAll();
    }

    @Override
    public List<Staff> getStaffByRole(Role role) {
        return staffDao.getAll().stream()
                .filter(staff -> staff.getRole() == role)
                .collect(Collectors.toList());
    }

    @Override
    public long countDoctors() {
        return getStaffByRole(Role.DOCTOR).size();
    }

    @Override
    public long countNurses() {
        return getStaffByRole(Role.NURSE).size();
    }

    @Override
    public long countReceptionists() {
        return getStaffByRole(Role.RECEPTIONIST).size();
    }

    private void validateStaff(Staff staff) {
        if (staff == null) throw new IllegalArgumentException("Staff object cannot be null.");
        if (staff.getFirstName() == null || staff.getFirstName().trim().isEmpty()) throw new IllegalArgumentException("First Name is required.");
        if (staff.getLastName() == null || staff.getLastName().trim().isEmpty()) throw new IllegalArgumentException("Last Name is required.");
        if (staff.getEmail() == null || !Pattern.matches(Constants.EMAIL_REGEX, staff.getEmail())) throw new IllegalArgumentException(Constants.ERROR_INVALID_EMAIL);
        if (staff.getDateOfBirth() == null) throw new IllegalArgumentException("Date of Birth is required.");
        if (staff.getDateOfBirth().after(new Date())) throw new IllegalArgumentException("Date of birth cannot be in the future.");
        if (staff.getRole() == null) throw new IllegalArgumentException("Role is required.");
        if (staff.getRole() == Role.DOCTOR && (staff.getSpecialty() == null || staff.getSpecialty().trim().isEmpty())) {
            throw new IllegalArgumentException("Specialty is required for doctors.");
        }
        if (staff.getRole() != Role.DOCTOR && staff.getSpecialty() != null && !staff.getSpecialty().trim().isEmpty()) {
            throw new IllegalArgumentException("Specialty should only be set for doctors.");
        }
    }
}
