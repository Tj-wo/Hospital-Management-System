package org.pahappa.exception;

// Used when an expected resource (e.g., entity by ID) is not found
public class ResourceNotFoundException extends HospitalServiceException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
