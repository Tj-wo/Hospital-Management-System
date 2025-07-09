package org.pahappa.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role extends BaseModel { // Inherit BaseModel for id, dateCreated, etc.

    @Column(nullable = false, unique = true)
    private String name; // e.g., "Hospital Administrator", "Senior Doctor"

    @Column(columnDefinition = "TEXT")
    private String description;

    // Many-to-Many relationship with permissions via a join table
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<RolePermission> permissions = new HashSet<>();

    public Role() {
    }

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<RolePermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<RolePermission> permissions) {
        this.permissions = permissions;
    }

    // Helper methods to manage permissions
    public void addPermission(RolePermission permission) {
        this.permissions.add(permission);
        permission.setRole(this);
    }

    public void removePermission(RolePermission permission) {
        this.permissions.remove(permission);
        permission.setRole(null);
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", deleted=" + isDeleted() +
                '}';
    }
}
