package org.pahappa.service.staff.impl;

import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.dao.StaffDao;
import org.pahappa.dao.UserDao;
import org.pahappa.model.Appointment;
import org.pahappa.model.Staff;
import org.pahappa.model.User;
import org.pahappa.service.appointment.AppointmentService;
import org.pahappa.service.audit.AuditService;
import org.pahappa.service.role.RoleService;
import org.pahappa.service.staff.StaffService;
import org.pahappa.utils.AppointmentStatus;
import org.pahappa.utils.Constants;
import org.pahappa.model.Role;
import org.pahappa.exception.HospitalServiceException;
import org.pahappa.exception.ValidationException;
import org.pahappa.exception.ResourceNotFoundException;
import org.pahappa.exception.DuplicateEntryException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.pahappa.controller.LoginBean;

@ApplicationScoped
public class StaffServiceImpl implements StaffService {

    @Inject
    private StaffDao staffDao;

    @Inject
    private UserDao userDao;

    @Inject
    private AppointmentService appointmentService;

    @Inject
    private AuditService auditService;

    @Inject
    private RoleService roleService;

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
    public List<Staff> getAllStaff() {
        // FIXED: The DAO already filters for active staff. No need for an extra loop.
        return staffDao.getAll();
    }

    @Override
    public List<Staff> getSoftDeletedStaff() {
        // FIXED: The DAO already filters for deleted staff. No need for an extra loop.
        return staffDao.getAllDeleted();
    }

    @Override
    public Staff getStaff(Long id) {
        return staffDao.getById(id);
    }

