package org.pahappa.exception;

// Used for expected input validation failures or business rule violations
public class ValidationException extends HospitalServiceException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
