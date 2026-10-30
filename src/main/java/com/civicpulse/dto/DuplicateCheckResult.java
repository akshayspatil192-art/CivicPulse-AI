package com.civicpulse.dto;

public class DuplicateCheckResult {
    private boolean isDuplicate;
    private Long existingComplaintId;
    private String existingTrackingId;
    private String title;
    private double distanceMeters;
    private double textSimilarity;
    private String reasoning;

    public DuplicateCheckResult() {}

    public DuplicateCheckResult(boolean isDuplicate, Long existingComplaintId, String existingTrackingId, String title, double distanceMeters, double textSimilarity, String reasoning) {
        this.isDuplicate = isDuplicate;
        this.existingComplaintId = existingComplaintId;
        this.existingTrackingId = existingTrackingId;
        this.title = title;
        this.distanceMeters = distanceMeters;
        this.textSimilarity = textSimilarity;
        this.reasoning = reasoning;
    }

    public boolean isDuplicate() { return isDuplicate; }
    public void setDuplicate(boolean duplicate) { isDuplicate = duplicate; }

    public Long getExistingComplaintId() { return existingComplaintId; }
    public void setExistingComplaintId(Long existingComplaintId) { this.existingComplaintId = existingComplaintId; }

    public String getExistingTrackingId() { return existingTrackingId; }
    public void setExistingTrackingId(String existingTrackingId) { this.existingTrackingId = existingTrackingId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(double distanceMeters) { this.distanceMeters = distanceMeters; }

    public double getTextSimilarity() { return textSimilarity; }
    public void setTextSimilarity(double textSimilarity) { this.textSimilarity = textSimilarity; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
}
