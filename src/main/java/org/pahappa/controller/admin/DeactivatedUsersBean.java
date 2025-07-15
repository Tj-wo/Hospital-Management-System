package org.pahappa.controller.admin;

import org.pahappa.model.User;
import org.pahappa.service.user.UserService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Named("deactivatedUsersBean")
@ViewScoped
public class DeactivatedUsersBean implements Serializable {

    @Inject
    private UserService userService;

    // No longer need to inject PatientService or StaffService here
    // private PatientService patientService;
    // private StaffService staffService;

    private List<User> allDeactivatedUsers;
    private List<User> filteredUsers;
    private String filterType = "ALL"; // Default filter

    @PostConstruct
    public void init() {
        this.allDeactivatedUsers = userService.findDeactivatedUsers();
        applyFilter();
    }

    /**
     * Filters the master list of deactivated users based on the selected filterType.
     */
    public void applyFilter() {
        if ("STAFF".equals(filterType)) {
            this.filteredUsers = this.allDeactivatedUsers.stream()
                    .filter(user -> user.getStaff() != null)
                    .collect(Collectors.toList());
        } else if ("PATIENT".equals(filterType)) {
            this.filteredUsers = this.allDeactivatedUsers.stream()
                    .filter(user -> user.getPatient() != null)
                    .collect(Collectors.toList());
        } else {
            this.filteredUsers = new ArrayList<>(this.allDeactivatedUsers);
        }
    }

    /**
     * Reactivates a selected user by making a single, clear call to the UserService.
     * @param userToReactivate The user object to be reactivated.
     */
    public void reactivateUser(User userToReactivate) {
        try {
            // The bean's only job is to call the high-level service method.
            // All complex logic is now correctly encapsulated in the service layer.
            userService.reactivateUserAndProfile(userToReactivate.getId());

            // For a snappier UI, remove the user from the in-memory lists
            allDeactivatedUsers.removeIf(user -> Objects.equals(user.getId(), userToReactivate.getId()));
            filteredUsers.removeIf(user -> Objects.equals(user.getId(), userToReactivate.getId()));

            String successMessage = String.format("User '%s' has been restored.", userToReactivate.getUsername());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", successMessage));

        } catch (Exception e) {
            String errorMessage = "Failed to restore user: " + e.getMessage();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", errorMessage));
        }
    }

    public void runLegacyCleanup() {
        try {
            int fixedCount = userService.cleanupLegacyDeletedUsers();
            if (fixedCount > 0) {
                // Reload the list to include the newly fixed users
                init();
                String successMessage = String.format("Successfully found and migrated %d previously deleted user(s). They are now visible in this list.", fixedCount);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Cleanup Complete", successMessage));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Cleanup Complete", "No legacy users found to migrate."));
            }
        } catch (Exception e) {
            String errorMessage = "Cleanup failed: " + e.getMessage();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", errorMessage));
        }
    }

    // --- Getters and Setters ---

    public List<User> getFilteredUsers() {
        return filteredUsers;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }
}