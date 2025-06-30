package org.pahappa.controller;

import org.pahappa.model.User;
import org.pahappa.service.UserService;

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

    public String login() {
        loggedInUser = userService.login(username, password);

        if (loggedInUser != null) {
            // A successful login should redirect to the main index page.
            // This outcome is handled by faces-config.xml.
            return "index";
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Failed", "Invalid username or password."));
            // Stay on the same page by returning null.
            return null;
        }
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        loggedInUser = null;
        // Correctly return the "login" outcome to trigger navigation.
        return "login";
    }

    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    // Getters and Setters...
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public User getLoggedInUser() { return loggedInUser; }
}