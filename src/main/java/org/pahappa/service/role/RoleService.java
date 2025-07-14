package org.pahappa.service.role;

import org.pahappa.exception.HospitalServiceException;
import org.pahappa.model.Role;
import org.pahappa.utils.PermissionType;

import java.util.List;
import java.util.Set;

public interface RoleService {
    Role createRole(Role role, Long performedByUserId, String performedByUsername) throws HospitalServiceException;
    Role updateRole(Role role, Long performedByUserId, String performedByUsername) throws HospitalServiceException;
    void deleteRole(Long roleId, Long performedByUserId, String performedByUsername) throws HospitalServiceException;

    Role getRoleById(Long roleId);
    Role getRoleByName(String roleName);
    List<Role> getAllRoles();

    void assignPermissionToRole(Long roleId, PermissionType permissionType, Long performedByUserId, String performedByUsername) throws HospitalServiceException;
    void revokePermissionFromRole(Long roleId, PermissionType permissionType, Long performedByUserId, String performedByUsername) throws HospitalServiceException;

    Set<PermissionType> getPermissionsForRole(Long roleId);
}