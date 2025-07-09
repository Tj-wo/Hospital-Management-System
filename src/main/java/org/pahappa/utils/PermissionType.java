package org.pahappa.utils;

public enum PermissionType {
    // Admin Permissions
    ADMIN_VIEW_ALL_ADMISSIONS,          // Managed by AdminAdmissionBean 
    ADMIN_MANAGE_APPOINTMENTS,          // Managed by AdminAppointmentBean (schedule, update, cancel) 
    ADMIN_VIEW_ALL_MEDICAL_RECORDS,     // Managed by AdminMedicalRecordBean
    ADMIN_MANAGE_PATIENTS,              // Managed by AdminPatientBean (soft delete, restore, perm. delete)  DeactivatedUsersBean 
    ADMIN_MANAGE_STAFF,                 // Managed by AdminStaffBean (add, soft delete, restore, perm. delete) 
    ADMIN_VIEW_AUDIT_LOGS,              // Managed by AuditLogBean 
    ADMIN_MANAGE_DEACTIVATED_USERS,     // Managed by DeactivatedUsersBean

    // Doctor Permissions
    DOCTOR_VIEW_OWN_APPOINTMENTS,       // Managed by DoctorAppointmentBean 
    DOCTOR_UPDATE_APPOINTMENT_STATUS,   // Managed by DoctorAppointmentBean 
    DOCTOR_MANAGE_PATIENT_MEDICAL_RECORDS, // Managed by DoctorMedicalRecordBean (add, view records for any patient)
    DOCTOR_MANAGE_ADMISSIONS,           // Managed by DoctorAdmissionBean (admit, discharge) [97-103]

    // Nurse Permissions
    NURSE_VIEW_OWN_ADMISSIONS,          // Managed by NurseAdmissionBean 
    NURSE_UPDATE_OWN_PATIENT_RECORDS,   // Managed by NurseRecordBean (update records for assigned patients) 

    // Receptionist Permissions
    RECEPTIONIST_REGISTER_PATIENT,      // Managed by ReceptionistPatientBean 
    RECEPTIONIST_MANAGE_APPOINTMENTS,   // Managed by ReceptionistAppointmentBean (schedule, update, cancel all appointments) 

    // Patient Permissions
    PATIENT_VIEW_OWN_ADMISSIONS,        // Managed by PatientAdmissionBean 
    PATIENT_MANAGE_OWN_APPOINTMENTS,    // Managed by PatientAppointmentBean (book, update, cancel own) 
    PATIENT_VIEW_OWN_MEDICAL_RECORDS;   // Managed by PatientMedicalRecordBean
}