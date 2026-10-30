package com.civicpulse.repository;

import com.civicpulse.entity.Complaint;
import com.civicpulse.entity.ComplaintCategory;
import com.civicpulse.entity.ComplaintStatus;
import com.civicpulse.entity.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByTrackingId(String trackingId);

    List<Complaint> findByCitizenIdOrderByCreatedAtDesc(Long citizenId);

    List<Complaint> findAllByOrderByCreatedAtDesc();

    List<Complaint> findByStatus(ComplaintStatus status);

    List<Complaint> findByPriority(Priority priority);

    List<Complaint> findByCategory(ComplaintCategory category);

    long countByStatus(ComplaintStatus status);

    long countByPriority(Priority priority);

    long countByCategory(ComplaintCategory category);

    @Query("SELECT c FROM Complaint c WHERE c.status <> 'RESOLVED' ORDER BY c.createdAt DESC")
    List<Complaint> findActiveComplaints();

    @Query("SELECT c FROM Complaint c WHERE c.createdAt >= :since")
    List<Complaint> findComplaintsSince(@Param("since") LocalDateTime since);

    @Query("SELECT c.category, COUNT(c) FROM Complaint c GROUP BY c.category")
    List<Object[]> countComplaintsByCategory();

    @Query("SELECT c.priority, COUNT(c) FROM Complaint c GROUP BY c.priority")
    List<Object[]> countComplaintsByPriority();

    @Query("SELECT c.status, COUNT(c) FROM Complaint c GROUP BY c.status")
    List<Object[]> countComplaintsByStatus();
}
