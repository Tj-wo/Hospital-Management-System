/*package org.pahappa.controller;



import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.dao.UserDao;
import org.pahappa.dao.StaffDao;
import org.pahappa.dao.PatientDao;
import org.pahappa.dao.AppointmentDao;
import org.pahappa.dao.MedicalRecordDao;
import org.pahappa.dao.AdmissionDao;
import org.pahappa.dao.RoleDao;         // Directly import RoleDao
import org.pahappa.dao.RolePermissionDao; // Directly import RolePermissionDao

import org.pahappa.model.User;
import org.pahappa.model.Role;
import org.pahappa.model.Staff;
import org.pahappa.model.Patient;
import org.pahappa.model.Appointment;
import org.pahappa.model.MedicalRecord;
import org.pahappa.model.Admission;
import org.pahappa.model.RolePermission; // Directly import RolePermission

import org.pahappa.service.role.RoleService; // Still inject roleService for permission management
import org.pahappa.exception.HospitalServiceException;
import org.pahappa.utils.PermissionType;
import org.pahappa.utils.AppointmentStatus;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date; // Ensure java.util.Date is used consistently
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class DataInitializerBean {

    @Inject
    private RoleService roleService; // For permission logic which is likely in service

    // Direct DAO Injections
    @Inject
    UserDao userDao;
    @Inject
    StaffDao staffDao;
    @Inject
    PatientDao patientDao;
    @Inject
    AppointmentDao appointmentDao;
    @Inject
    MedicalRecordDao medicalRecordDao;
    @Inject
    AdmissionDao admissionDao;
    @Inject // Inject RoleDao directly
    RoleDao roleDao;
    @Inject // Inject RolePermissionDao directly
    RolePermissionDao rolePermissionDao;


    private static final Long SYSTEM_USER_ID = null;
    private static final String SYSTEM_USERNAME = "system";

    private final Random random = new Random();

    // Arrays for generating dummy data
    private final String[] FIRST_NAMES = {"Alice", "Bob", "Charlie", "Diana", "Edward", "Fiona", "George", "Hannah", "Ian", "Jane", "Kevin", "Laura", "Mark", "Nancy", "Oscar", "Penny", "Quinn", "Rachel", "Steve", "Tina", "Ursula", "Victor", "Wendy", "Xavier", "Yara", "Zoe"};
    private final String[] LAST_NAMES = {"Smith", "Jones", "Williams", "Brown", "Davis", "Miller", "Wilson", "Moore", "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Garcia", "Martinez", "Robinson", "Clark", "Lewis", "Lee", "Hall", "Allen", "Young", "Hernandez"};
    private final String[] SPECIALTIES = {"General Medicine", "Pediatrics", "Cardiology", "Dermatology", "Orthopedics", "Neurology", "Oncology", "Internal Medicine", "Surgery", "Psychiatry"};
    private final String[] APPOINTMENT_REASONS = {"Routine Checkup", "Follow-up", "Acute Illness", "Chronic Condition Management", "Vaccination", "Consultation"};
    private final String[] DIAGNOSES = {"Common Cold", "Influenza", "Hypertension", "Diabetes Type 2", "Asthma", "Allergies", "Migraine", "Gastroenteritis"};
    private final String[] TREATMENTS = {"Rest and fluids", "Prescription medication", "Lifestyle changes", "Referral to specialist", "Physical therapy", "Surgery preparation"};
    private final String[] ADMISSION_REASONS = {"Emergency", "Surgery", "Observation", "Rehabilitation", "Acute Care"};
    private final String[] WARDS = {"General Ward", "ICU", "Pediatric Ward", "Maternity Ward", "Surgical Ward", "Oncology Ward"};
    private final String[] WARD_NUMBERS = {"A1", "A2", "B1", "B2", "C1", "C2", "D1", "D2"};


    public void onStartup(@Observes @Initialized(ApplicationScoped.class) Object useless) {
        System.out.println("******************************************");
        System.out.println("***** APPLICATION STARTUP - SEEDING DATA *****");
        System.out.println("******************************************");

        try {
            // 1. Create Roles (if they don't exist)
            Role adminRole = findOrCreateRole("ADMIN", "System Administrator");
            Role doctorRole = findOrCreateRole("DOCTOR", "Medical Doctor");
            Role nurseRole = findOrCreateRole("NURSE", "Registered Nurse");
            Role receptionistRole = findOrCreateRole("RECEPTIONIST", "Hospital Receptionist");
            Role patientRole = findOrCreateRole("PATIENT", "Registered Patient");

            // --- 2. Assign Permissions to Roles ---
            assignPermissionsIfRoleExists(adminRole, Arrays.asList(
                    PermissionType.VIEW_STAFF_MANAGEMENT, PermissionType.VIEW_PATIENT_MANAGEMENT,
                    PermissionType.VIEW_APPOINTMENT_MANAGEMENT, PermissionType.VIEW_ADMISSION_MANAGEMENT,
                    PermissionType.VIEW_MEDICAL_RECORDS_ADMIN, PermissionType.VIEW_DEACTIVATED_USERS,
                    PermissionType.VIEW_AUDIT_LOGS, PermissionType.VIEW_DOCTOR_APPOINTMENTS,
                    PermissionType.VIEW_DOCTOR_MEDICAL_RECORDS, PermissionType.VIEW_DOCTOR_ADMISSIONS,
                    PermissionType.VIEW_NURSE_ADMISSIONS, PermissionType.VIEW_NURSE_UPDATE_RECORDS,
                    PermissionType.REGISTER_PATIENT, PermissionType.VIEW_RECEPTIONIST_APPOINTMENTS,
                    PermissionType.VIEW_PATIENT_APPOINTMENTS, PermissionType.VIEW_PATIENT_MEDICAL_RECORDS,
                    PermissionType.VIEW_PATIENT_ADMISSIONS, PermissionType.ADMIN_VIEW_ALL_ADMISSIONS,
                    PermissionType.ADMIN_MANAGE_APPOINTMENTS, PermissionType.ADMIN_MANAGE_PATIENTS,
                    PermissionType.ADMIN_MANAGE_STAFF, PermissionType.ADMIN_MANAGE_DEACTIVATED_USERS,
                    PermissionType.MANAGE_ROLES_PERMISSIONS, PermissionType.ADMIN_VIEW_AUDIT_LOGS,
                    PermissionType.ADMIN_VIEW_ALL_MEDICAL_RECORDS, PermissionType.VIEW_DASHBOARD,
                    PermissionType.VIEW_DASHBOARD_ADMIN_STATS, PermissionType.VIEW_DASHBOARD_DOCTOR_STATS,
                    PermissionType.VIEW_DASHBOARD_NURSE_STATS, PermissionType.VIEW_DASHBOARD_PATIENT_STATS,
                    PermissionType.VIEW_DASHBOARD_RECEPTIONIST_STATS, PermissionType.VIEW_STAFF_COUNTS
            ));

            assignPermissionsIfRoleExists(doctorRole, Arrays.asList(
                    PermissionType.VIEW_DOCTOR_APPOINTMENTS, PermissionType.VIEW_DOCTOR_MEDICAL_RECORDS,
                    PermissionType.VIEW_DOCTOR_ADMISSIONS
            ));

            assignPermissionsIfRoleExists(nurseRole, Arrays.asList(
                    PermissionType.VIEW_NURSE_ADMISSIONS, PermissionType.VIEW_NURSE_UPDATE_RECORDS
            ));

            assignPermissionsIfRoleExists(receptionistRole, Arrays.asList(
                    PermissionType.REGISTER_PATIENT, PermissionType.VIEW_RECEPTIONIST_APPOINTMENTS
            ));

            assignPermissionsIfRoleExists(patientRole, Arrays.asList(
                    PermissionType.VIEW_PATIENT_APPOINTMENTS, PermissionType.VIEW_PATIENT_MEDICAL_RECORDS,
                    PermissionType.VIEW_PATIENT_ADMISSIONS
            ));


            // --- 3. Create Default Users (Admin, Doctor, Nurse, Receptionist, Patient) ---
            // For primary users, use specific emails/passwords
            User adminUser = findOrCreateStaffUser("admin@email.com", "admin123", "System", "Administrator", "ADMIN", null, true);
            User doctorUser = findOrCreateStaffUser("doctor@email.com", "doctor123", "Alice", "Smith", "DOCTOR", "General Medicine", true);
            User nurseUser = findOrCreateStaffUser("nurse@email.com", "nurse123", "Brenda", "Jones", "NURSE", null, true);
            User receptionistUser = findOrCreateStaffUser("receptionist@email.com", "receptionist123", "Carol", "White", "RECEPTIONIST", null, true);
            User patientUser = findOrCreatePatientUser("patient@email.com", "patient123", "David", "Brown", true);

            // --- 4. Create additional dummy staff and patients ---
            System.out.println("\nSeeding additional dummy data...");

            // Create 5 more Doctors
            for (int i = 0; i < 5; i++) {
                String fname = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                String lname = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                findOrCreateStaffUser(fname, lname, "DOCTOR", SPECIALTIES[random.nextInt(SPECIALTIES.length)], false);
            }

            // Create 5 more Nurses
            for (int i = 0; i < 5; i++) {
                String fname = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                String lname = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                findOrCreateStaffUser(fname, lname, "NURSE", null, false);
            }

            // Create 5 more Receptionists
            for (int i = 0; i < 5; i++) {
                String fname = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                String lname = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                findOrCreateStaffUser(fname, lname, "RECEPTIONIST", null, false);
            }

            // Create 20 more Patients
            for (int i = 0; i < 20; i++) {
                String fname = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                String lname = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                findOrCreatePatientUser(fname, lname, false);
            }

            // *************************************************************************
            // *** CRITICAL SECTION: Adhering to "NO DAO CHANGES" for Staff lookup ***
            // *************************************************************************

            // To get Staff by RoleName *without* custom methods in StaffDao or BaseDao:
            // We must retrieve ALL Staff and filter them in memory based on their associated User's role.
            // This is the most complex part given the constraint.

            Role doctorRoleObj = roleDao.findByName("DOCTOR"); // Use directly injected roleDao
            List<Staff> doctors = staffDao.getAll().stream() // Get all staff using getAll() from BaseDao
                    .filter(staff -> {
                        // Find the user associated with this staff, then check their role.
                        // This is still an N+1 like query in memory if not eagerly fetched.
                        // Assumes User.staff field is correctly mapped for lookup.
                        // If Staff directly had a 'role' field, this would be simpler: staff.getRole().equals(doctorRoleObj)
                        User associatedUser = userDao.getAll().stream() // Get all users using getAll() from BaseDao
                                .filter(user -> user.getStaff() != null && user.getStaff().getId().equals(staff.getId()))
                                .findFirst()
                                .orElse(null);
                        return associatedUser != null && associatedUser.getRole() != null && associatedUser.getRole().equals(doctorRoleObj);
                    })
                    .collect(Collectors.toList());


            Role nurseRoleObj = roleDao.findByName("NURSE"); // Use directly injected roleDao
            List<Staff> nurses = staffDao.getAll().stream() // Get all staff using getAll() from BaseDao
                    .filter(staff -> {
                        // Same logic for nurses
                        User associatedUser = userDao.getAll().stream() // Get all users using getAll() from BaseDao
                                .filter(user -> user.getStaff() != null && user.getStaff().getId().equals(staff.getId()))
                                .findFirst()
                                .orElse(null);
                        return associatedUser != null && associatedUser.getRole() != null && associatedUser.getRole().equals(nurseRoleObj);
                    })
                    .collect(Collectors.toList());

            // FIX: Use patientDao.getAll() as findAll() does not exist in PatientDao
            List<Patient> patients = patientDao.getAll();

            if (!doctors.isEmpty() && !patients.isEmpty()) {
                // Generate 50 Appointments
                for (int i = 0; i < 50; i++) {
                    Staff doctor = doctors.get(random.nextInt(doctors.size()));
                    Patient patient = patients.get(random.nextInt(patients.size()));
                    createSampleAppointment(doctor, patient);
                }
            } else {
                System.out.println("Not enough doctors or patients to seed appointments.");
            }

            if (!doctors.isEmpty() && !patients.isEmpty()) {
                // Generate 30 Medical Records
                for (int i = 0; i < 30; i++) {
                    Staff doctor = doctors.get(random.nextInt(doctors.size()));
                    Patient patient = patients.get(random.nextInt(patients.size()));
                    createSampleMedicalRecord(doctor, patient);
                }
            } else {
                System.out.println("Not enough doctors or patients to seed medical records.");
            }

            if (!nurses.isEmpty() && !patients.isEmpty()) {
                // Generate 15 Admissions
                for (int i = 0; i < 15; i++) {
                    Staff nurse = nurses.get(random.nextInt(nurses.size()));
                    Patient patient = patients.get(random.nextInt(patients.size()));
                    createSampleAdmission(nurse, patient);
                }
            } else {
                System.out.println("Not enough nurses or patients to seed admissions.");
            }


            System.out.println("\n***** Data Seeding Complete! *****");

        } catch (HospitalServiceException e) {
            System.err.println("Failed to seed default data during startup: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for debugging
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during data seeding: " + e.getMessage());
            e.printStackTrace(); // Catch all other exceptions
        }
    }


    // --- Helper Methods ---

    private Role findOrCreateRole(String roleName, String description) throws HospitalServiceException {
        // Use directly injected roleDao
        Role role = roleDao.findByName(roleName);
        if (role == null) {
            role = roleService.createRole(new Role(roleName, description), SYSTEM_USER_ID, SYSTEM_USERNAME);
            System.out.println("Seeding default '" + roleName + "' role.");
        } else {
            // System.out.println(roleName + " role already exists."); // Suppress for cleaner output
        }
        return role;
    }

    private void assignPermissionsIfRoleExists(Role role, List<PermissionType> permissions) throws HospitalServiceException {
        if (role != null) {
            System.out.println("Assigning permissions to " + role.getName() + " role...");
            for (PermissionType permission : permissions) {
                assignPermissionSafe(role.getId(), permission, SYSTEM_USER_ID, SYSTEM_USERNAME);
            }
            System.out.println("Finished assigning permissions to " + role.getName() + " role.");
        }
    }

    private void assignPermissionSafe(Long roleId, PermissionType permissionType, Long performedByUserId, String performedByUsername) {
        try {
            // Use directly injected rolePermissionDao
            RolePermission existingPermission = rolePermissionDao.findByRoleIdAndPermissionType(roleId, permissionType);

            if (existingPermission == null) { // If permission doesn't exist
                roleService.assignPermissionToRole(roleId, permissionType, performedByUserId, performedByUsername);
                System.out.println("--> Assigned permission: " + permissionType.name() + " to role ID " + roleId);
            } else {
                // System.out.println("--> Permission: " + permissionType.name() + " already assigned to role ID " + roleId); // Suppress for cleaner output
            }
        } catch (HospitalServiceException e) {
            System.err.println("Error assigning permission " + permissionType.name() + " to role ID " + roleId + ": " + e.getMessage());
        }
    }

    // Overloaded method for specific email/password
    private User findOrCreateStaffUser(String email, String password, String firstName, String lastName, String roleName, String specialty, boolean isPrimaryUser) {
        User existingUser = userDao.findByUsername(email);
        if (existingUser == null) {
            Role role = roleDao.findByName(roleName); // Use directly injected roleDao
            if (role == null) {
                System.err.println("Role '" + roleName + "' not found. Cannot create staff user " + email);
                return null;
            }

            Staff staff = new Staff();
            staff.setFirstName(firstName);
            staff.setLastName(lastName);
            staff.setEmail(email);
            staff.setDateOfBirth(getRandomDateOfBirth(1960, 2000));
            // This line assumes your Staff model has a @ManyToOne Role field.
            // If not, remove it, and the staff lookup logic for doctors/nurses will need to exclusively rely on User's role.
            staff.setRole(role);
            staff.setSpecialty(specialty);
            staff.setDateCreated(new Date());
            staff.setCreatedBy(SYSTEM_USER_ID);
            staffDao.save(staff);

            User user = new User();
            user.setUsername(email);
            user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            user.setRole(role);
            user.setStaff(staff); // Link staff to user
            user.setDateCreated(new Date());
            user.setCreatedBy(SYSTEM_USER_ID);
            userDao.save(user);

            if (isPrimaryUser) {
                System.out.println("Created " + roleName + " user: " + email + " / " + password + ", linked to Staff ID: " + staff.getId());
            }
            return user;
        } else {
            // System.out.println(roleName + " user " + email + " already exists.");
        }
        return existingUser;
    }

    // Overloaded method for random email/password based on first name
    private User findOrCreateStaffUser(String firstName, String lastName, String roleName, String specialty, boolean isPrimaryUser) {
        String baseEmail = (firstName.toLowerCase() + "." + lastName.toLowerCase()).replace(" ", "");
        String password = (firstName.toLowerCase() + "123").replace(" ", "");

        String email = baseEmail + "@email.com";
        int counter = 0;
        // Ensure email is unique
        while (userDao.findByUsername(email) != null) {
            counter++;
            email = baseEmail + counter + "@email.com";
        }

        return findOrCreateStaffUser(email, password, firstName, lastName, roleName, specialty, isPrimaryUser);
    }

    // Overloaded method for specific email/password
    private User findOrCreatePatientUser(String email, String password, String firstName, String lastName, boolean isPrimaryUser) {
        User existingUser = userDao.findByUsername(email);
        if (existingUser == null) {
            Role patientRole = roleDao.findByName("PATIENT"); // Use directly injected roleDao
            if (patientRole == null) {
                System.err.println("PATIENT role not found. Cannot create patient user " + email);
                return null;
            }

            Patient patient = new Patient();
            patient.setFirstName(firstName);
            patient.setLastName(lastName);
            patient.setEmail(email);
            patient.setDateOfBirth(getRandomDateOfBirth(1940, 2010));
            patient.setDateCreated(new Date());
            patient.setCreatedBy(SYSTEM_USER_ID);
            patientDao.save(patient);

            User user = new User();
            user.setUsername(email);
            user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            user.setRole(patientRole);
            user.setPatient(patient); // Link patient to user
            user.setDateCreated(new Date());
            user.setCreatedBy(SYSTEM_USER_ID);
            userDao.save(user);

            if (isPrimaryUser) {
                System.out.println("Created PATIENT user: " + email + " / " + password + ", linked to Patient ID: " + patient.getId());
            }
            return user;
        } else {
            // System.out.println("PATIENT user " + email + " already exists.");
        }
        return existingUser;
    }

    // Overloaded method for random email/password based on first name
    private User findOrCreatePatientUser(String firstName, String lastName, boolean isPrimaryUser) {
        String baseEmail = (firstName.toLowerCase() + "." + lastName.toLowerCase()).replace(" ", "");
        String password = (firstName.toLowerCase() + "123").replace(" ", "");

        String email = baseEmail + "@email.com";
        int counter = 0;
        // Ensure email is unique
        while (userDao.findByUsername(email) != null) {
            counter++;
            email = baseEmail + counter + "@email.com";
        }

        return findOrCreatePatientUser(email, password, firstName, lastName, isPrimaryUser);
    }


    private void createSampleAppointment(Staff doctor, Patient patient) {
        try {
            Appointment appointment = new Appointment();
            appointment.setDoctor(doctor);
            appointment.setPatient(patient);
            appointment.setAppointmentDate(getRandomFutureDateTime());
            appointment.setReason(APPOINTMENT_REASONS[random.nextInt(APPOINTMENT_REASONS.length)]);
            appointment.setStatus(AppointmentStatus.values()[random.nextInt(AppointmentStatus.values().length)]);
            appointment.setDateCreated(new Date());
            appointment.setCreatedBy(SYSTEM_USER_ID);
            appointmentDao.save(appointment);
        } catch (Exception e) {
            System.err.println("Error seeding appointment: " + e.getMessage());
        }
    }

    private void createSampleMedicalRecord(Staff doctor, Patient patient) {
        try {
            MedicalRecord record = new MedicalRecord();
            record.setDoctor(doctor);
            record.setPatient(patient);
            record.setRecordDate(getRandomPastDate());
            record.setDiagnosis(DIAGNOSES[random.nextInt(DIAGNOSES.length)]);
            record.setTreatment(TREATMENTS[random.nextInt(TREATMENTS.length)]);
            record.setDateCreated(new Date());
            record.setCreatedBy(SYSTEM_USER_ID);
            medicalRecordDao.save(record);
        } catch (Exception e) {
            System.err.println("Error seeding medical record: " + e.getMessage());
        }
    }

    private void createSampleAdmission(Staff nurse, Patient patient) {
        try {
            Admission admission = new Admission();
            admission.setNurse(nurse);
            admission.setPatient(patient);
            admission.setAdmissionDate((java.sql.Date) getRandomPastDate());
            // Ensure these fields in your Admission model are java.util.Date, not java.sql.Date
            if (random.nextBoolean()) {
                admission.setDischargeDate((java.sql.Date) getRandomFutureDate(admission.getAdmissionDate()));
            }
            admission.setReason(ADMISSION_REASONS[random.nextInt(ADMISSION_REASONS.length)]);
            admission.setWard(WARDS[random.nextInt(WARDS.length)]);
            admission.setWardNumber(WARD_NUMBERS[random.nextInt(WARD_NUMBERS.length)]);
            admission.setDateCreated(new Date());
            admission.setCreatedBy(SYSTEM_USER_ID);
            admissionDao.save(admission);
        } catch (Exception e) {
            System.err.println("Error seeding admission: " + e.getMessage());
        }
    }


    // --- Date Generation Helpers ---

    private Date getRandomDateOfBirth(int startYear, int endYear) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, startYear);
        cal.set(Calendar.MONTH, random.nextInt(12)); // 0-11
        cal.set(Calendar.DAY_OF_MONTH, random.nextInt(28) + 1); // 1-28 for simplicity

        long startMillis = cal.getTimeInMillis();

        cal.set(Calendar.YEAR, endYear);
        cal.set(Calendar.MONTH, random.nextInt(12));
        cal.set(Calendar.DAY_OF_MONTH, random.nextInt(28) + 1);
        long endMillis = cal.getTimeInMillis();

        if (startMillis > endMillis) { // Swap if somehow range is inverted by random day/month
            long temp = startMillis;
            startMillis = endMillis;
            endMillis = temp;
        }

        long randomMillis = ThreadLocalRandom.current().nextLong(startMillis, endMillis);
        return new Date(randomMillis);
    }

    private Date getRandomFutureDateTime() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, random.nextInt(30) + 1); // 1 to 30 days in future
        cal.set(Calendar.HOUR_OF_DAY, 9 + random.nextInt(8)); // 9 AM to 4 PM (inclusive of 9, exclusive of 5 PM)
        cal.set(Calendar.MINUTE, random.nextBoolean() ? 0 : 30);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getRandomPastDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -(random.nextInt(365 * 3) + 1)); // 1 day to 3 years in past
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getRandomFutureDate(Date startDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.DAY_OF_MONTH, random.nextInt(14) + 1); // 1 to 14 days after admission
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}

  */