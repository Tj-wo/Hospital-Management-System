package org.pahappa.controller;

import org.pahappa.model.Admission;
import org.pahappa.model.Appointment;
import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
import org.pahappa.service.admission.AdmissionService;
import org.pahappa.service.appointment.AppointmentService;
import org.pahappa.service.medicalRecord.MedicalRecordService;
import org.pahappa.service.patient.PatientService;
import org.pahappa.service.staff.StaffService;
import org.pahappa.utils.AppointmentStatus;

// UPDATED: Using the new PrimeFaces 12 Chart API
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.donut.DonutChartDataSet;
import org.primefaces.model.charts.donut.DonutChartModel;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.optionconfig.title.Title;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Named("dashboardBean")
@SessionScoped
public class DashboardBean implements Serializable {

    // --- Injected Services ---
    @Inject private PatientService patientService;
    @Inject private StaffService staffService;
    @Inject private AppointmentService appointmentService;
    @Inject private AdmissionService admissionService;
    @Inject private MedicalRecordService medicalRecordService;
    @Inject private LoginBean loginBean;

    // --- Admin Stats ---
    private long totalPatients;
    private long totalAppointments;
    private long totalStaff;
    private long totalDoctors;
    private long totalNurses;
    private long totalReceptionists;
    private long activeAdmissionsCount;
    private long dischargedAdmissionsCount;
    private long totalMedicalRecords;
    private List<Appointment> adminUpcomingAppointments;

    // --- UPDATED: Chart Models using the new PrimeFaces 12 API ---
    private LineChartModel appointmentsLineChart;
    private BarChartModel statusDistributionBarChart;
    private DonutChartModel staffDistributionChart;
    private DonutChartModel admissionStatusChart;

    // --- Doctor Stats ---
    private List<Appointment> todaysAppointments;
    private long totalPatientsSeenByDoctor;
    private Map<String, Long> doctorAppointmentStatusCounts;

    // --- Patient Stats ---
    private List<Appointment> upcomingAppointments;
    private long totalPatientAdmissions;
    private long totalPatientMedicalRecords;

    // --- Nurse Stats ---
    private long myActivePatientCount;
    private long totalAdmissionsManagedByNurse;

    // --- Receptionist Stats ---
    private long allTodaysAppointmentsCount;
    private long newPatientsToday;

    @PostConstruct
    public void init() {
        if (loginBean.getLoggedInUser() == null || loginBean.getLoggedInUser().getRole() == null) {
            resetFields();
            return;
        }

        resetFields();
        String roleName = loginBean.getLoggedInUser().getRole().getName();
        switch (roleName) {
            case "ADMIN": loadAdminData(); break;
            case "DOCTOR": loadDoctorData(); break;
            case "PATIENT": loadPatientData(); break;
            case "NURSE": loadNurseData(); break;
            case "RECEPTIONIST": loadReceptionistData(); break;
            default:
                System.out.println("No specific dashboard data for role: " + roleName);
                break;
        }
    }

    private void loadAdminData() {
        totalPatients = patientService.countPatients();
        totalAppointments = appointmentService.countAppointments();
        totalStaff = staffService.getAllStaff().size();
        totalDoctors = staffService.countDoctors();
        totalNurses = staffService.countNurses();
        totalReceptionists = staffService.countReceptionists();
        totalMedicalRecords = medicalRecordService.getAllMedicalRecords().size();

        List<Appointment> allAppointments = appointmentService.getAllAppointments();
        List<Admission> allAdmissions = admissionService.getAllAdmissions();

        activeAdmissionsCount = allAdmissions.stream().filter(a -> a.getDischargeDate() == null).count();
        dischargedAdmissionsCount = allAdmissions.size() - activeAdmissionsCount;

        createAppointmentsLineChart(allAppointments);
        createStatusDistributionBarChart(allAppointments);
        createStaffDistributionChart();
        createAdmissionStatusChart();

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        adminUpcomingAppointments = allAppointments.stream()
                .filter(a -> a.getAppointmentDate() != null && a.getAppointmentDate().after(now) && a.getStatus() == AppointmentStatus.SCHEDULED)
                .sorted(Comparator.comparing(Appointment::getAppointmentDate))
                .limit(10)
                .collect(Collectors.toList());
    }

    // ... (other load methods are unchanged) ...
    private void loadDoctorData() {
        Staff doctor = loginBean.getLoggedInUser().getStaff();
        if (doctor == null) return;

        List<Appointment> doctorAppointments = appointmentService.getAppointmentsForDoctor(doctor.getId());
        todaysAppointments = filterForToday(doctorAppointments);

        doctorAppointmentStatusCounts = new HashMap<>();
        for (AppointmentStatus status : AppointmentStatus.values()) {
            doctorAppointmentStatusCounts.put(status.name(), 0L);
        }
        Map<String, Long> actualCounts = doctorAppointments.stream()
                .collect(Collectors.groupingBy(appt -> appt.getStatus().name(), Collectors.counting()));
        doctorAppointmentStatusCounts.putAll(actualCounts);

        totalPatientsSeenByDoctor = doctorAppointments.stream()
                .map(Appointment::getPatient)
                .distinct()
                .count();
    }

