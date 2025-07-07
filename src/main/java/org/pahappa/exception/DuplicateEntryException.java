package org.pahappa.exception;

// Specific exception for unique constraint violations (e.g., duplicate email)
public class DuplicateEntryException extends ValidationException {
    public DuplicateEntryException(String message) {
        super(message);
    }

    public DuplicateEntryException(String message, Throwable cause) {
        super(message, cause);
    }
}