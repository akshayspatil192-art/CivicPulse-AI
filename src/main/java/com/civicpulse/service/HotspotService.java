package com.civicpulse.service;

import com.civicpulse.ai.HotspotDetectionEngine;
import com.civicpulse.dto.HotspotClusterDto;
import com.civicpulse.entity.Complaint;
import com.civicpulse.repository.ComplaintRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotspotService {

    private final ComplaintRepository complaintRepository;
    private final HotspotDetectionEngine hotspotDetectionEngine;
    private final ComplaintService complaintService;

    public HotspotService(
            ComplaintRepository complaintRepository,
            HotspotDetectionEngine hotspotDetectionEngine,
            ComplaintService complaintService) {
        this.complaintRepository = complaintRepository;
        this.hotspotDetectionEngine = hotspotDetectionEngine;
        this.complaintService = complaintService;
    }

    public List<HotspotClusterDto> getHotspots() {
        List<Complaint> complaints = complaintRepository.findActiveComplaints();
        return hotspotDetectionEngine.detectHotspots(complaints, complaintService::mapToResponse);
    }
}
