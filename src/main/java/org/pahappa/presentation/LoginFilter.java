package org.pahappa.presentation;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

///@WebFilter("*.xhtml") // Intercept every request for an .xhtml page
public class LoginFilter implements Filter {

    @Inject
    private LoginBean loginBean;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String reqURI = req.getRequestURI();

        // Check if the user is logged in
        boolean loggedIn = (loginBean != null && loginBean.isLoggedIn());

        // Define public pages that don't require login
        boolean loginRequest = reqURI.endsWith("/login.xhtml");
        boolean registrationRequest = reqURI.endsWith("/register.xhtml"); // <-- NEW
        boolean resourceRequest = reqURI.startsWith(req.getContextPath() + "/javax.faces.resource");

        // If user is logged in OR is accessing a public page, let them through
        if (loggedIn || loginRequest || registrationRequest || resourceRequest) {
            chain.doFilter(request, response);
        } else {
            // User is not logged in and is trying to access a protected page. Redirect to login.
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}