    private void loadPatientData() {
        Patient patient = loginBean.getLoggedInUser().getPatient();
        if (patient == null) return;

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        upcomingAppointments = appointmentService.getAppointmentsByPatient(patient).stream()
                .filter(a -> a.getAppointmentDate() != null && a.getAppointmentDate().after(now) && a.getStatus() == AppointmentStatus.SCHEDULED)
                .collect(Collectors.toList());

        totalPatientAdmissions = admissionService.getAdmissionsForPatient(patient.getId()).size();
        totalPatientMedicalRecords = medicalRecordService.getRecordsForPatient(patient.getId()).size();
    }

    private void loadNurseData() {
        Staff nurse = loginBean.getLoggedInUser().getStaff();
        if (nurse == null) return;

        List<Admission> nurseAdmissions = admissionService.getAdmissionsForNurse(nurse.getId());
        myActivePatientCount = nurseAdmissions.stream().filter(a -> a.getDischargeDate() == null).count();
        totalAdmissionsManagedByNurse = nurseAdmissions.size();
    }

    private void loadReceptionistData() {
        allTodaysAppointmentsCount = filterForToday(appointmentService.getAllAppointments()).size();

        List<Patient> allPatients = patientService.getAllPatients();
        LocalDate today = LocalDate.now();
        Timestamp startOfToday = Timestamp.valueOf(LocalDateTime.of(today, LocalTime.MIN));

        newPatientsToday = allPatients.stream()
                .filter(p -> p.getDateCreated() != null && !p.getDateCreated().before(startOfToday))
                .count();
    }


    // --- UPDATED: Chart Creation Methods using the new PrimeFaces 12 API ---

    private void createAppointmentsLineChart(List<Appointment> appointments) {
        appointmentsLineChart = new LineChartModel();
        ChartData data = new ChartData();
        LineChartDataSet dataSet = new LineChartDataSet();

        Map<LocalDate, Long> appointmentsByDay = appointments.stream()
                .filter(a -> a.getDateCreated().toInstant().isAfter(Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS)))
                .collect(Collectors.groupingBy(
                        a -> a.getDateCreated().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        TreeMap::new,
                        Collectors.counting()
                ));

        List<Object> values = new ArrayList<>(appointmentsByDay.values());
        dataSet.setData(values);
        dataSet.setLabel("Appointments");
        dataSet.setFill(true);
        dataSet.setBorderColor("rgb(75, 192, 192)");
        data.addChartDataSet(dataSet);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        List<String> labels = appointmentsByDay.keySet().stream()
                .map(date -> date.format(formatter))
                .collect(Collectors.toList());
        data.setLabels(labels);

        //Options
        LineChartOptions options = new LineChartOptions();
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Appointments Trend (Last 30 Days)");
        options.setTitle(title);

