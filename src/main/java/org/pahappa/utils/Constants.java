package org.pahappa.utils;

// Constants used across the application
public class Constants {
    public static final String ERROR_INVALID_ID = "ID must be a positive number";
    public static final String ERROR_NOT_FOUND = "Record not found";
    public static final String ERROR_REQUIRED_FIELD = "This field is required";
    public static final String ERROR_INVALID_EMAIL = "Invalid email format";
    public static final String ERROR_INVALID_DATE = "Invalid date format. Use yyyy-MM-dd";
    public static final String ERROR_INVALID_TIMESTAMP = "Invalid timestamp format. Use yyyy-MM-dd HH:mm";
    public static final String ERROR_PAST_APPOINTMENT = "Appointment date cannot be in the past";
    public static final String ERROR_FUTURE_ADMISSION_DATE = "Admission date cannot be in the future";
    public static final String ERROR_DISCHARGE_BEFORE_ADMISSION = "Discharge date cannot be before admission date";
    public static final String ERROR_REASON_TOO_LONG = "Reason cannot exceed 255 characters";
    public static final String ERROR_INVALID_DOCTOR = "Selected staff must be a doctor";
    public static final String ERROR_INVALID_ROLE = "Invalid role. Must be DOCTOR, NURSE, or ADMIN";

    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_SPECIALTY_LENGTH = 100;
    public static final int MAX_REASON_LENGTH = 255;
    public static final int MAX_DIAGNOSIS_LENGTH = 500;
    public static final int MAX_TREATMENT_LENGTH = 500;

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
}