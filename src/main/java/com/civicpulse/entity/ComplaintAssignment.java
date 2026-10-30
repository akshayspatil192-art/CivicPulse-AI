package com.civicpulse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_assignments")
public class ComplaintAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ComplaintAssignment() {}

    public ComplaintAssignment(Complaint complaint, Department department, User assignedBy, String notes) {
        this.complaint = complaint;
        this.department = department;
        this.assignedBy = assignedBy;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Complaint getComplaint() {
        return complaint;
    }
    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public Department getDepartment() {
        return department;
    }
    public void setDepartment(Department department) {
        this.department = department;
    }

    public User getAssignedBy() {
        return assignedBy;
    }
    public void setAssignedBy(User assignedBy) {
        this.assignedBy = assignedBy;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