    @Override
    public void addStaff(Staff staff, String password) throws HospitalServiceException {
        try {
            validateStaff(staff);
            if (password == null || password.trim().isEmpty()) {
                throw new ValidationException("A password is required to create a staff user account.");
            }

            String username = staff.getEmail();
            if (userDao.findByUsername(username) != null) {
                throw new DuplicateEntryException("An account with the email '" + username + "' already exists.");
            }

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            User user = new User();
            user.setUsername(username);
            user.setPassword(hashedPassword);

            org.pahappa.model.Role staffRoleEntity = roleService.getRoleById(staff.getRole().getId());
            if (staffRoleEntity == null) {
                throw new ResourceNotFoundException("Role not found for staff: " + staff.getRole().getName());
            }

            user.setRole(staffRoleEntity);
            staff.setRole(staffRoleEntity);

            user.setStaff(staff);
            staff.setUser(user);
            staff.setDeleted(false);

            staffDao.save(staff);
            String details = "Name: " + staff.getFirstName() + " " + staff.getLastName() + ", Role: " + staff.getRole().getName();
            auditService.logCreate(staff, getCurrentUserId(), getCurrentUser(), details);
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to add staff: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateStaff(Staff staff) throws HospitalServiceException {
        try {
            Staff original = staffDao.getById(staff.getId());
            if (original == null) {
                throw new ResourceNotFoundException("Original Staff not found for ID: " + staff.getId());
            }
            if (staff.getId() == null) {
                throw new ValidationException("Staff ID is required for an update.");
            }
            if (!original.getEmail().equalsIgnoreCase(staff.getEmail()) &&
                    staffDao.getAll().stream().anyMatch(s -> s.getEmail().equalsIgnoreCase(staff.getEmail()) && !s.isDeleted() && !s.getId().equals(staff.getId()))) {
                throw new DuplicateEntryException("Staff with email '" + staff.getEmail() + "' already exists.");
            }

            validateStaff(staff);

            // Ensure the Role on the staff object is a managed entity
            Role managedRole = roleService.getRoleById(staff.getRole().getId());
            if(managedRole == null) {
                throw new ResourceNotFoundException("Role with ID " + staff.getRole().getId() + " not found.");
            }
            staff.setRole(managedRole);

            User user = staff.getUser();
            if (user != null && !user.getRole().getId().equals(staff.getRole().getId())) {
                user.setRole(staff.getRole());
                userDao.update(user);
            }

            staffDao.update(staff);

            String details = "Staff ID: " + staff.getId() + ", New Name: " + staff.getFirstName() + " " + staff.getLastName();
            auditService.logUpdate(original, staff, getCurrentUserId(), getCurrentUser(), details);
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to update staff: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteStaff(Long id) throws HospitalServiceException {
        softDeleteStaff(id);
    }

    @Override
    public void softDeleteStaff(Long id) throws HospitalServiceException {
        try {
            if (id == null || id <= 0) {
                throw new ValidationException("Invalid ID for soft deletion.");
            }
            Staff staff = staffDao.getById(id);
            if (staff == null) {
                throw new ResourceNotFoundException("Staff not found with ID: " + id);
            }
            if (staff.isDeleted()) {
                throw new ValidationException("Staff is already soft-deleted.");
            }

            User user = staff.getUser();
            if (user != null && user.isActive()) {
                user.setActive(false);
                user.setDateDeactivated(new Date());
                userDao.update(user);
            }

            if (staff.getRole() != null && "DOCTOR".equalsIgnoreCase(staff.getRole().getName())) {
                appointmentService.handleDeactivatedDoctorAppointments(staff.getId());
            }

            staff.setDeleted(true);
            staffDao.update(staff);

            String details = "Soft Deleted Staff ID: " + staff.getId() + ", Name: " + staff.getFullName();
            auditService.logDelete(staff, getCurrentUserId(), getCurrentUser(), details);
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to soft-delete staff: " + e.getMessage(), e);
        }
    }

    @Override
    public void restoreStaff(Long id) throws HospitalServiceException {
        try {
            if (id == null || id <= 0) {
                throw new ValidationException("Invalid ID for restoration.");
            }
            Staff staff = staffDao.getByIdIncludingDeleted(id);
            if (staff == null) {
                throw new ResourceNotFoundException("Staff not found with ID: " + id);
            }
            if (!staff.isDeleted()) {
                throw new ValidationException("Staff is not soft-deleted and cannot be restored.");
            }

            User user = staff.getUser();
            if (user != null && !user.isActive()) {
                user.setActive(true);
                user.setDateDeactivated(null);
                user.setDeleted(false);
                userDao.update(user);
            }

            staff.setDeleted(false);
            staffDao.update(staff);

            String details = "Restored Staff ID: " + staff.getId() + ", Name: " + staff.getFirstName() + " " + staff.getLastName();
            auditService.logUpdate(staff, staff, getCurrentUserId(), getCurrentUser(), details);
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HospitalServiceException("Failed to restore staff: " + e.getMessage(), e);
        }
    }

    @Override
    public void permanentlyDeleteStaff(Long id) throws HospitalServiceException {
        try {
            if (id == null || id <= 0) {
                throw new ValidationException("Invalid ID for permanent deletion.");
            }
            Staff staff = staffDao.getByIdIncludingDeleted(id);
            if (staff == null) {
                throw new ResourceNotFoundException("Staff not found with ID: " + id);
            }

            staffDao.hardDelete(id);

            String details = "Permanently Deleted Staff ID: " + staff.getId() + ", Name: " + staff.getFullName();
            auditService.logDelete(staff, getCurrentUserId(), getCurrentUser(), details);
        } catch (Exception e) {
            throw new HospitalServiceException("Failed to permanently delete staff", e);
        }
    }

    @Override
    public List<Staff> getStaffByRole(org.pahappa.model.Role role) {
        // Use a stream on the already filtered list of active staff
        return getAllStaff().stream()
                .filter(staff -> staff.getRole() != null && staff.getRole().getId().equals(role.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public long countDoctors() {
        Role doctorRole = roleService.getRoleByName("DOCTOR");
        return doctorRole != null ? staffDao.countByRole(doctorRole) : 0;
    }
    @Override
    public long countNurses() {
        Role nurseRole = roleService.getRoleByName("NURSE");
        return nurseRole != null ? staffDao.countByRole(nurseRole) : 0;
    }

    @Override
    public long countReceptionists() {
        Role receptionistRole = roleService.getRoleByName("RECEPTIONIST");
        return receptionistRole != null ? staffDao.countByRole(receptionistRole) : 0;
    }

    private void validateStaff(Staff staff) throws ValidationException {
        if (staff == null) throw new ValidationException("Staff object cannot be null.");
        if (staff.getFirstName() == null || staff.getFirstName().trim().isEmpty())
            throw new ValidationException("First Name is required.");
        if (staff.getLastName() == null || staff.getLastName().trim().isEmpty())
            throw new ValidationException("Last Name is required.");
        if (staff.getEmail() == null || !Pattern.matches(Constants.EMAIL_REGEX, staff.getEmail()))
            throw new ValidationException(Constants.ERROR_INVALID_EMAIL);
        if (staff.getDateOfBirth() == null)
            throw new ValidationException("Date of Birth is required.");
        if (staff.getDateOfBirth().after(new Date()))
            throw new ValidationException("Date of birth cannot be in the future.");

        if (staff.getRole() == null || staff.getRole().getId() == null) {
            throw new ValidationException("Role is required.");
        }

        Role role = roleService.getRoleById(staff.getRole().getId());
        if (role == null) {
            throw new ValidationException("Invalid role selected.");
        }

        if (role.getName().equalsIgnoreCase("DOCTOR") && (staff.getSpecialty() == null || staff.getSpecialty().trim().isEmpty())) {
            throw new ValidationException("Specialty is required for doctors.");
        }
        if (!role.getName().equalsIgnoreCase("DOCTOR") && staff.getSpecialty() != null && !staff.getSpecialty().trim().isEmpty()) {
            staff.setSpecialty(null); // Automatically clear specialty for non-doctors
        }
    }

    @Override
    public List<Staff> findAvailableDoctorsForSlot(Date startTime) {
        Role doctorRole = roleService.getRoleByName("DOCTOR");
        if (doctorRole == null) {
            return Collections.emptyList();
        }

        List<Staff> allDoctors = getStaffByRole(doctorRole);
        List<Appointment> allAppointments = appointmentService.getAllAppointments();

        Set<Long> busyDoctorIds = allAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .filter(a -> {
                    Instant slotStart = startTime.toInstant();
                    Instant slotEnd = slotStart.plus(Constants.APPOINTMENT_DURATION_MINUTES, ChronoUnit.MINUTES);
                    Instant existingStart = a.getAppointmentDate().toInstant();
                    Instant existingEnd = existingStart.plus(Constants.APPOINTMENT_DURATION_MINUTES, ChronoUnit.MINUTES);
                    return slotStart.isBefore(existingEnd) && slotEnd.isAfter(existingStart);
                })
                .map(a -> a.getDoctor().getId())
                .collect(Collectors.toSet());

        return allDoctors.stream()
                .filter(doctor -> !busyDoctorIds.contains(doctor.getId()))
                .collect(Collectors.toList());
    }
}