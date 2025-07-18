package org.pahappa.controller.admin;

import org.pahappa.model.AuditLog;
import org.pahappa.service.audit.AuditService;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Named
@ViewScoped
public class AuditLogBean implements Serializable {

    @Inject
    private AuditService auditService;

    private List<AuditLog> filteredLogs;
    private Date startDate;
    private Date endDate;
    private AuditLog.ActionType selectedAction;
    private String selectedEntityType;
    private AuditLog selectedLog;

    // Dummy methods for export functionality to avoid compilation errors
    // You should implement the actual logic for these.
    public void exportToCSV() {
        System.out.println("Export to CSV action called.");
    }

    public void exportToPDF() {
        System.out.println("Export to PDF action called.");
    }

    @PostConstruct
    public void init() {
        filterLogs();
    }

    public void filterLogs() {
        filteredLogs = auditService.getFilteredLogs(startDate, endDate, selectedAction, selectedEntityType);
    }

    /**
     * Clears all filter fields and reloads the data table.
     */
    public void clearFilters() {
        startDate = null;
        endDate = null;
        selectedAction = null;
        selectedEntityType = null;
        filterLogs(); // Reload the logs with no filters
    }

    public void selectLog(AuditLog log) {
        this.selectedLog = log;
    }

    public String getBriefDetails(String fullDetails, int maxLength) {
        if (fullDetails == null || fullDetails.isEmpty()) {
            return "";
        }
        if (fullDetails.length() <= maxLength) {
            return fullDetails;
        }
        return fullDetails.substring(0, maxLength) + "...";
    }

    // --- Getters and Setters ---

    public List<AuditLog> getFilteredLogs() {
        return filteredLogs;
    }

    public void setFilteredLogs(List<AuditLog> filteredLogs) {
        this.filteredLogs = filteredLogs;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public AuditLog.ActionType getSelectedAction() {
        return selectedAction;
    }

    public void setSelectedAction(AuditLog.ActionType selectedAction) {
        this.selectedAction = selectedAction;
    }

    public String getSelectedEntityType() {
        return selectedEntityType;
    }

    public void setSelectedEntityType(String selectedEntityType) {
        this.selectedEntityType = selectedEntityType;
    }

    public AuditLog getSelectedLog() {
        return selectedLog;
    }

    public void setSelectedLog(AuditLog selectedLog) {
        this.selectedLog = selectedLog;
    }
}