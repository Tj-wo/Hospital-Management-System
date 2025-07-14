package org.pahappa.controller.admin;

import org.pahappa.exception.HospitalServiceException;
import org.pahappa.exception.ResourceNotFoundException;
import org.pahappa.exception.ValidationException;
import org.pahappa.model.Role;
import org.pahappa.model.User;
import org.pahappa.service.role.RoleService;
import org.pahappa.controller.LoginBean;

import org.primefaces.PrimeFaces;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("adminRoleBean")
@ViewScoped
public class AdminRoleBean implements Serializable {

    @Inject
    private RoleService roleService;

    @Inject
    private LoginBean loginBean;

    private List<Role> roles;
    private Role selectedRole;

    // REMOVED: Fields related to permissions are no longer needed here.
    // private Set<PermissionType> selectedPermissions;
    // private List<PermissionType> availablePermissions;

    @PostConstruct
    public void init() {
        loadRoles();
    }

    private void loadRoles() {
        roles = roleService.getAllRoles();
    }

    public void prepareNewRole() {
        selectedRole = new Role();
        // REMOVED: No need to initialize permissions here.
    }

    public void selectRoleForEdit(Role role) {
        // Now it only fetches the role, not its permissions.
        selectedRole = roleService.getRoleById(role.getId());
    }

    public void saveRole() {
        Long performedByUserId = null;
        String performedByUsername = "system";
        User currentUser = loginBean.getLoggedInUser();
        if (currentUser != null) {
            performedByUserId = currentUser.getId();
            performedByUsername = currentUser.getUsername();
        }

        try {
            // SIMPLIFIED: The save logic now only handles role creation/update.
            if (selectedRole.getId() == null) {
                roleService.createRole(selectedRole, performedByUserId, performedByUsername);
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Role '" + selectedRole.getName() + "' created successfully.");
            } else {
                roleService.updateRole(selectedRole, performedByUserId, performedByUsername);
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Role '" + selectedRole.getName() + "' updated successfully.");
            }

            // REMOVED: All permission assignment/revocation logic is gone from this method.

            loadRoles();
            PrimeFaces.current().executeScript("PF('roleDialog').hide()");

        } catch (ValidationException ve) {
            addMessage(FacesMessage.SEVERITY_WARN, "Validation Failed", ve.getMessage());
            PrimeFaces.current().ajax().update("roleDialogForm:dialogMessages");
        } catch (ResourceNotFoundException rnfe) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", rnfe.getMessage());
            PrimeFaces.current().ajax().update("roleDialogForm:dialogMessages");
        } catch (HospitalServiceException hse) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Operation Failed", hse.getMessage());
            PrimeFaces.current().ajax().update("roleDialogForm:dialogMessages");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred.");
            PrimeFaces.current().ajax().update("roleDialogForm:dialogMessages");
            e.printStackTrace();
        }
    }

    public void deleteRole(Long roleId) {
        Long performedByUserId = null;
        String performedByUsername = "system";
        User currentUser = loginBean.getLoggedInUser();
        if (currentUser != null) {
            performedByUserId = currentUser.getId();
            performedByUsername = currentUser.getUsername();
        }

        try {
            roleService.deleteRole(roleId, performedByUserId, performedByUsername);
            loadRoles();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Role deleted successfully.");
        } catch (ResourceNotFoundException rnfe) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", rnfe.getMessage());
        } catch (HospitalServiceException hse) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Operation Failed", hse.getMessage());
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred.");
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // --- Getters and Setters ---
    public List<Role> getRoles() { return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles; }
    public Role getSelectedRole() { return selectedRole; }
    public void setSelectedRole(Role selectedRole) { this.selectedRole = selectedRole; }
}