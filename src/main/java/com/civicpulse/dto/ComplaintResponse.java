package com.civicpulse.dto;

import com.civicpulse.entity.ComplaintCategory;
import com.civicpulse.entity.ComplaintStatus;
import com.civicpulse.entity.Priority;
import java.time.LocalDateTime;
import java.util.List;

public class ComplaintResponse {
    private Long id;
    private String trackingId;
    private Long citizenId;
    private String citizenName;
    private String citizenEmail;
    private String citizenPhone;
    private String title;
    private String description;
    private ComplaintCategory category;
    private String categoryDisplayName;
    private String address;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private Priority priority;
    private String prioritySla;
    private ComplaintStatus status;
    private String statusDisplayName;
    private Long assignedDepartmentId;
    private String assignedDepartmentName;
    private String aiSummary;
    private String aiExplanation;
    private Boolean isDuplicate;
    private Long duplicateOfId;
    private String duplicateOfTrackingId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<StatusHistoryDto> history;

    public ComplaintResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }

    public Long getCitizenId() { return citizenId; }
    public void setCitizenId(Long citizenId) { this.citizenId = citizenId; }

    public String getCitizenName() { return citizenName; }
    public void setCitizenName(String citizenName) { this.citizenName = citizenName; }

    public String getCitizenEmail() { return citizenEmail; }
    public void setCitizenEmail(String citizenEmail) { this.citizenEmail = citizenEmail; }

    public String getCitizenPhone() { return citizenPhone; }
    public void setCitizenPhone(String citizenPhone) { this.citizenPhone = citizenPhone; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ComplaintCategory getCategory() { return category; }
    public void setCategory(ComplaintCategory category) { this.category = category; }

    public String getCategoryDisplayName() { return categoryDisplayName; }
    public void setCategoryDisplayName(String categoryDisplayName) { this.categoryDisplayName = categoryDisplayName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getPrioritySla() { return prioritySla; }
    public void setPrioritySla(String prioritySla) { this.prioritySla = prioritySla; }

    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }

    public String getStatusDisplayName() { return statusDisplayName; }
    public void setStatusDisplayName(String statusDisplayName) { this.statusDisplayName = statusDisplayName; }

    public Long getAssignedDepartmentId() { return assignedDepartmentId; }
    public void setAssignedDepartmentId(Long assignedDepartmentId) { this.assignedDepartmentId = assignedDepartmentId; }

    public String getAssignedDepartmentName() { return assignedDepartmentName; }
    public void setAssignedDepartmentName(String assignedDepartmentName) { this.assignedDepartmentName = assignedDepartmentName; }

    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }

    public String getAiExplanation() { return aiExplanation; }
    public void setAiExplanation(String aiExplanation) { this.aiExplanation = aiExplanation; }

    public Boolean getIsDuplicate() { return isDuplicate; }
    public void setIsDuplicate(Boolean duplicate) { isDuplicate = duplicate; }

    public Long getDuplicateOfId() { return duplicateOfId; }
    public void setDuplicateOfId(Long duplicateOfId) { this.duplicateOfId = duplicateOfId; }

    public String getDuplicateOfTrackingId() { return duplicateOfTrackingId; }
    public void setDuplicateOfTrackingId(String duplicateOfTrackingId) { this.duplicateOfTrackingId = duplicateOfTrackingId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<StatusHistoryDto> getHistory() { return history; }
    public void setHistory(List<StatusHistoryDto> history) { this.history = history; }

    public static class StatusHistoryDto {
        private Long id;
        private ComplaintStatus oldStatus;
        private ComplaintStatus newStatus;
        private String remarks;
        private String updatedByName;
        private LocalDateTime createdAt;

        public StatusHistoryDto() {}

        public StatusHistoryDto(Long id, ComplaintStatus oldStatus, ComplaintStatus newStatus, String remarks, String updatedByName, LocalDateTime createdAt) {
            this.id = id;
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.remarks = remarks;
            this.updatedByName = updatedByName;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public ComplaintStatus getOldStatus() { return oldStatus; }
        public void setOldStatus(ComplaintStatus oldStatus) { this.oldStatus = oldStatus; }

        public ComplaintStatus getNewStatus() { return newStatus; }
        public void setNewStatus(ComplaintStatus newStatus) { this.newStatus = newStatus; }

        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }

        public String getUpdatedByName() { return updatedByName; }
        public void setUpdatedByName(String updatedByName) { this.updatedByName = updatedByName; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
