package org.pahappa.controller.admin;

import org.pahappa.model.Staff;
import org.pahappa.service.staff.StaffService;
import org.pahappa.utils.Role;
import org.primefaces.PrimeFaces;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("adminStaffBean")
@ViewScoped
public class AdminStaffBean implements Serializable {

    @Inject
    private StaffService staffService;

    private List<Staff> staffList;
    private Staff newStaff;
    private Staff selectedStaff;
    private String password;

    @PostConstruct
    public void init() {
        staffList = staffService.getAllStaff();
        prepareNewStaff();
    }

    /**
     * Prepares a fresh Staff object for the creation dialog.
     */
    public void prepareNewStaff() {
        newStaff = new Staff();
        password = null;
    }

    /**
     * Action method to save the new staff member.
     */
    public void addStaff() {
        try {
            staffService.addStaff(newStaff, password);
            init(); // Reload the staff list and prepare for a new entry

            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Staff member '" + newStaff.getFullName() + "' was added.");

            // Hide the dialog only on success
            PrimeFaces.current().executeScript("PF('staffDialog').hide()");
            PrimeFaces.current().ajax().update("staffForm:messages", "staffForm:staffTable");

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not add staff: " + e.getMessage());
            // Keep dialog open for correction
            PrimeFaces.current().ajax().update("dialogForm");
        }
    }

    /**
     * Action method to delete the currently selected staff member.
     */
    public void deleteStaff() {
        if (selectedStaff == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Warning", "No staff member selected for deletion.");
            return;
        }

        try {
            staffService.deleteStaff(selectedStaff.getId());
            staffList.remove(selectedStaff); // Optimistically update the UI

            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Staff member '" + selectedStaff.getFullName() + "' was deleted.");

            selectedStaff = null; // Clear selection
            PrimeFaces.current().ajax().update("staffForm:messages", "staffForm:staffTable");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not delete staff: " + e.getMessage());
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }


    // --- Getters and Setters ---
    public List<Staff> getStaffList() { return staffList; }
    public void setStaffList(List<Staff> staffList) { this.staffList = staffList; }
    public Staff getNewStaff() { return newStaff; }
    public void setNewStaff(Staff newStaff) { this.newStaff = newStaff; }
    public Staff getSelectedStaff() { return selectedStaff; }
    public void setSelectedStaff(Staff selectedStaff) { this.selectedStaff = selectedStaff; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role[] getRoles() { return Role.values(); }
}