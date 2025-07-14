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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Date;
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

    @Override
    public Role createRole(Role role, Long performedByUserId, String performedByUsername) throws HospitalServiceException {
        if (role == null) throw new ValidationException("Role cannot be null.");
        if (role.getName() == null || role.getName().trim().isEmpty()) throw new ValidationException("Role name is required.");

        if (roleDao.findByName(role.getName()) != null) {
            throw new DuplicateEntryException("Role with name '" + role.getName() + "' already exists.");
        }

        role.setDeleted(false);
        role.setDateCreated(new Date());
        role.setCreatedBy(performedByUserId);
        roleDao.save(role);
        auditService.logCreate(role, String.valueOf(performedByUserId), performedByUsername, "Role created: " + role.getName());
        return role;
    }

    @Override
    public Role updateRole(Role role, Long performedByUserId, String performedByUsername) throws HospitalServiceException {
        if (role == null || role.getId() == null) throw new ValidationException("Role and ID cannot be null for update.");

        Role originalRole = roleDao.getById(role.getId());
        if (originalRole == null) {
            throw new ResourceNotFoundException("Role not found with ID: " + role.getId());
        }

        if (!originalRole.getName().equalsIgnoreCase(role.getName()) && roleDao.findByName(role.getName()) != null) {
            throw new DuplicateEntryException("Role with name '" + role.getName() + "' already exists.");
        }

        originalRole.setName(role.getName());
        originalRole.setDescription(role.getDescription());
        originalRole.setDateUpdated(new Date());
        originalRole.setUpdatedBy(performedByUserId);

        roleDao.update(originalRole);
        auditService.logUpdate(originalRole, role, String.valueOf(performedByUserId), performedByUsername, "Role updated: " + role.getName());
        return originalRole;
    }

    @Override
    public void deleteRole(Long roleId, Long performedByUserId, String performedByUsername) throws HospitalServiceException {
        if (roleId == null) throw new ValidationException("Role ID cannot be null for deletion.");

        Role role = roleDao.getById(roleId);
        if (role == null) {
            throw new ResourceNotFoundException("Role not found with ID: " + roleId);
        }

        roleDao.delete(roleId);
        auditService.logDelete(role, String.valueOf(performedByUserId), performedByUsername, "Role soft-deleted: " + role.getName());
    }

    @Override
    public Role getRoleById(Long roleId) {
        System.out.println("DEBUG: RoleServiceImpl.getRoleById() called with ID: " + roleId);
        if (roleId == null) {
            System.out.println("DEBUG: RoleServiceImpl.getRoleById() received null ID.");
            return null;
        }
        Role role = roleDao.getById(roleId);
        System.out.println("DEBUG: RoleServiceImpl.getRoleById(" + roleId + ") returned: " + (role != null ? "Role ID: " + role.getId() + ", Name: " + role.getName() : "null"));
        return role;
    }

    @Override
    public Role getRoleByName(String roleName) {
        System.out.println("RoleService: Attempting to find role by name: " + roleName);
        Role role = roleDao.findByName(roleName);
        System.out.println("RoleService: Found role: " + (role != null ? role.getName() + " (ID: " + role.getId() + ")" : "null")); // Keep this ADDED LINE
        return role;
    }

    @Override
    public List<Role> getAllRoles() {
        List<Role> roles = roleDao.getAll();
        System.out.println("DEBUG: RoleServiceImpl.getAllRoles() returned " + roles.size() + " roles.");
        return roles;
    }

    @Override
    public void assignPermissionToRole(Long roleId, PermissionType permissionType, Long performedByUserId, String performedByUsername) throws HospitalServiceException {
        if (roleId == null || permissionType == null) throw new ValidationException("Role ID and Permission Type are required.");

        Role role = roleDao.getById(roleId);
        if (role == null) {
            throw new ResourceNotFoundException("Role not found with ID: " + roleId);
        }

        RolePermission existingPermission = rolePermissionDao.findByRoleIdAndPermissionType(roleId, permissionType);
        if (existingPermission != null) {
            if (!existingPermission.isDeleted()) {
                throw new DuplicateEntryException("Permission '" + permissionType.name() + "' already assigned to role '" + role.getName() + "'.");
            } else {
                existingPermission.setDeleted(false);
                existingPermission.setDateUpdated(new Date());
                existingPermission.setUpdatedBy(performedByUserId);
                rolePermissionDao.update(existingPermission);
                auditService.logUpdate(existingPermission, existingPermission, String.valueOf(performedByUserId), performedByUsername,
                        "Reactivated permission '" + permissionType.name() + "' for role '" + role.getName() + "'.");
                return;
            }
        }

        RolePermission rolePermission = new RolePermission(role, permissionType);
        rolePermission.setDateCreated(new Date());
        rolePermission.setCreatedBy(performedByUserId);
        rolePermissionDao.save(rolePermission);
        auditService.logCreate(rolePermission, String.valueOf(performedByUserId), performedByUsername,
                "Permission '" + permissionType.name() + "' assigned to role '" + role.getName() + "'.");
    }

    @Override
    public void revokePermissionFromRole(Long roleId, PermissionType permissionType, Long performedByUserId, String performedByUsername) throws HospitalServiceException {
        if (roleId == null || permissionType == null) throw new ValidationException("Role ID and Permission Type are required.");

        Role role = roleDao.getById(roleId);
        if (role == null) {
            throw new ResourceNotFoundException("Role not found with ID: " + roleId);
        }

        RolePermission rolePermission = rolePermissionDao.findByRoleIdAndPermissionType(roleId, permissionType);
        if (rolePermission == null || rolePermission.isDeleted()) {
            throw new ResourceNotFoundException("Permission '" + permissionType.name() + "' not found or already revoked for role '" + role.getName() + "'.");
        }

        rolePermissionDao.delete(rolePermission.getId());
        auditService.logDelete(rolePermission, String.valueOf(performedByUserId), performedByUsername,
                "Permission '" + permissionType.name() + "' revoked from role '" + role.getName() + "'.");
    }

    @Override
    public Set<PermissionType> getPermissionsForRole(Long roleId) {
        System.out.println("RoleService: Getting permissions for role ID: " + roleId);
        if (roleId == null) {
            System.out.println("RoleService: Role ID is null, returning empty set.");
            return new HashSet<>();
        }
        List<RolePermission> rolePermissions = rolePermissionDao.findByRoleId(roleId);
        System.out.println("RoleService: Found " + rolePermissions.size() + " RolePermission entities for role ID " + roleId);

        Set<PermissionType> permissions = rolePermissions.stream()
                .filter(rp -> !rp.isDeleted())
                .map(RolePermission::getPermission)
                .collect(Collectors.toSet());
        System.out.println("RoleService: Mapped permissions: " + permissions);
        return permissions;
    }
}