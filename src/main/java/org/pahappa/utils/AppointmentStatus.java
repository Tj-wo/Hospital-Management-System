package org.pahappa.utils;

public enum AppointmentStatus {
    SCHEDULED ("SCHEDULED"),
    COMPLETED("COMPLETED"),
    CANCELLED_BY_PATIENT("CANCELLED BY PATIENT"),
    CANCELLED_BY_DOCTOR("CANCELLED BY DOCTOR"),
    NO_SHOW("NO SHOW"),
    NEEDS_RESCHEDULING("NEEDS RESCHEDULING");

    private final String displayName;

    AppointmentStatus(String displayName){
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}