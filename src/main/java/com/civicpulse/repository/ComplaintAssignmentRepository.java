package com.civicpulse.repository;

import com.civicpulse.entity.ComplaintAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintAssignmentRepository extends JpaRepository<ComplaintAssignment, Long> {
    List<ComplaintAssignment> findByComplaintIdOrderByCreatedAtDesc(Long complaintId);
}
