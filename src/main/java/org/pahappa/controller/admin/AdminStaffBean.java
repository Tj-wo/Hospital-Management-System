package org.pahappa.controller.admin;

import org.pahappa.model.Staff;
import org.pahappa.model.Role;
import org.pahappa.service.role.RoleService;
import org.pahappa.service.staff.StaffService;
import org.pahappa.exception.HospitalServiceException;
import org.pahappa.exception.ValidationException;
import org.pahappa.exception.ResourceNotFoundException;

import org.primefaces.PrimeFaces;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Named("adminStaffBean")
@ViewScoped
public class AdminStaffBean implements Serializable {

    @Inject
    private StaffService staffService;

    @Inject
    private RoleService roleService;

    private List<Staff> staffList;
    private Staff newStaff;
    private String password;
    private Long selectedRoleId;
    private List<Role> availableRoles;

    // ADDED: Field to hold the global filter text
    private String globalFilter;

    @PostConstruct
    public void init() {
        staffList = staffService.getAllStaff();
        availableRoles = roleService.getAllRoles();
        prepareNewStaff();
    }

    public void prepareNewStaff() {
        newStaff = new Staff();
        password = null;
        selectedRoleId = null;
    }

    public void selectStaffForEdit(Staff staff) {
        this.newStaff = staffService.getStaff(staff.getId());
        if (this.newStaff != null && this.newStaff.getRole() != null) {
            this.selectedRoleId = this.newStaff.getRole().getId();
        } else {
            this.selectedRoleId = null;
        }
        this.password = null;
    }

    public void saveStaff() {
        try {
            if (selectedRoleId == null) {
                throw new ValidationException("Please select a role for the staff member.");
            }
            Role role = roleService.getRoleById(selectedRoleId);
            if (role == null) {
                throw new ResourceNotFoundException("Selected role not found. Please refresh and try again.");
            }
            newStaff.setRole(role);

            String staffName = newStaff.getFullName();

            if (newStaff.getId() == null) {
                staffService.addStaff(newStaff, password);
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Staff member '" + staffName + "' was added.");
            } else {
                staffService.updateStaff(newStaff);
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Staff member '" + staffName + "' was updated.");
            }

            init(); // Reload all data from the database and reset the form state

        } catch (ValidationException | ResourceNotFoundException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Validation Failed", e.getMessage());
            PrimeFaces.current().ajax().update("dialogForm:dialogMessages");
        } catch (HospitalServiceException hse) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Operation Failed", hse.getMessage());
            PrimeFaces.current().ajax().update("dialogForm:dialogMessages");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred.");
            PrimeFaces.current().ajax().update("dialogForm:dialogMessages");
            e.printStackTrace();
        }
    }

    public void softDeleteStaff(Staff staffToDelete) {
        if (staffToDelete == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Warning", "No staff member selected for deletion.");
            return;
        }
        try {
            staffService.softDeleteStaff(staffToDelete.getId());
            staffList.remove(staffToDelete);
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Staff member '" + staffToDelete.getFullName() + "' was soft-deleted.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Operation Failed", "Could not soft-delete staff: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // --- Getters and Setters ---

    public Date getToday() {
        return new Date();
    }

    public List<Staff> getStaffList() { return staffList; }
    public Staff getNewStaff() { return newStaff; }
    public void setNewStaff(Staff newStaff) { this.newStaff = newStaff; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<Role> getRoles() { return availableRoles; }
    public Long getSelectedRoleId() { return selectedRoleId; }
    public void setSelectedRoleId(Long selectedRoleId) { this.selectedRoleId = selectedRoleId; }

    // ADDED: Getter and Setter for the global filter
    public String getGlobalFilter() { return globalFilter; }
    public void setGlobalFilter(String globalFilter) { this.globalFilter = globalFilter; }
}