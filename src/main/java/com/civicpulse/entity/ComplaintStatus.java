package com.civicpulse.entity;

public enum ComplaintStatus {
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    ASSIGNED("Assigned"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved");

    private final String displayName;

    ComplaintStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
