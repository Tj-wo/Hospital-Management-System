package org.pahappa.service.role.impl;

import org.pahappa.dao.RoleDao;
import org.pahappa.dao.RolePermissionDao;
import org.pahappa.exception.HospitalServiceException;
import org.pahappa.exception.ResourceNotFoundException;
import org.pahappa.exception.DuplicateEntryException;
import org.pahappa.exception.ValidationException;
import org.pahappa.model.Role;
import org.pahappa.model.RolePermission;
import org.pahappa.service.audit.AuditService;
import org.pahappa.service.role.RoleService;
import org.pahappa.utils.PermissionType;
import org.pahappa.controller.LoginBean; 

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoleServiceImpl implements RoleService {

    @Inject
    private RoleDao roleDao;

    @Inject
    private RolePermissionDao rolePermissionDao;

    @Inject
    private AuditService auditService;

    @Inject
    private LoginBean loginBean;

    private String getCurrentUser() {
        return (loginBean != null && loginBean.isLoggedIn()) ? loginBean.getLoggedInUser().getUsername() : "system";
    }

    private String getCurrentUserId() {
        return (loginBean != null && loginBean.isLoggedIn() && loginBean.getLoggedInUser().getId() != null) ?
                loginBean.getLoggedInUser().getId().toString() : "0";
    }

    @Override
    public Role createRole(Role role) throws HospitalServiceException {
        if (role == null) throw new ValidationException("Role cannot be null.");
        if (role.getName() == null || role.getName().trim().isEmpty()) throw new ValidationException("Role name is required.");

        if (roleDao.findByName(role.getName()) != null) {
            throw new DuplicateEntryException("Role with name '" + role.getName() + "' already exists.");
        }

        role.setDeleted(false);
        roleDao.save(role);
        auditService.logCreate(role, getCurrentUserId(), getCurrentUser(), "Role created: " + role.getName());
        return role;
    }

    @Override
    public Role updateRole(Role role) throws HospitalServiceException {
        if (role == null || role.getId() == null) throw new ValidationException("Role and ID cannot be null for update.");

        Role originalRole = roleDao.getById(role.getId());
        if (originalRole == null) {
            throw new ResourceNotFoundException("Role not found with ID: " + role.getId());
        }

        if (!originalRole.getName().equalsIgnoreCase(role.getName()) && roleDao.findByName(role.getName()) != null) {
            throw new DuplicateEntryException("Role with name '" + role.getName() + "' already exists.");
        }

        // Only update allowed fields. Permissions are managed separately.
        originalRole.setName(role.getName());
        originalRole.setDescription(role.getDescription());
        // Other fields like deleted status are managed by specific methods or BaseModel lifecycle.

        roleDao.update(originalRole);
        auditService.logUpdate(originalRole, role, getCurrentUserId(), getCurrentUser(), "Role updated: " + role.getName());
        return originalRole;
    }

    @Override
    public void deleteRole(Long roleId) throws HospitalServiceException {
        if (roleId == null) throw new ValidationException("Role ID cannot be null for deletion.");

        Role role = roleDao.getById(roleId);
        if (role == null) {
            throw new ResourceNotFoundException("Role not found with ID: " + roleId);
        }

        // Prevent deletion of pre-defined roles if any are still needed or linked implicitly
        // For example, if you want ADMIN to be undeletable:
        // if (role.getName().equalsIgnoreCase("ADMIN")) throw new ValidationException("Admin role cannot be deleted.");

        // Soft delete the role
        roleDao.delete(roleId); // BaseDao performs soft delete [126, 127]
        auditService.logDelete(role, getCurrentUserId(), getCurrentUser(), "Role soft-deleted: " + role.getName());
    }

    @Override
    public Role getRoleById(Long roleId) {
        return roleDao.getById(roleId);
    }

    @Override
    public Role getRoleByName(String roleName) {
        return roleDao.findByName(roleName);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleDao.getAll();
    }

    @Override
    public void assignPermissionToRole(Long roleId, PermissionType permissionType) throws HospitalServiceException {
        if (roleId == null || permissionType == null) throw new ValidationException("Role ID and Permission Type are required.");

        Role role = roleDao.getById(roleId);
        if (role == null) {
            throw new ResourceNotFoundException("Role not found with ID: " + roleId);
        }

        RolePermission existingPermission = rolePermissionDao.findByRoleIdAndPermissionType(roleId, permissionType);
        if (existingPermission != null) {
            throw new DuplicateEntryException("Permission '" + permissionType.name() + "' already assigned to role '" + role.getName() + "'.");
        }

        RolePermission rolePermission = new RolePermission(role, permissionType);
        role.addPermission(rolePermission); // Adds to role's collection and sets role on permission
        rolePermissionDao.save(rolePermission);
        auditService.logCreate(rolePermission, getCurrentUserId(), getCurrentUser(),
                "Permission '" + permissionType.name() + "' assigned to role '" + role.getName() + "'.");
    }

    @Override
    public void revokePermissionFromRole(Long roleId, PermissionType permissionType) throws HospitalServiceException {
        if (roleId == null || permissionType == null) throw new ValidationException("Role ID and Permission Type are required.");

        Role role = roleDao.getById(roleId);
        if (role == null) {
            throw new ResourceNotFoundException("Role not found with ID: " + roleId);
        }

        RolePermission rolePermission = rolePermissionDao.findByRoleIdAndPermissionType(roleId, permissionType);
        if (rolePermission == null) {
            throw new ResourceNotFoundException("Permission '" + permissionType.name() + "' not found for role '" + role.getName() + "'.");
        }

        role.removePermission(rolePermission); // Removes from role's collection and clears role on permission
        rolePermissionDao.delete(rolePermission.getId()); // Soft delete the join record
        auditService.logDelete(rolePermission, getCurrentUserId(), getCurrentUser(),
                "Permission '" + permissionType.name() + "' revoked from role '" + role.getName() + "'.");
    }

    @Override
    public Set<PermissionType> getPermissionsForRole(Long roleId) {
        if (roleId == null) return new HashSet<>();
        List<RolePermission> rolePermissions = rolePermissionDao.findByRoleId(roleId);
        return rolePermissions.stream()
                .map(RolePermission::getPermission)
                .collect(Collectors.toSet());
    }
}
