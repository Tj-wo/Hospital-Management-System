package org.pahappa.utils;

public enum Role {
    DOCTOR,
    NURSE,
    ADMIN;

    // Check if a given string is a valid role
    public static boolean isValidRole(String role) {
        if (role == null) {
            return false;
        }
        try {
            Role.valueOf(role.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Convert a string to a Role enum
    public static Role fromString(String role) {
        if (role == null) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_ROLE);
        }
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_ROLE);
        }
    }
}