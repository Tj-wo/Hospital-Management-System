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

    private List<RolePermissionRow> permissionRows;
    private List<Map.Entry<String, List<PermissionType>>> groupedPermissionsList;

    /**
     * A helper class to represent a single row in the permission matrix,
     * holding a map of permissions to their boolean selected state.
     */
    public static class RolePermissionRow implements Serializable {
        private Role role;
        private Map<PermissionType, Boolean> permissions;

        public RolePermissionRow(Role role, Map<PermissionType, Boolean> permissions) {
            this.role = role;
            this.permissions = permissions;
        }

        public Role getRole() { return role; }
        public Map<PermissionType, Boolean> getPermissions() { return permissions; }
        public void setPermissions(Map<PermissionType, Boolean> permissions) { this.permissions = permissions; }
    }

    @PostConstruct
    public void init() {
        permissionRows = new ArrayList<>();
        loadPermissions();
        groupPermissionsForDisplay();
    }

    private void loadPermissions() {
        for (Role role : roleService.getAllRoles()) {
            Set<PermissionType> currentPermissions = roleService.getPermissionsForRole(role.getId());
            Map<PermissionType, Boolean> permissionMap = new LinkedHashMap<>();

            // Initialize the map for every possible permission
            for (PermissionType perm : PermissionType.values()) {
                permissionMap.put(perm, currentPermissions.contains(perm));
            }

            permissionRows.add(new RolePermissionRow(role, permissionMap));
        }
    }


    private void groupPermissionsForDisplay() {
        Map<String, List<PermissionType>> grouped = Arrays.stream(PermissionType.values())
                .sorted(Comparator.comparing(PermissionType::getDisplayName)) // Sort alphabetically within groups
                .collect(Collectors.groupingBy(
                        PermissionType::getCategory, // Group by the new 'category' field
                        LinkedHashMap::new,          // Use a LinkedHashMap to preserve insertion order of categories
                        Collectors.toList()          // Collect permissions into a list for each category
                ));
        groupedPermissionsList = new ArrayList<>(grouped.entrySet());
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
            for (RolePermissionRow row : permissionRows) {
                Long roleId = row.getRole().getId();
                Set<PermissionType> originalPermissions = roleService.getPermissionsForRole(roleId);

                // Check every permission in the map submitted from the view
                for (Map.Entry<PermissionType, Boolean> submittedPerm : row.getPermissions().entrySet()) {
                    PermissionType perm = submittedPerm.getKey();
                    boolean isSelected = submittedPerm.getValue();
                    boolean wasSelected = originalPermissions.contains(perm);

                    if (isSelected && !wasSelected) {
                        roleService.assignPermissionToRole(roleId, perm, performedByUserId, performedByUsername);
                    } else if (!isSelected && wasSelected) {
                        roleService.revokePermissionFromRole(roleId, perm, performedByUserId, performedByUsername);
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
    public List<RolePermissionRow> getPermissionRows() { return permissionRows; }
    public List<Map.Entry<String, List<PermissionType>>> getGroupedPermissionsList() { return groupedPermissionsList; }
}