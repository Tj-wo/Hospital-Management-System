package org.pahappa.exception;

// Specific exception for appointment scheduling conflicts (e.g., doctor already booked)
public class AppointmentConflictException extends ValidationException {
    public AppointmentConflictException(String message) {
        super(message);
    }

    public AppointmentConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
