package org.pahappa.controller.admin;

import org.pahappa.model.Staff;
import org.pahappa.service.staff.StaffService;
import org.pahappa.utils.Role;
import org.pahappa.exception.HospitalServiceException;
import org.pahappa.exception.ValidationException;
import org.pahappa.exception.ResourceNotFoundException;
import org.pahappa.exception.DuplicateEntryException;

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
    private List<Staff> softDeletedStaffList;
    private Staff newStaff;
    private Staff selectedStaff;
    private Staff selectedSoftDeletedStaff;
    private String password;

    @PostConstruct
    public void init() {
        staffList = staffService.getAllStaff();
        softDeletedStaffList = staffService.getSoftDeletedStaff();
        prepareNewStaff();
        System.out.println("Soft deleted staff count: " + softDeletedStaffList.size());//debug [77]
    }

    public void prepareNewStaff() {
        newStaff = new Staff();
        password = null; // This clears password only when preparing for a NEW staff entry
    }

    public void addStaff() {
        try {
            staffService.addStaff(newStaff, password);
            init(); // Re-initialize (and clear) only on SUCCESS
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Staff member '" + newStaff.getFirstName() + " " + newStaff.getLastName() + "' was added.");
            PrimeFaces.current().executeScript("PF('staffDialog').hide()");
            PrimeFaces.current().ajax().update("staffForm:messages", "staffForm:staffTable", "staffForm:softDeletedStaffTable");
        } catch (ValidationException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not add staff: " + e.getMessage());
            // Update the growl and the panel containing the inputs. The bean values are preserved.
            PrimeFaces.current().ajax().update("dialogForm:dialogMessages", "dialogForm:staffDetailsPanel");
        } catch (HospitalServiceException hse) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Operation Failed", hse.getMessage());
            PrimeFaces.current().ajax().update("dialogForm:dialogMessages");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support.");
            PrimeFaces.current().ajax().update("dialogForm:dialogMessages");
        }
    }

    public void softDeleteStaff() {
        if (selectedStaff == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Warning", "No staff member selected for soft deletion.");
            return;
        }
        try {
            staffService.softDeleteStaff(selectedStaff.getId());
            staffList.remove(selectedStaff);
            softDeletedStaffList.add(selectedStaff);
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Staff member '" + selectedStaff.getFirstName() + " " + selectedStaff.getLastName() + "' was soft-deleted.");
            selectedStaff = null;
            PrimeFaces.current().ajax().update("staffForm:messages", "staffForm:staffTable", "staffForm:staffTable", "staffForm:softDeletedStaffTable");
        } catch (ValidationException | ResourceNotFoundException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not soft-delete staff: " + e.getMessage());
        } catch (HospitalServiceException hse) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Operation Failed", hse.getMessage());
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support.");
        }
    }

    public void restoreStaff() {
        if (selectedSoftDeletedStaff == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Warning", "No staff member selected for restoration.");
            return;
        }
        try {
            staffService.restoreStaff(selectedSoftDeletedStaff.getId());
            softDeletedStaffList.remove(selectedSoftDeletedStaff);
            staffList.add(selectedSoftDeletedStaff);
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Staff member '" + selectedSoftDeletedStaff.getFirstName() + " " + selectedSoftDeletedStaff.getLastName() + "' was restored.");
            selectedSoftDeletedStaff = null;
            PrimeFaces.current().ajax().update("staffForm:messages", "staffForm:staffTable", "staffForm:softDeletedStaffTable");
        } catch (ValidationException | ResourceNotFoundException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not restore staff: " + e.getMessage());
        } catch (HospitalServiceException hse) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Operation Failed", hse.getMessage());
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support.");
        }
    }

    public void permanentlyDeleteStaff() {
        if (selectedSoftDeletedStaff == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Warning", "No staff member selected for permanent deletion.");
            return;
        }
        try {
            staffService.permanentlyDeleteStaff(selectedSoftDeletedStaff.getId());
            softDeletedStaffList.remove(selectedSoftDeletedStaff);
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Staff member '" + selectedSoftDeletedStaff.getFirstName() + " " + selectedSoftDeletedStaff.getLastName() + "' was permanently deleted.");
            selectedSoftDeletedStaff = null;
            PrimeFaces.current().ajax().update("staffForm:messages", "staffForm:softDeletedStaffTable");
        } catch (ValidationException | ResourceNotFoundException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not permanently delete staff: " + e.getMessage());
        } catch (HospitalServiceException hse) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Operation Failed", hse.getMessage());
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support.");
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public List<Staff> getStaffList() { return staffList; }
    public List<Staff> getSoftDeletedStaffList() { return softDeletedStaffList; }
    public Staff getNewStaff() { return newStaff; }
    public void setNewStaff(Staff newStaff) { this.newStaff = newStaff; }
    public Staff getSelectedStaff() { return selectedStaff; }
    public void setSelectedStaff(Staff selectedStaff) { this.selectedStaff = selectedStaff; }
    public Staff getSelectedSoftDeletedStaff() { return selectedSoftDeletedStaff; }
    public void setSelectedSoftDeletedStaff(Staff selectedSoftDeletedStaff) { this.selectedSoftDeletedStaff = selectedSoftDeletedStaff; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role[] getRoles() { return Role.values(); }
}