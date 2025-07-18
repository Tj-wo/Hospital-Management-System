package org.pahappa.controller;

import org.pahappa.model.Admission;
import org.pahappa.model.Appointment;
import org.pahappa.model.Patient;
import org.pahappa.model.Staff;
import org.pahappa.service.admission.AdmissionService;
import org.pahappa.service.appointment.AppointmentService;
import org.pahappa.service.medicalRecord.MedicalRecordService;
import org.pahappa.service.patient.PatientService;
import org.pahappa.model.MedicalRecord;
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
import org.primefaces.model.charts.donut.DonutChartOptions;
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

    // --- NEW CHART MODELS ---
    private LineChartModel monthlyActivityChart;
    private BarChartModel doctorPerformanceChart;
    private BarChartModel doctorAppointmentHistoryChart;
    private LineChartModel patientVitalsChart;

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
    private List<Admission> nurseActiveAdmissions;
    private DonutChartModel nurseWardDistributionChart;
    private long nurseNewAdmissionsToday;
    private long nurseDischargesToday;

    // --- Receptionist Stats ---
    private long allTodaysAppointmentsCount;
    private long newPatientsToday;
    private List<Appointment> receptionistTodaysAppointments;
    private List<Appointment> receptionistNeedsReschedulingList;
    private long receptionistCancelledToday;
    private long receptionistNeedsRescheduling;

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
        createMonthlyActivityChart();
        createDoctorPerformanceChart();

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        adminUpcomingAppointments = allAppointments.stream()
                .filter(a -> a.getAppointmentDate() != null && a.getAppointmentDate().after(now) && a.getStatus() == AppointmentStatus.SCHEDULED)
                .sorted(Comparator.comparing(Appointment::getAppointmentDate))
                .limit(10)
                .collect(Collectors.toList());
    }

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

        createDoctorAppointmentHistoryChart(doctor.getId());
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
        createPatientVitalsChart(patient.getId());
    }

    private void loadNurseData() {
        Staff nurse = loginBean.getLoggedInUser().getStaff();
        if (nurse == null) return;

        List<Admission> nurseAdmissions = admissionService.getAdmissionsForNurse(nurse.getId());
        myActivePatientCount = nurseAdmissions.stream().filter(a -> a.getDischargeDate() == null).count();
        totalAdmissionsManagedByNurse = nurseAdmissions.size();

        nurseActiveAdmissions = nurseAdmissions.stream()
                .filter(a -> a.getDischargeDate() == null)
                .sorted(Comparator.comparing(Admission::getAdmissionDate).reversed())
                .collect(Collectors.toList());
        createNurseWardDistributionChart(nurseActiveAdmissions);

        LocalDate today = LocalDate.now();
        nurseNewAdmissionsToday = nurseAdmissions.stream()
                .filter(a -> a.getAdmissionDate() != null && a.getAdmissionDate().toLocalDate().equals(today))
                .count();

        nurseDischargesToday = nurseAdmissions.stream()
                .filter(a -> a.getDischargeDate() != null && a.getDischargeDate().toLocalDate().equals(today))
                .count();
    }

    private void loadReceptionistData() {
        List<Appointment> allAppointments = appointmentService.getAllAppointments();
        allTodaysAppointmentsCount = filterForToday(allAppointments).size();
        receptionistTodaysAppointments = filterForToday(allAppointments);
        receptionistNeedsReschedulingList = allAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.NEEDS_RESCHEDULING).collect(Collectors.toList());

        List<Patient> allPatients = patientService.getAllPatients();
        LocalDate today = LocalDate.now();
        Timestamp startOfToday = Timestamp.valueOf(LocalDateTime.of(today, LocalTime.MIN));

        newPatientsToday = allPatients.stream()
                .filter(p -> p.getDateCreated() != null && !p.getDateCreated().before(startOfToday))
                .count();

        receptionistCancelledToday = allAppointments.stream()
                .filter(a -> a.getAppointmentDate() != null &&
                        a.getAppointmentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().equals(today) &&
                        (a.getStatus() == AppointmentStatus.CANCELLED_BY_DOCTOR || a.getStatus() == AppointmentStatus.CANCELLED_BY_PATIENT))
                .count();

        receptionistNeedsRescheduling = allAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.NEEDS_RESCHEDULING)
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
        dataSet.setBackgroundColor("rgba(75, 192, 192, 0.2)");
        data.addChartDataSet(dataSet);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        List<String> labels = appointmentsByDay.keySet().stream()
                .map(date -> date.format(formatter))
                .collect(Collectors.toList());
        data.setLabels(labels);

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

        barDataSet.setData(new ArrayList<>(statusCounts.values()));

        List<String> bgColor = Arrays.asList(
                "rgba(54, 162, 235, 0.5)",  // Scheduled
                "rgba(16, 185, 129, 0.5)",  // Completed
                "rgba(239, 68, 68, 0.5)",   // Cancelled
                "rgba(245, 158, 11, 0.5)"   // Needs Rescheduling
        );
        barDataSet.setBackgroundColor(bgColor);

        List<String> borderColor = Arrays.asList(
                "rgb(54, 162, 235)",
                "rgb(16, 185, 129)",
                "rgb(239, 68, 68)",
                "rgb(245, 158, 11)"
        );
        barDataSet.setBorderColor(borderColor);
        barDataSet.setBorderWidth(1);

        data.addChartDataSet(barDataSet);
        data.setLabels(new ArrayList<>(statusCounts.keySet()));
        statusDistributionBarChart.setData(data);

        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setBeginAtZero(true);
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

        dataSet.setData(Arrays.asList(totalDoctors, totalNurses, totalReceptionists));
        dataSet.setBackgroundColor(Arrays.asList(
                "rgb(37, 99, 235)",   // Primary Blue
                "rgb(16, 185, 129)",  // Medical Teal
                "rgb(245, 158, 11)"   // Warning Yellow
        ));

        data.addChartDataSet(dataSet);
        data.setLabels(Arrays.asList("Doctors", "Nurses", "Receptionists"));
        staffDistributionChart.setData(data);

        DonutChartOptions options = new DonutChartOptions();
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Staff Distribution");
        options.setTitle(title);
        staffDistributionChart.setOptions(options);
    }

    private void createAdmissionStatusChart() {
        admissionStatusChart = new DonutChartModel();
        ChartData data = new ChartData();
        DonutChartDataSet dataSet = new DonutChartDataSet();

        dataSet.setData(Arrays.asList(activeAdmissionsCount, dischargedAdmissionsCount));
        dataSet.setBackgroundColor(Arrays.asList(
                "rgb(239, 68, 68)",   // Danger Red
                "rgb(100, 116, 139)"  // Gray
        ));

        data.addChartDataSet(dataSet);
        data.setLabels(Arrays.asList("Active Admissions", "Discharged"));
        admissionStatusChart.setData(data);

        DonutChartOptions options = new DonutChartOptions();
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Admission Status");
        options.setTitle(title);
        admissionStatusChart.setOptions(options);
    }

    private void createMonthlyActivityChart() {
        monthlyActivityChart = new LineChartModel();
        ChartData data = new ChartData();

        String primaryBlue = "rgb(37, 99, 235)";
        String medicalTeal = "rgb(20, 184, 166)";
        String primaryBlueTransparent = "rgba(37, 99, 235, 0.2)";
        String medicalTealTransparent = "rgba(20, 184, 166, 0.2)";

        LineChartDataSet patientDataSet = new LineChartDataSet();
        patientDataSet.setLabel("New Patients");
        Map<String, Long> patientMonthlyData = patientService.getMonthlyPatientRegistrations(6);
        patientDataSet.setData(new ArrayList<>(patientMonthlyData.values()));
        patientDataSet.setFill(true);
        patientDataSet.setBorderColor(primaryBlue);
        patientDataSet.setBackgroundColor(primaryBlueTransparent);
        patientDataSet.setTension(0.4);
        data.addChartDataSet(patientDataSet);

        LineChartDataSet apptDataSet = new LineChartDataSet();
        apptDataSet.setLabel("Appointments");
        Map<String, Long> apptMonthlyData = appointmentService.getMonthlyAppointmentCreations(6);
        apptDataSet.setData(new ArrayList<>(apptMonthlyData.values()));
        apptDataSet.setFill(true);
        apptDataSet.setBorderColor(medicalTeal);
        apptDataSet.setBackgroundColor(medicalTealTransparent);
        apptDataSet.setTension(0.4);
        data.addChartDataSet(apptDataSet);

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
        data.setLabels(patientMonthlyData.keySet().stream()
                .map(ym -> LocalDate.parse(ym + "-01").format(monthFormatter))
                .collect(Collectors.toList()));

        LineChartOptions options = new LineChartOptions();
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Monthly Activity (Last 6 Months)");
        options.setTitle(title);

        // CORRECTED: Simplified axis configuration to ensure compatibility
        CartesianScales scales = new CartesianScales();
        CartesianLinearAxes yAxes = new CartesianLinearAxes();
        yAxes.setBeginAtZero(true);
        scales.addYAxesData(yAxes);
        options.setScales(scales);

        monthlyActivityChart.setOptions(options);
        monthlyActivityChart.setData(data);
    }

    private void createDoctorPerformanceChart() {
        doctorPerformanceChart = new BarChartModel();
        ChartData data = new ChartData();
        BarChartDataSet barDataSet = new BarChartDataSet();
        barDataSet.setLabel("Completed Appointments");

        Map<String, Long> performanceData = appointmentService.getDoctorPerformance(30);
        barDataSet.setData(new ArrayList<>(performanceData.values()));
        barDataSet.setBackgroundColor("rgba(0, 255, 0, 1)");

        data.addChartDataSet(barDataSet);
        data.setLabels(new ArrayList<>(performanceData.keySet()));
        doctorPerformanceChart.setData(data);

        BarChartOptions options = new BarChartOptions();
        options.setIndexAxis("y");

        Title title = new Title();
        title.setDisplay(true);
        title.setText("Busiest Doctors (Last 30 Days)");
        options.setTitle(title);

        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setBeginAtZero(true);
        cScales.addXAxesData(linearAxes);
        options.setScales(cScales);

        doctorPerformanceChart.setOptions(options);
    }

    private void createDoctorAppointmentHistoryChart(Long doctorId) {
        doctorAppointmentHistoryChart = new BarChartModel();
        ChartData data = new ChartData();
        BarChartDataSet barDataSet = new BarChartDataSet();
        barDataSet.setLabel("Appointments");

        Map<LocalDate, Long> dailyCounts = appointmentService.getDailyAppointmentCountForDoctor(doctorId, 7);
        barDataSet.setData(new ArrayList<>(dailyCounts.values()));
        barDataSet.setBackgroundColor("rgba(16, 185, 129, 0.6)"); // Medical Teal

        data.addChartDataSet(barDataSet);

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE, MMM dd");
        data.setLabels(dailyCounts.keySet().stream().map(d -> d.format(dayFormatter)).collect(Collectors.toList()));
        doctorAppointmentHistoryChart.setData(data);

        BarChartOptions options = new BarChartOptions();
        Title title = new Title();
        title.setDisplay(true);
        title.setText("My Appointment History (Last 7 Days)");
        options.setTitle(title);

        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setBeginAtZero(true);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);

        doctorAppointmentHistoryChart.setOptions(options);
    }

    private void createPatientVitalsChart(Long patientId) {
        patientVitalsChart = new LineChartModel();
        ChartData data = new ChartData();
        List<MedicalRecord> records = medicalRecordService.getRecordsForPatient(patientId);

        if (records.isEmpty()) {
            LineChartDataSet emptyDataSet = new LineChartDataSet();
            emptyDataSet.setLabel("No Vitals Data");
            emptyDataSet.setData(Collections.emptyList());
            data.addChartDataSet(emptyDataSet);
            data.setLabels(Collections.singletonList("No Data Available"));
        } else {
            // In a real implementation, you would extract structured vitals.
            // This is a placeholder.
            LineChartDataSet recordCountSet = new LineChartDataSet();
            recordCountSet.setLabel("Medical Records");
            recordCountSet.setData(records.stream().map(r -> 1L).collect(Collectors.toList()));
            recordCountSet.setFill(true);
            recordCountSet.setBorderColor("rgb(37, 99, 235)");
            recordCountSet.setBackgroundColor("rgba(37, 99, 235, 0.2)");
            data.addChartDataSet(recordCountSet);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            data.setLabels(records.stream()
                    .map(r -> r.getDateCreated().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(formatter))
                    .collect(Collectors.toList()));
        }

        LineChartOptions options = new LineChartOptions();
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Vitals History");
        options.setTitle(title);

        patientVitalsChart.setData(data);
        patientVitalsChart.setOptions(options);
    }

    private void createNurseWardDistributionChart(List<Admission> activeAdmissions) {
        nurseWardDistributionChart = new DonutChartModel();
        ChartData data = new ChartData();
        DonutChartDataSet dataSet = new DonutChartDataSet();

        Map<String, Long> wardCounts = activeAdmissions.stream()
                .collect(Collectors.groupingBy(
                        admission -> admission.getWard() != null ? admission.getWard() : "N/A",
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        dataSet.setData(new ArrayList<>(wardCounts.values()));
        dataSet.setBackgroundColor(Arrays.asList(
                "rgb(37, 99, 235)",
                "rgb(16, 185, 129)",
                "rgb(245, 158, 11)",
                "rgb(239, 68, 68)"
        ));

        data.addChartDataSet(dataSet);
        data.setLabels(new ArrayList<>(wardCounts.keySet()));
        nurseWardDistributionChart.setData(data);

        DonutChartOptions options = new DonutChartOptions();
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Patient Distribution by Ward");
        options.setTitle(title);
        nurseWardDistributionChart.setOptions(options);
    }

    private List<Appointment> filterForToday(List<Appointment> appointments) {
        if (appointments == null) {
            return Collections.emptyList();
        }
        LocalDate today = LocalDate.now();
        return appointments.stream()
                .filter(a -> a.getAppointmentDate() != null &&
                        a.getAppointmentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().equals(today))
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
        appointmentsLineChart = new LineChartModel();
        statusDistributionBarChart = new BarChartModel();
        staffDistributionChart = new DonutChartModel();
        admissionStatusChart = new DonutChartModel();
        monthlyActivityChart = new LineChartModel();
        doctorPerformanceChart = new BarChartModel();
        doctorAppointmentHistoryChart = new BarChartModel();
        patientVitalsChart = new LineChartModel();
        todaysAppointments = Collections.emptyList();
        totalPatientsSeenByDoctor = 0;
        doctorAppointmentStatusCounts = Collections.emptyMap();
        upcomingAppointments = Collections.emptyList();
        totalPatientAdmissions = 0;
        totalPatientMedicalRecords = 0;
        myActivePatientCount = 0;
        totalAdmissionsManagedByNurse = 0;
        nurseActiveAdmissions = Collections.emptyList();
        nurseWardDistributionChart = new DonutChartModel();
        nurseNewAdmissionsToday = 0;
        nurseDischargesToday = 0;
        allTodaysAppointmentsCount = 0;
        newPatientsToday = 0;
        receptionistTodaysAppointments = Collections.emptyList();
        receptionistNeedsReschedulingList = Collections.emptyList();
        receptionistCancelledToday = 0;
        receptionistNeedsRescheduling = 0;
    }

    // --- ADDED: Getters and Setters for all fields accessed by the dashboard ---

    public long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public long getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public long getTotalStaff() {
        return totalStaff;
    }

    public void setTotalStaff(long totalStaff) {
        this.totalStaff = totalStaff;
    }

    public long getTotalDoctors() {
        return totalDoctors;
    }

    public void setTotalDoctors(long totalDoctors) {
        this.totalDoctors = totalDoctors;
    }

    public long getTotalNurses() {
        return totalNurses;
    }

    public void setTotalNurses(long totalNurses) {
        this.totalNurses = totalNurses;
    }

    public long getTotalReceptionists() {
        return totalReceptionists;
    }

    public void setTotalReceptionists(long totalReceptionists) {
        this.totalReceptionists = totalReceptionists;
    }

    public long getActiveAdmissionsCount() {
        return activeAdmissionsCount;
    }

    public void setActiveAdmissionsCount(long activeAdmissionsCount) {
        this.activeAdmissionsCount = activeAdmissionsCount;
    }

    public long getDischargedAdmissionsCount() {
        return dischargedAdmissionsCount;
    }

    public void setDischargedAdmissionsCount(long dischargedAdmissionsCount) {
        this.dischargedAdmissionsCount = dischargedAdmissionsCount;
    }

    public long getTotalMedicalRecords() {
        return totalMedicalRecords;
    }

    public void setTotalMedicalRecords(long totalMedicalRecords) {
        this.totalMedicalRecords = totalMedicalRecords;
    }

    public List<Appointment> getAdminUpcomingAppointments() {
        return adminUpcomingAppointments;
    }

    public void setAdminUpcomingAppointments(List<Appointment> adminUpcomingAppointments) {
        this.adminUpcomingAppointments = adminUpcomingAppointments;
    }

    public LineChartModel getAppointmentsLineChart() {
        return appointmentsLineChart;
    }

    public void setAppointmentsLineChart(LineChartModel appointmentsLineChart) {
        this.appointmentsLineChart = appointmentsLineChart;
    }

    public BarChartModel getStatusDistributionBarChart() {
        return statusDistributionBarChart;
    }

    public void setStatusDistributionBarChart(BarChartModel statusDistributionBarChart) {
        this.statusDistributionBarChart = statusDistributionBarChart;
    }

    public DonutChartModel getStaffDistributionChart() {
        return staffDistributionChart;
    }

    public void setStaffDistributionChart(DonutChartModel staffDistributionChart) {
        this.staffDistributionChart = staffDistributionChart;
    }

    public DonutChartModel getAdmissionStatusChart() {
        return admissionStatusChart;
    }

    public void setAdmissionStatusChart(DonutChartModel admissionStatusChart) {
        this.admissionStatusChart = admissionStatusChart;
    }

    public LineChartModel getMonthlyActivityChart() {
        return monthlyActivityChart;
    }

    public void setMonthlyActivityChart(LineChartModel monthlyActivityChart) {
        this.monthlyActivityChart = monthlyActivityChart;
    }

    public BarChartModel getDoctorPerformanceChart() {
        return doctorPerformanceChart;
    }

    public void setDoctorPerformanceChart(BarChartModel doctorPerformanceChart) {
        this.doctorPerformanceChart = doctorPerformanceChart;
    }

    public BarChartModel getDoctorAppointmentHistoryChart() {
        return doctorAppointmentHistoryChart;
    }

    public void setDoctorAppointmentHistoryChart(BarChartModel doctorAppointmentHistoryChart) {
        this.doctorAppointmentHistoryChart = doctorAppointmentHistoryChart;
    }

    public LineChartModel getPatientVitalsChart() {
        return patientVitalsChart;
    }

    public void setPatientVitalsChart(LineChartModel patientVitalsChart) {
        this.patientVitalsChart = patientVitalsChart;
    }

    public List<Appointment> getTodaysAppointments() {
        return todaysAppointments;
    }

    public void setTodaysAppointments(List<Appointment> todaysAppointments) {
        this.todaysAppointments = todaysAppointments;
    }

    public long getTotalPatientsSeenByDoctor() {
        return totalPatientsSeenByDoctor;
    }

    public void setTotalPatientsSeenByDoctor(long totalPatientsSeenByDoctor) {
        this.totalPatientsSeenByDoctor = totalPatientsSeenByDoctor;
    }

    public Map<String, Long> getDoctorAppointmentStatusCounts() {
        return doctorAppointmentStatusCounts;
    }

    public void setDoctorAppointmentStatusCounts(Map<String, Long> doctorAppointmentStatusCounts) {
        this.doctorAppointmentStatusCounts = doctorAppointmentStatusCounts;
    }

    public List<Appointment> getUpcomingAppointments() {
        return upcomingAppointments;
    }

    public void setUpcomingAppointments(List<Appointment> upcomingAppointments) {
        this.upcomingAppointments = upcomingAppointments;
    }

    public long getTotalPatientAdmissions() {
        return totalPatientAdmissions;
    }

    public void setTotalPatientAdmissions(long totalPatientAdmissions) {
        this.totalPatientAdmissions = totalPatientAdmissions;
    }

    public long getTotalPatientMedicalRecords() {
        return totalPatientMedicalRecords;
    }

    public void setTotalPatientMedicalRecords(long totalPatientMedicalRecords) {
        this.totalPatientMedicalRecords = totalPatientMedicalRecords;
    }

    public long getMyActivePatientCount() {
        return myActivePatientCount;
    }

    public void setMyActivePatientCount(long myActivePatientCount) {
        this.myActivePatientCount = myActivePatientCount;
    }

    public long getTotalAdmissionsManagedByNurse() {
        return totalAdmissionsManagedByNurse;
    }

    public void setTotalAdmissionsManagedByNurse(long totalAdmissionsManagedByNurse) {
        this.totalAdmissionsManagedByNurse = totalAdmissionsManagedByNurse;
    }

    public List<Admission> getNurseActiveAdmissions() {
        return nurseActiveAdmissions;
    }

    public void setNurseActiveAdmissions(List<Admission> nurseActiveAdmissions) {
        this.nurseActiveAdmissions = nurseActiveAdmissions;
    }

    public DonutChartModel getNurseWardDistributionChart() {
        return nurseWardDistributionChart;
    }

    public void setNurseWardDistributionChart(DonutChartModel nurseWardDistributionChart) {
        this.nurseWardDistributionChart = nurseWardDistributionChart;
    }

    public long getNurseNewAdmissionsToday() {
        return nurseNewAdmissionsToday;
    }

    public void setNurseNewAdmissionsToday(long nurseNewAdmissionsToday) {
        this.nurseNewAdmissionsToday = nurseNewAdmissionsToday;
    }

    public long getNurseDischargesToday() {
        return nurseDischargesToday;
    }

    public void setNurseDischargesToday(long nurseDischargesToday) {
        this.nurseDischargesToday = nurseDischargesToday;
    }

    public long getAllTodaysAppointmentsCount() {
        return allTodaysAppointmentsCount;
    }

    public void setAllTodaysAppointmentsCount(long allTodaysAppointmentsCount) {
        this.allTodaysAppointmentsCount = allTodaysAppointmentsCount;
    }

    public long getNewPatientsToday() {
        return newPatientsToday;
    }

    public void setNewPatientsToday(long newPatientsToday) {
        this.newPatientsToday = newPatientsToday;
    }

    public List<Appointment> getReceptionistTodaysAppointments() {
        return receptionistTodaysAppointments;
    }

    public void setReceptionistTodaysAppointments(List<Appointment> receptionistTodaysAppointments) {
        this.receptionistTodaysAppointments = receptionistTodaysAppointments;
    }

    public List<Appointment> getReceptionistNeedsReschedulingList() {
        return receptionistNeedsReschedulingList;
    }

    public void setReceptionistNeedsReschedulingList(List<Appointment> receptionistNeedsReschedulingList) {
        this.receptionistNeedsReschedulingList = receptionistNeedsReschedulingList;
    }

    public long getReceptionistCancelledToday() {
        return receptionistCancelledToday;
    }

    public void setReceptionistCancelledToday(long receptionistCancelledToday) {
        this.receptionistCancelledToday = receptionistCancelledToday;
    }

    public long getReceptionistNeedsRescheduling() {
        return receptionistNeedsRescheduling;
    }

    public void setReceptionistNeedsRescheduling(long receptionistNeedsRescheduling) {
        this.receptionistNeedsRescheduling = receptionistNeedsRescheduling;
    }
}