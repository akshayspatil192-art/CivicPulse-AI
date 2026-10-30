package com.civicpulse.dto;

import java.util.Map;
import java.util.List;

public class DashboardMetricsDto {
    private long totalComplaints;
    private long pendingComplaints;
    private long criticalComplaints;
    private long inProgressComplaints;
    private long resolvedComplaints;
    private double resolutionRate; // percentage
    private Map<String, Long> categoryCounts;
    private Map<String, Long> priorityCounts;
    private Map<String, Long> statusCounts;
    private List<ComplaintResponse> recentComplaints;

    public DashboardMetricsDto() {}

    public long getTotalComplaints() { return totalComplaints; }
    public void setTotalComplaints(long totalComplaints) { this.totalComplaints = totalComplaints; }

    public long getPendingComplaints() { return pendingComplaints; }
    public void setPendingComplaints(long pendingComplaints) { this.pendingComplaints = pendingComplaints; }

    public long getCriticalComplaints() { return criticalComplaints; }
    public void setCriticalComplaints(long criticalComplaints) { this.criticalComplaints = criticalComplaints; }

    public long getInProgressComplaints() { return inProgressComplaints; }
    public void setInProgressComplaints(long inProgressComplaints) { this.inProgressComplaints = inProgressComplaints; }

    public long getResolvedComplaints() { return resolvedComplaints; }
    public void setResolvedComplaints(long resolvedComplaints) { this.resolvedComplaints = resolvedComplaints; }

    public double getResolutionRate() { return resolutionRate; }
    public void setResolutionRate(double resolutionRate) { this.resolutionRate = resolutionRate; }

    public Map<String, Long> getCategoryCounts() { return categoryCounts; }
    public void setCategoryCounts(Map<String, Long> categoryCounts) { this.categoryCounts = categoryCounts; }

    public Map<String, Long> getPriorityCounts() { return priorityCounts; }
    public void setPriorityCounts(Map<String, Long> priorityCounts) { this.priorityCounts = priorityCounts; }

    public Map<String, Long> getStatusCounts() { return statusCounts; }
    public void setStatusCounts(Map<String, Long> statusCounts) { this.statusCounts = statusCounts; }

    public List<ComplaintResponse> getRecentComplaints() { return recentComplaints; }
    public void setRecentComplaints(List<ComplaintResponse> recentComplaints) { this.recentComplaints = recentComplaints; }
}
