package org.pahappa.controller;

import org.pahappa.model.User;
import org.pahappa.service.user.UserService;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private String username;
    private String password;
    private User loggedInUser;

    @Inject
    private UserService userService;

    @Inject
    private MenuSecurityBean menuSecurityBean;

    public String login() {
        loggedInUser = userService.login(username, password);

        if (loggedInUser != null) {
            // This part is excellent. It correctly loads the user's permissions into the security bean.
            System.out.println("LoginBean: Successful login. Calling MenuSecurityBean.loadUserPermissions().");
            menuSecurityBean.loadUserPermissions(loggedInUser);

            // FIXED: Return the "dashboard" outcome to match the rule in faces-config.xml
            return "dashboard";
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Failed", "Invalid username or password."));
            return null; // Stay on the same page (login.xhtml)
        }
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        loggedInUser = null;
        System.out.println("LoginBean: User logged out. Session invalidated.");
        return "login"; // This correctly returns the "login" outcome.
    }

    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    // --- Getters and Setters ---
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public User getLoggedInUser() { return loggedInUser; }
}