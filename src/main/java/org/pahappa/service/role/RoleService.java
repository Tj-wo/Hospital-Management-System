package org.pahappa.service.role;

import org.pahappa.exception.HospitalServiceException;
import org.pahappa.model.Role;
import org.pahappa.utils.PermissionType;

import java.util.List;
import java.util.Set;

public interface RoleService {
    Role createRole(Role role) throws HospitalServiceException;
    Role updateRole(Role role) throws HospitalServiceException;
    void deleteRole(Long roleId) throws HospitalServiceException;
    Role getRoleById(Long roleId);
    Role getRoleByName(String roleName);
    List<Role> getAllRoles();

    void assignPermissionToRole(Long roleId, PermissionType permissionType) throws HospitalServiceException;
    void revokePermissionFromRole(Long roleId, PermissionType permissionType) throws HospitalServiceException;
    Set<PermissionType> getPermissionsForRole(Long roleId);
}