package com.civicpulse.controller;

import com.civicpulse.ai.AiProcessingService;
import com.civicpulse.dto.*;
import com.civicpulse.entity.ComplaintCategory;
import com.civicpulse.entity.ComplaintStatus;
import com.civicpulse.entity.Priority;
import com.civicpulse.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final AiProcessingService aiProcessingService;

    public ComplaintController(ComplaintService complaintService, AiProcessingService aiProcessingService) {
        this.complaintService = complaintService;
        this.aiProcessingService = aiProcessingService;
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ComplaintResponse> submitComplaint(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "category", required = false) String categoryStr,
            @RequestParam("address") String address,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam(value = "citizenId", defaultValue = "1") Long citizenId,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        ComplaintCategory category = null;
        if (categoryStr != null && !categoryStr.isBlank() && !categoryStr.equalsIgnoreCase("AUTO")) {
            try {
                category = ComplaintCategory.valueOf(categoryStr);
            } catch (Exception ignored) {}
        }

        ComplaintCreateRequest req = new ComplaintCreateRequest();
        req.setTitle(title);
        req.setDescription(description);
        req.setCategory(category);
        req.setAddress(address);
        req.setLatitude(latitude);
        req.setLongitude(longitude);

        ComplaintResponse response = complaintService.createComplaint(req, citizenId, image);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze")
    public ResponseEntity<AiAnalysisResult> previewAiAnalysis(@RequestBody ComplaintCreateRequest req) {
        AiAnalysisResult result = aiProcessingService.processNewComplaint(
            req.getTitle(),
            req.getDescription(),
            req.getCategory(),
            req.getLatitude(),
            req.getLongitude()
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<ComplaintResponse>> getComplaints(
            @RequestParam(value = "category", required = false) ComplaintCategory category,
            @RequestParam(value = "priority", required = false) Priority priority,
            @RequestParam(value = "status", required = false) ComplaintStatus status,
            @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(complaintService.getAllComplaints(category, priority, status, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComplaintResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getById(id));
    }

    @GetMapping("/track/{trackingId}")
    public ResponseEntity<ComplaintResponse> trackByTrackingId(@PathVariable String trackingId) {
        return ResponseEntity.ok(complaintService.getByTrackingId(trackingId));
    }

    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<List<ComplaintResponse>> getCitizenComplaints(@PathVariable Long citizenId) {
        return ResponseEntity.ok(complaintService.getCitizenComplaints(citizenId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ComplaintResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest req,
            @RequestParam(value = "userId", required = false) Long userId) {
        return ResponseEntity.ok(complaintService.updateStatus(id, req, userId));
    }

    @PatchMapping("/{id}/priority")
    public ResponseEntity<ComplaintResponse> updatePriority(
            @PathVariable Long id,
            @RequestParam("priority") Priority priority,
            @RequestParam(value = "reason", defaultValue = "Administrative triage adjustment") String reason,
            @RequestParam(value = "userId", required = false) Long userId) {
        return ResponseEntity.ok(complaintService.updatePriority(id, priority, reason, userId));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<ComplaintResponse> assignDepartment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentRequest req,
            @RequestParam(value = "userId", required = false) Long userId) {
        return ResponseEntity.ok(complaintService.assignDepartment(id, req, userId));
    }
}
