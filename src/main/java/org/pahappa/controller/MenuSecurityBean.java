package org.pahappa.controller;

import org.pahappa.exception.HospitalServiceException;
import org.pahappa.model.Role;
import org.pahappa.model.User;
import org.pahappa.service.role.RoleService;
import org.pahappa.utils.PermissionType;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Named("menuSecurityBean")
@SessionScoped
public class MenuSecurityBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(MenuSecurityBean.class.getName());

    @Inject
    private LoginBean loginBean; // To get the logged-in user

    @Inject
    private RoleService roleService;

    private Set<PermissionType> userPermissions;
    private Role loggedInUserRole; // Store the logged-in user's role for quick checks

    @PostConstruct
    public void init() {
        userPermissions = new HashSet<>();
        loggedInUserRole = null;
        // Permissions are loaded when user logs in via loadUserPermissions method
    }

    /**
     * Loads the permissions for the given user's role.
     * This method should be called after a successful user login.
     * @param user The logged-in user.
     */
    public void loadUserPermissions(User user) {
        if (user != null && user.getRole() != null) {
            this.loggedInUserRole = user.getRole(); // Store the role
            userPermissions = roleService.getPermissionsForRole(user.getRole().getId());
            LOGGER.log(Level.INFO, "MenuSecurityBean: Permissions loaded for user {0}: {1}",
                    new Object[]{user.getUsername(), userPermissions});
        } else {
            userPermissions = Collections.emptySet();
            loggedInUserRole = null;
            LOGGER.log(Level.WARNING, "MenuSecurityBean: Attempted to load permissions for null user or null role.");
        }
    }

    /**
     * Checks if the currently logged-in user has a specific permission.
     * @param permissionName The name of the permission to check (e.g., "ADMIN_MANAGE_STAFF").
     * @return true if the user has the permission, false otherwise.
     */
    public boolean hasPermission(String permissionName) {
        try {
            return userPermissions.contains(PermissionType.valueOf(permissionName));
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "MenuSecurityBean: Invalid permission type requested: {0}", permissionName);
            return false;
        }
    }

    /**
     * Checks if the currently logged-in user belongs to a specific role.
     * @param roleName The name of the role to check (e.g., "Admin", "Doctor").
     * @return true if the user is in the specified role, false otherwise.
     */
    public boolean isUserInRole(String roleName) {
        if (loggedInUserRole == null) {
            return false; // No user or role loaded
        }
        return loggedInUserRole.getName().equalsIgnoreCase(roleName);
    }

    /**
     * Checks if the currently logged-in user has at least one of the specified permissions.
     * This is useful for menu items that require one of several permissions.
     * @param permissions A variable number of permission names to check.
     * @return true if the user has any of the specified permissions, false otherwise.
     */
    public boolean hasAnyPermission(String... permissions) {
        if (userPermissions == null || userPermissions.isEmpty()) {
            return false; // No permissions loaded for the user
        }
        for (String permission : permissions) {
            try {
                if (userPermissions.contains(PermissionType.valueOf(permission))) {
                    return true;
                }
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.FINE, "MenuSecurityBean: Invalid permission type in hasAnyPermission: {0}", permission);
                // Continue to check other permissions even if one is invalid
            }
        }
        return false;
    }

    // --- Getters for EL access (Optional, as discussed) ---

    /**
     * Returns the set of permissions the user has. Primarily for debugging or advanced EL use.
     * @return A Set of PermissionType enums.
     */
    public Set<PermissionType> getUserPermissions() {
        return userPermissions;
    }

    public String getLoggedInUserRoleName() {
        return loggedInUserRole != null ? loggedInUserRole.getName() : null;
    }
}