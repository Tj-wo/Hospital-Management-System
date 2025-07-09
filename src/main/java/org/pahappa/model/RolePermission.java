package org.pahappa.model;

import org.pahappa.utils.PermissionType;

import javax.persistence.*;

@Entity
@Table(name = "role_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "permission_type"}))
public class RolePermission extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false)
    private PermissionType permission;

    public RolePermission() {
    }

    public RolePermission(Role role, PermissionType permission) {
        this.role = role;
        this.permission = permission;
    }

    // Getters and Setters
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public PermissionType getPermission() {
        return permission;
    }

    public void setPermission(PermissionType permission) {
        this.permission = permission;
    }

    @Override
    public String toString() {
        return "RolePermission{" +
                "id=" + getId() +
                ", role=" + (role != null ? role.getName() : "N/A") +
                ", permission=" + permission +
                '}';
    }
}
