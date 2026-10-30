package com.civicpulse.service;

import com.civicpulse.dto.DashboardMetricsDto;
import com.civicpulse.entity.ComplaintCategory;
import com.civicpulse.entity.ComplaintStatus;
import com.civicpulse.entity.Priority;
import com.civicpulse.repository.ComplaintRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintService complaintService;

    public DashboardService(ComplaintRepository complaintRepository, ComplaintService complaintService) {
        this.complaintRepository = complaintRepository;
        this.complaintService = complaintService;
    }

    public DashboardMetricsDto getMetrics() {
        DashboardMetricsDto dto = new DashboardMetricsDto();

        long total = complaintRepository.count();
        long resolved = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);
        long inProgress = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);
        long pending = total - (resolved + inProgress);
        long critical = complaintRepository.countByPriority(Priority.CRITICAL);

        dto.setTotalComplaints(total);
        dto.setResolvedComplaints(resolved);
        dto.setInProgressComplaints(inProgress);
        dto.setPendingComplaints(Math.max(pending, 0));
        dto.setCriticalComplaints(critical);
        dto.setResolutionRate(total > 0 ? Math.round(((double) resolved / total) * 1000.0) / 10.0 : 0.0);

        // Category counts
        Map<String, Long> catCounts = new LinkedHashMap<>();
        for (ComplaintCategory c : ComplaintCategory.values()) {
            catCounts.put(c.name(), complaintRepository.countByCategory(c));
        }
        dto.setCategoryCounts(catCounts);

        // Priority counts
        Map<String, Long> priorityCounts = new LinkedHashMap<>();
        for (Priority p : Priority.values()) {
            priorityCounts.put(p.name(), complaintRepository.countByPriority(p));
        }
        dto.setPriorityCounts(priorityCounts);

        // Status counts
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (ComplaintStatus s : ComplaintStatus.values()) {
            statusCounts.put(s.name(), complaintRepository.countByStatus(s));
        }
        dto.setStatusCounts(statusCounts);

        // Recent complaints (top 10)
        dto.setRecentComplaints(complaintRepository.findAllByOrderByCreatedAtDesc().stream()
            .limit(10)
            .map(complaintService::mapToResponse)
            .toList());

        return dto;
    }
}
