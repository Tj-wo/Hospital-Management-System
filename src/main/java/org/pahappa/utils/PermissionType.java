package org.pahappa.utils;

public enum PermissionType {
    // --- NEW CATEGORY-BASED ORGANIZATION ---

    // Category: General & Dashboard
    VIEW_DASHBOARD("View Dashboard", "General"),
    BOOK_APPOINTMENT_PATIENT("Book an Appointment", "General"),
    VIEW_DASHBOARD_ADMIN_STATS("View Admin Dashboard Stats", "General"),
    VIEW_DASHBOARD_DOCTOR_STATS("View Doctor Dashboard Stats", "General"),
    VIEW_DASHBOARD_PATIENT_STATS("View Patient Dashboard Stats", "General"),
    VIEW_DASHBOARD_NURSE_STATS("View Nurse Dashboard Stats", "General"),
    VIEW_DASHBOARD_RECEPTIONIST_STATS("View Receptionist Dashboard Stats", "General"),

    // Category: Staff Management
    VIEW_STAFF_MANAGEMENT("View Staff Management Menu", "Staff Management"),
    ADMIN_MANAGE_STAFF("Admin: Manage Staff", "Staff Management"),
    VIEW_STAFF_COUNTS("View Staff Counts", "Staff Management"),

    // Category: Patient Management
    VIEW_PATIENT_MANAGEMENT("View Patient Management Menu", "Patient Management"),
    ADMIN_MANAGE_PATIENTS("Admin: Manage Patients", "Patient Management"),
    VIEW_RECEPTIONIST_REGISTER_PATIENT("View Register Patient Menu", "Patient Management"),
    REGISTER_PATIENT("Register Patient", "Patient Management"),
    RECEPTIONIST_REGISTER_PATIENT("Receptionist: Register Patient", "Patient Management"),

    // Category: Appointment Management
    VIEW_APPOINTMENT_MANAGEMENT("View Appointment Management Menu", "Appointment Management"),
    ADMIN_MANAGE_APPOINTMENTS("Admin: Manage Appointments", "Appointment Management"),
    VIEW_DOCTOR_APPOINTMENTS("View Doctor Appointments Menu", "Appointment Management"),
    DOCTOR_VIEW_OWN_APPOINTMENTS("Doctor: View Own Appointments", "Appointment Management"),
    DOCTOR_UPDATE_APPOINTMENT_STATUS("Doctor: Update Appointment Status", "Appointment Management"),
    VIEW_RECEPTIONIST_APPOINTMENTS("View Receptionist Appointments Menu", "Appointment Management"),
    RECEPTIONIST_MANAGE_APPOINTMENTS("Receptionist: Manage Appointments", "Appointment Management"),
    VIEW_PATIENT_APPOINTMENTS("View Patient Appointments Menu", "Appointment Management"),
    PATIENT_MANAGE_OWN_APPOINTMENTS("Patient: Manage Own Appointments", "Appointment Management"),

    // Category: Admission Management
    VIEW_ADMISSION_MANAGEMENT("View Admission Management Menu", "Admission Management"),
    ADMIN_VIEW_ALL_ADMISSIONS("Admin: View All Admissions", "Admission Management"),
    VIEW_DOCTOR_ADMISSIONS("View Doctor Admissions Menu", "Admission Management"),
    DOCTOR_MANAGE_ADMISSIONS("Doctor: Manage Admissions", "Admission Management"),
    VIEW_NURSE_ADMISSIONS("View Nurse Admissions Menu", "Admission Management"),
    NURSE_VIEW_OWN_ADMISSIONS("Nurse: View Own Admissions", "Admission Management"),
    VIEW_PATIENT_ADMISSIONS("View Patient Admissions Menu", "Admission Management"),
    PATIENT_VIEW_OWN_ADMISSIONS("Patient: View Own Admissions", "Admission Management"),

    // Category: Medical Records
    VIEW_MEDICAL_RECORDS_ADMIN("View Admin Medical Records Menu", "Medical Records"),
    ADMIN_VIEW_ALL_MEDICAL_RECORDS("Admin: View All Medical Records", "Medical Records"),
    VIEW_DOCTOR_MEDICAL_RECORDS("View Doctor Medical Records Menu", "Medical Records"),
    DOCTOR_MANAGE_PATIENT_MEDICAL_RECORDS("Doctor: Manage Patient Medical Records", "Medical Records"),
    VIEW_NURSE_UPDATE_RECORDS("View Nurse Update Records Menu", "Medical Records"),
    NURSE_UPDATE_OWN_PATIENT_RECORDS("Nurse: Update Own Patient Records", "Medical Records"),
    VIEW_PATIENT_MEDICAL_RECORDS("View Patient Medical Records Menu", "Medical Records"),
    PATIENT_VIEW_OWN_MEDICAL_RECORDS("Patient: View Own Medical Records", "Medical Records"),

    // Category: System & Security
    VIEW_DEACTIVATED_USERS("View Deactivated Users Menu", "System & Security"),
    ADMIN_MANAGE_DEACTIVATED_USERS("Admin: Manage Deactivated Users", "System & Security"),
    VIEW_AUDIT_LOGS("View Audit Logs Menu", "System & Security"),
    ADMIN_VIEW_AUDIT_LOGS("Admin: View Audit Logs", "System & Security"),
    VIEW_ROLES_PERMISSIONS("View Roles & Permissions Menu", "System & Security"),
    MANAGE_ROLES_PERMISSIONS("Manage Roles & Permissions", "System & Security");

    private final String displayName;
    private final String category;

    PermissionType(String displayName, String category) {
        this.displayName = displayName;
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategory() {
        return category;
    }
}