package org.pahappa.filters;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("*.xhtml")
public class LoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;
            HttpSession session = req.getSession(false);

            String reqURI = req.getRequestURI();
            boolean loggedIn = session != null && session.getAttribute("user") != null;
            boolean loginRequest = reqURI.endsWith("/login.xhtml");
            boolean resourceRequest = reqURI.contains("javax.faces.resource");

            if (loggedIn || loginRequest || resourceRequest) {
                chain.doFilter(request, response);
            } else {
                res.sendRedirect(req.getContextPath() + "/login.xhtml");
            }
        } catch (Exception e) {
            System.out.println("LoginFilter error: " + e.getMessage());
        }
    }

    @Override
    public void init(FilterConfig config) throws ServletException {}

    @Override
    public void destroy() {}
}
