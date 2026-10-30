package com.civicpulse.dto;

import java.util.List;
import java.util.Map;

public class HotspotClusterDto {
    private String clusterId;
    private String name;
    private double centerLatitude;
    private double centerLongitude;
    private double radiusMeters;
    private int complaintCount;
    private String severityLevel; // CRITICAL, HIGH, MEDIUM
    private String dominantCategory;
    private String compoundRiskDescription;
    private Map<String, Integer> categoryBreakdown;
    private List<ComplaintResponse> complaints;

    public HotspotClusterDto() {}

    public String getClusterId() { return clusterId; }
    public void setClusterId(String clusterId) { this.clusterId = clusterId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getCenterLatitude() { return centerLatitude; }
    public void setCenterLatitude(double centerLatitude) { this.centerLatitude = centerLatitude; }

    public double getCenterLongitude() { return centerLongitude; }
    public void setCenterLongitude(double centerLongitude) { this.centerLongitude = centerLongitude; }

    public double getRadiusMeters() { return radiusMeters; }
    public void setRadiusMeters(double radiusMeters) { this.radiusMeters = radiusMeters; }

    public int getComplaintCount() { return complaintCount; }
    public void setComplaintCount(int complaintCount) { this.complaintCount = complaintCount; }

    public String getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(String severityLevel) { this.severityLevel = severityLevel; }

    public String getDominantCategory() { return dominantCategory; }
    public void setDominantCategory(String dominantCategory) { this.dominantCategory = dominantCategory; }

    public String getCompoundRiskDescription() { return compoundRiskDescription; }
    public void setCompoundRiskDescription(String compoundRiskDescription) { this.compoundRiskDescription = compoundRiskDescription; }

    public Map<String, Integer> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(Map<String, Integer> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }

    public List<ComplaintResponse> getComplaints() { return complaints; }
    public void setComplaints(List<ComplaintResponse> complaints) { this.complaints = complaints; }
}
