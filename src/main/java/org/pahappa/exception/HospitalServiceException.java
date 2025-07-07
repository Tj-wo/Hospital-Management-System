package org.pahappa.exception;

public class HospitalServiceException extends Exception {
    public HospitalServiceException(String message) {
        super(message);
    }

    public HospitalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}