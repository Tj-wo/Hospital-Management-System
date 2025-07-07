package org.pahappa.controller.admin;

import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
import org.pahappa.dao.PatientDao;
import org.pahappa.service.patient.PatientService;
import org.pahappa.service.staff.StaffService;
import org.pahappa.utils.Role;
import org.pahappa.exception.HospitalServiceException;
import org.pahappa.exception.ValidationException;
import org.pahappa.exception.ResourceNotFoundException;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named("deactivatedUsersBean")
@ViewScoped
public class DeactivatedUsersBean implements Serializable {

    @Inject
    private PatientDao patientDao; 
    @Inject
    private PatientService patientService; 
    @Inject
    private StaffService staffService; 

    private Integer selectedCategory; // 0 for Patients, 1 for Staff [86]
    private List currentDeactivatedList; 
    private Object selectedUser; 

    @PostConstruct
    public void init() {
        selectedCategory = null; 
        currentDeactivatedList = new ArrayList<>(); 
    }

    public void updateDeactivatedList() {
        if (selectedCategory == null) { 
            currentDeactivatedList.clear(); 
            return; 
        }

        if (selectedCategory == 0) { // Patients [87]
            currentDeactivatedList = new ArrayList<>(patientDao.getAllDeleted());
            System.out.println("Deactivated patients count: " + currentDeactivatedList.size()); 
        } else if (selectedCategory == 1) { // Staff [87]
            currentDeactivatedList = new ArrayList<>(staffService.getSoftDeletedStaff()); 
            System.out.println("Deactivated staff count: " + currentDeactivatedList.size()); 
        }
    }

    public void restoreUser() {
        if (selectedUser == null) { 
            addMessage(FacesMessage.SEVERITY_WARN, "Warning", "No user selected for restoration."); 
            return; 
        }

        try {
            if (selectedCategory == 0 && selectedUser instanceof Patient) { 
                System.out.println("Patient .." + ((Patient) selectedUser).getFullName()); 
                patientService.restorePatient(((Patient) selectedUser).getId()); 
                currentDeactivatedList.remove(selectedUser); 
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Patient restored successfully."); 
            } else if (selectedCategory == 1 && selectedUser instanceof Staff) { 
                staffService.restoreStaff(((Staff) selectedUser).getId()); 
                currentDeactivatedList.remove(selectedUser); 
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Staff member restored successfully."); 
            }
            selectedUser = null;
        } catch (ValidationException | ResourceNotFoundException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not restore user: " + e.getMessage()); 
        } catch (HospitalServiceException hse) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Operation Failed", hse.getMessage());
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support."); 
        }
    }

    public void permanentlyDeleteUser() {
        if (selectedUser == null) { 
            addMessage(FacesMessage.SEVERITY_WARN, "Warning", "No user selected for permanent deletion."); 
            return; 
        }

        try {
            if (selectedCategory == 0 && selectedUser instanceof Patient) { 
                patientService.permanentlyDeletePatient(((Patient) selectedUser).getId()); 
                currentDeactivatedList.remove(selectedUser); 
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Patient permanently deleted."); 
            } else if (selectedCategory == 1 && selectedUser instanceof Staff) { 
                staffService.permanentlyDeleteStaff(((Staff) selectedUser).getId()); 
                currentDeactivatedList.remove(selectedUser); 
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Staff member permanently deleted."); 
            }
            selectedUser = null;
        } catch (ValidationException | ResourceNotFoundException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not permanently delete user: " + e.getMessage()); 
        } catch (HospitalServiceException hse) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Operation Failed", hse.getMessage());
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred. Please contact support."); 
        }
    }

    public String getUserName() {
        if (selectedCategory == null || selectedUser == null) return ""; 
        if (selectedCategory == 0 && selectedUser instanceof Patient) { 
            return ((Patient) selectedUser).getFullName(); 
        } else if (selectedCategory == 1 && selectedUser instanceof Staff) { 
            return ((Staff) selectedUser).getFirstName() + " " + ((Staff) selectedUser).getLastName(); 
        }
        return ""; 
    }

    public String getUserEmail() {
        if (selectedUser == null) return ""; 
        if (selectedCategory == 0 && selectedUser instanceof Patient) { 
            return ((Patient) selectedUser).getEmail(); 
        } else if (selectedCategory == 1 && selectedUser instanceof Staff) { 
            return ((Staff) selectedUser).getEmail(); 
        }
        return ""; 
    }

    public String getUserDateOfBirth() {
        if (selectedUser == null) return ""; 
        if (selectedCategory == 0 && selectedUser instanceof Patient) { 
            return ((Patient) selectedUser).getDateOfBirth() != null ? 
                    new SimpleDateFormat("yyyy-MM-dd").format(((Patient) selectedUser).getDateOfBirth()) : ""; 
        }
        return ""; 
    }

    public String getUserRole() {
        if (selectedUser == null) return ""; 
        if (selectedCategory == 1 && selectedUser instanceof Staff) { 
            Role role = ((Staff) selectedUser).getRole(); 
            return role != null ? role.name() : ""; 
        }
        return ""; 
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail)); 
    }

    public Integer getSelectedCategory() { return selectedCategory; } 
    public void setSelectedCategory(Integer selectedCategory) { 
        this.selectedCategory = selectedCategory; 
        updateDeactivatedList(); 
    }
    public List getCurrentDeactivatedList() { return currentDeactivatedList; } 
    public Object getSelectedUser() { return selectedUser; } 
    public void setSelectedUser(Object selectedUser) { this.selectedUser = selectedUser; } 
}