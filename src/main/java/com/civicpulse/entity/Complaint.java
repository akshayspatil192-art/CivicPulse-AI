package com.civicpulse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints", indexes = {
    @Index(name = "idx_tracking_id", columnList = "trackingId"),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_priority", columnList = "priority"),
    @Index(name = "idx_status", columnList = "status")
})
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String trackingId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintCategory category;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(length = 255)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComplaintStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department assignedDepartment;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    @Column(columnDefinition = "TEXT")
    private String aiExplanation;

    @Column(nullable = false)
    private Boolean isDuplicate = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duplicate_of_id")
    private Complaint duplicateOf;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Complaint() {}

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.isDuplicate == null) {
            this.isDuplicate = false;
        }
        if (this.status == null) {
            this.status = ComplaintStatus.SUBMITTED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getTrackingId() {
        return trackingId;
    }
    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public User getCitizen() {
        return citizen;
    }
    public void setCitizen(User citizen) {
        this.citizen = citizen;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public ComplaintCategory getCategory() {
        return category;
    }
    public void setCategory(ComplaintCategory category) {
        this.category = category;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Priority getPriority() {
        return priority;
    }
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public ComplaintStatus getStatus() {
        return status;
    }
    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public Department getAssignedDepartment() {
        return assignedDepartment;
    }
    public void setAssignedDepartment(Department assignedDepartment) {
        this.assignedDepartment = assignedDepartment;
    }

    public String getAiSummary() {
        return aiSummary;
    }
    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }

    public String getAiExplanation() {
        return aiExplanation;
    }
    public void setAiExplanation(String aiExplanation) {
        this.aiExplanation = aiExplanation;
    }

    public Boolean getIsDuplicate() {
        return isDuplicate;
    }
    public void setIsDuplicate(Boolean duplicate) {
        isDuplicate = duplicate;
    }

    public Complaint getDuplicateOf() {
        return duplicateOf;
    }
    public void setDuplicateOf(Complaint duplicateOf) {
        this.duplicateOf = duplicateOf;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
