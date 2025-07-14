package org.pahappa.filter;

import org.pahappa.controller.LoginBean;
import org.pahappa.controller.MenuSecurityBean;
import org.pahappa.utils.PermissionType;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

// FIXED: The filter now correctly watches the /views/ directory
@WebFilter(urlPatterns = {"/views/*", "/dashboard.xhtml"})
public class AuthorizationFilter implements Filter {

    @Inject
    private LoginBean loginBean;

    @Inject
    private MenuSecurityBean menuSecurityBean;

    private static final Map<String, PermissionType[]> PAGE_PERMISSIONS = new HashMap<>();

    static {
        // FIXED: All paths now correctly point to the /views/ directory.

        // --- Admin Pages ---
        PAGE_PERMISSIONS.put("/views/admin/manage-staff.xhtml", new PermissionType[]{PermissionType.VIEW_STAFF_MANAGEMENT});
        PAGE_PERMISSIONS.put("/views/admin/manage-patients.xhtml", new PermissionType[]{PermissionType.VIEW_PATIENT_MANAGEMENT});
        PAGE_PERMISSIONS.put("/views/admin/manage-appointments.xhtml", new PermissionType[]{PermissionType.VIEW_APPOINTMENT_MANAGEMENT});
        PAGE_PERMISSIONS.put("/views/admin/manage-admissions.xhtml", new PermissionType[]{PermissionType.VIEW_ADMISSION_MANAGEMENT});
        PAGE_PERMISSIONS.put("/views/admin/manage-medical-records.xhtml", new PermissionType[]{PermissionType.VIEW_MEDICAL_RECORDS_ADMIN});
        PAGE_PERMISSIONS.put("/views/admin/deactivatedUsers.xhtml", new PermissionType[]{PermissionType.VIEW_DEACTIVATED_USERS});
        PAGE_PERMISSIONS.put("/views/admin/audit-logs.xhtml", new PermissionType[]{PermissionType.VIEW_AUDIT_LOGS});
        PAGE_PERMISSIONS.put("/views/admin/manage-roles.xhtml", new PermissionType[]{PermissionType.MANAGE_ROLES_PERMISSIONS});
        PAGE_PERMISSIONS.put("/views/admin/manage-permissions.xhtml", new PermissionType[]{PermissionType.MANAGE_ROLES_PERMISSIONS});

        // --- Doctor Pages ---
        PAGE_PERMISSIONS.put("/views/doctor/appointments.xhtml", new PermissionType[]{PermissionType.VIEW_DOCTOR_APPOINTMENTS});
        PAGE_PERMISSIONS.put("/views/doctor/medical-records.xhtml", new PermissionType[]{PermissionType.VIEW_DOCTOR_MEDICAL_RECORDS});
        PAGE_PERMISSIONS.put("/views/doctor/admissions.xhtml", new PermissionType[]{PermissionType.VIEW_DOCTOR_ADMISSIONS});

        // --- Nurse Pages ---
        PAGE_PERMISSIONS.put("/views/nurse/admissions.xhtml", new PermissionType[]{PermissionType.VIEW_NURSE_ADMISSIONS});
        PAGE_PERMISSIONS.put("/views/nurse/update-records.xhtml", new PermissionType[]{PermissionType.VIEW_NURSE_UPDATE_RECORDS});

        // --- Receptionist Pages ---
        PAGE_PERMISSIONS.put("/views/receptionist/register-patient.xhtml", new PermissionType[]{PermissionType.VIEW_RECEPTIONIST_REGISTER_PATIENT});
        PAGE_PERMISSIONS.put("/views/receptionist/appointments.xhtml", new PermissionType[]{PermissionType.VIEW_RECEPTIONIST_APPOINTMENTS});

        // --- Patient Pages ---
        PAGE_PERMISSIONS.put("/views/patient/appointments.xhtml", new PermissionType[]{PermissionType.VIEW_PATIENT_APPOINTMENTS});
        PAGE_PERMISSIONS.put("/views/patient/medical-records.xhtml", new PermissionType[]{PermissionType.VIEW_PATIENT_MEDICAL_RECORDS});
        PAGE_PERMISSIONS.put("/views/patient/admissions.xhtml", new PermissionType[]{PermissionType.VIEW_PATIENT_ADMISSIONS});
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String pathWithoutContext = requestURI.substring(contextPath.length());

        if (pathWithoutContext.startsWith("/javax.faces.resource")) {
            chain.doFilter(request, response);
            return;
        }

        boolean loggedIn = (loginBean != null && loginBean.isLoggedIn());
        Set<String> publicPages = new HashSet<>(Arrays.asList("/login.xhtml", "/access-denied.xhtml"));

        if (publicPages.contains(pathWithoutContext)) {
            if (loggedIn && pathWithoutContext.equals("/login.xhtml")) {
                res.sendRedirect(contextPath + "/dashboard.xhtml");
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        if (!loggedIn) {
            res.sendRedirect(contextPath + "/login.xhtml");
            return;
        }

        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setDateHeader("Expires", 0);

        if (pathWithoutContext.equals("/dashboard.xhtml")) {
            chain.doFilter(request, response);
            return;
        }

        PermissionType[] requiredPermissions = PAGE_PERMISSIONS.get(pathWithoutContext);

        if (requiredPermissions != null && requiredPermissions.length > 0) {
            if (menuSecurityBean.hasAnyPermission(
                    Arrays.stream(requiredPermissions)
                            .map(PermissionType::name)
                            .toArray(String[]::new)
            )) {
                chain.doFilter(request, response);
            } else {
                res.sendRedirect(contextPath + "/access-denied.xhtml");
            }
        } else {
            System.out.println("AuthorizationFilter: Access Denied. No permissions defined for protected page: " + pathWithoutContext);
            res.sendRedirect(contextPath + "/access-denied.xhtml");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}