        appointmentsLineChart.setOptions(options);
        appointmentsLineChart.setData(data);
    }

    private void createStatusDistributionBarChart(List<Appointment> appointments) {
        statusDistributionBarChart = new BarChartModel();
        ChartData data = new ChartData();
        BarChartDataSet barDataSet = new BarChartDataSet();
        barDataSet.setLabel("Count");

        Map<String, Long> statusCounts = appointments.stream()
                .collect(Collectors.groupingBy(a -> a.getStatus().getDisplayName(), Collectors.counting()));

        List<Number> values = new ArrayList<>(statusCounts.values());
        barDataSet.setData(values);

        List<String> bgColor = new ArrayList<>();
        bgColor.add("rgba(255, 99, 132, 0.5)");
        bgColor.add("rgba(255, 159, 64, 0.5)");
        bgColor.add("rgba(16, 185, 129, 0.5)");
        bgColor.add("rgba(75, 192, 192, 0.5)");
        bgColor.add("rgba(54, 162, 235, 0.5)");
        barDataSet.setBackgroundColor(bgColor);

        List<String> borderColor = new ArrayList<>();
        borderColor.add("rgb(255, 99, 132)");
        borderColor.add("rgb(255, 159, 64)");
        borderColor.add("rgb(16, 185, 129)");
        borderColor.add("rgb(75, 192, 192)");
        borderColor.add("rgb(54, 162, 235)");
        barDataSet.setBorderColor(borderColor);
        barDataSet.setBorderWidth(1);

        data.addChartDataSet(barDataSet);
        data.setLabels(new ArrayList<>(statusCounts.keySet()));
        statusDistributionBarChart.setData(data);

        //Options
        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        CartesianLinearTicks ticks = new CartesianLinearTicks();
        ticks.setMirror(true);
        linearAxes.setTicks(ticks);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);

        Title title = new Title();
        title.setDisplay(true);
        title.setText("Appointment Status Distribution");
        options.setTitle(title);

        statusDistributionBarChart.setOptions(options);
    }

    private void createStaffDistributionChart() {
        staffDistributionChart = new DonutChartModel();
        ChartData data = new ChartData();
        DonutChartDataSet dataSet = new DonutChartDataSet();

        List<Number> values = new ArrayList<>();
        values.add(totalDoctors);
        values.add(totalNurses);
        values.add(totalReceptionists);
        dataSet.setData(values);

        List<String> bgColors = new ArrayList<>();
        bgColors.add("rgb(37, 99, 235)");
        bgColors.add("rgb(16, 185, 129)");
        bgColors.add("rgb(245, 158, 11)");
        dataSet.setBackgroundColor(bgColors);

        data.addChartDataSet(dataSet);
        List<String> labels = new ArrayList<>();
        labels.add("Doctors");
        labels.add("Nurses");
        labels.add("Receptionists");
        data.setLabels(labels);

        staffDistributionChart.setData(data);
    }

    private void createAdmissionStatusChart() {
        admissionStatusChart = new DonutChartModel();
        ChartData data = new ChartData();
        DonutChartDataSet dataSet = new DonutChartDataSet();

        List<Number> values = new ArrayList<>();
        values.add(activeAdmissionsCount);
        values.add(dischargedAdmissionsCount);
        dataSet.setData(values);

        List<String> bgColors = new ArrayList<>();
        bgColors.add("rgb(239, 68, 68)");
        bgColors.add("rgb(100, 116, 139)");
        dataSet.setBackgroundColor(bgColors);

        data.addChartDataSet(dataSet);
        List<String> labels = new ArrayList<>();
        labels.add("Active Admissions");
        labels.add("Discharged");
        data.setLabels(labels);

        admissionStatusChart.setData(data);
    }

    private List<Appointment> filterForToday(List<Appointment> appointments) {
        if (appointments == null) {
            return Collections.emptyList();
        }
        LocalDate today = LocalDate.now();
        Timestamp startOfDay = Timestamp.valueOf(LocalDateTime.of(today, LocalTime.MIN));
        Timestamp endOfDay = Timestamp.valueOf(LocalDateTime.of(today, LocalTime.MAX));
        return appointments.stream()
                .filter(a -> a.getAppointmentDate() != null &&
                        !a.getAppointmentDate().before(startOfDay) &&
                        !a.getAppointmentDate().after(endOfDay))
                .collect(Collectors.toList());
    }

    private void resetFields() {
        totalPatients = 0;
        totalAppointments = 0;
        totalStaff = 0;
        totalDoctors = 0;
        totalNurses = 0;
        totalReceptionists = 0;
        activeAdmissionsCount = 0;
        dischargedAdmissionsCount = 0;
        totalMedicalRecords = 0;
        adminUpcomingAppointments = Collections.emptyList();
        appointmentsLineChart = null;
        statusDistributionBarChart = null;
        staffDistributionChart = null;
        admissionStatusChart = null;
        todaysAppointments = Collections.emptyList();
        totalPatientsSeenByDoctor = 0;
        doctorAppointmentStatusCounts = Collections.emptyMap();
        upcomingAppointments = Collections.emptyList();
        totalPatientAdmissions = 0;
        totalPatientMedicalRecords = 0;
        myActivePatientCount = 0;
        totalAdmissionsManagedByNurse = 0;
        allTodaysAppointmentsCount = 0;
        newPatientsToday = 0;
    }

    // --- Getters for all fields ---
    public long getTotalPatients() { return totalPatients; }
    public long getTotalAppointments() { return totalAppointments; }
    public long getTotalStaff() { return totalStaff; }
    public long getTotalDoctors() { return totalDoctors; }
    public long getTotalNurses() { return totalNurses; }
    public long getTotalReceptionists() { return totalReceptionists; }
    public long getActiveAdmissionsCount() { return activeAdmissionsCount; }
    public long getDischargedAdmissionsCount() { return dischargedAdmissionsCount; }
    public long getTotalMedicalRecords() { return totalMedicalRecords; }
    public List<Appointment> getAdminUpcomingAppointments() { return adminUpcomingAppointments; }
    public LineChartModel getAppointmentsLineChart() { return appointmentsLineChart; }
    public BarChartModel getStatusDistributionBarChart() { return statusDistributionBarChart; }
    public DonutChartModel getStaffDistributionChart() { return staffDistributionChart; }
    public DonutChartModel getAdmissionStatusChart() { return admissionStatusChart; }
    public List<Appointment> getTodaysAppointments() { return todaysAppointments; }
    public long getTotalPatientsSeenByDoctor() { return totalPatientsSeenByDoctor; }
    public Map<String, Long> getDoctorAppointmentStatusCounts() { return doctorAppointmentStatusCounts; }
    public List<Appointment> getUpcomingAppointments() { return upcomingAppointments; }
    public long getTotalPatientAdmissions() { return totalPatientAdmissions; }
    public long getTotalPatientMedicalRecords() { return totalPatientMedicalRecords; }
    public long getMyActivePatientCount() { return myActivePatientCount; }
    public long getTotalAdmissionsManagedByNurse() { return totalAdmissionsManagedByNurse; }
    public long getAllTodaysAppointmentsCount() { return allTodaysAppointmentsCount; }
    public long getNewPatientsToday() { return newPatientsToday; }
}