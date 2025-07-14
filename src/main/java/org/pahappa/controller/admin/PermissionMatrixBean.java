package org.pahappa.controller.admin;

import org.pahappa.exception.HospitalServiceException;
import org.pahappa.model.Role;
import org.pahappa.model.User;
import org.pahappa.service.role.RoleService;
import org.pahappa.utils.PermissionType;
import org.pahappa.controller.LoginBean;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Named("permissionMatrixBean")
@ViewScoped
public class PermissionMatrixBean implements Serializable {

    @Inject
    private RoleService roleService;

    @Inject
    private LoginBean loginBean;

    private List<Role> allRoles;
    private Map<Long, Set<PermissionType>> permissionsMap; // Key: Role ID, Value: Set of assigned permissions

    // NEW: A map to hold permissions grouped by their category for the UI
    private Map<String, List<PermissionType>> groupedPermissions;

    @PostConstruct
    public void init() {
        allRoles = roleService.getAllRoles();
        loadPermissions();
        groupPermissionsForDisplay();
    }

    private void loadPermissions() {
        permissionsMap = new HashMap<>();
        for (Role role : allRoles) {
            permissionsMap.put(role.getId(), roleService.getPermissionsForRole(role.getId()));
        }
    }

    /**
     * NEW: This method groups the flat list of all permissions into a map
     * structured by category, which is perfect for the new UI.
     */
    private void groupPermissionsForDisplay() {
        groupedPermissions = Arrays.stream(PermissionType.values())
                .sorted(Comparator.comparing(PermissionType::getDisplayName)) // Sort alphabetically within groups
                .collect(Collectors.groupingBy(
                        PermissionType::getCategory, // Group by the new 'category' field
                        LinkedHashMap::new,          // Use a LinkedHashMap to preserve insertion order of categories
                        Collectors.toList()          // Collect permissions into a list for each category
                ));
    }

    public void togglePermission(Role role, PermissionType permission) {
        if (role == null || permission == null) {
            return;
        }
        Set<PermissionType> currentPermissions = permissionsMap.get(role.getId());
        if (currentPermissions != null) {
            if (currentPermissions.contains(permission)) {
                currentPermissions.remove(permission);
            } else {
                currentPermissions.add(permission);
            }
        }
    }

    public void savePermissions() {
        User currentUser = loginBean.getLoggedInUser();
        if (currentUser == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "You must be logged in to perform this action.");
            return;
        }
        Long performedByUserId = currentUser.getId();
        String performedByUsername = currentUser.getUsername();

        try {
            for (Role role : allRoles) {
                Set<PermissionType> originalPermissions = roleService.getPermissionsForRole(role.getId());
                Set<PermissionType> newPermissions = permissionsMap.get(role.getId());

                // Grant new permissions
                for (PermissionType perm : newPermissions) {
                    if (!originalPermissions.contains(perm)) {
                        roleService.assignPermissionToRole(role.getId(), perm, performedByUserId, performedByUsername);
                    }
                }

                // Revoke old permissions
                for (PermissionType perm : originalPermissions) {
                    if (!newPermissions.contains(perm)) {
                        roleService.revokePermissionFromRole(role.getId(), perm, performedByUserId, performedByUsername);
                    }
                }
            }
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Permissions updated successfully.");
        } catch (HospitalServiceException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Save Failed", "An error occurred while saving permissions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // --- Getters ---
    public List<Role> getAllRoles() { return allRoles; }
    public Map<Long, Set<PermissionType>> getPermissionsMap() { return permissionsMap; }
    public Map<String, List<PermissionType>> getGroupedPermissions() { return groupedPermissions; }
}