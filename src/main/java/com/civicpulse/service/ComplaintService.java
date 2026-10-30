package com.civicpulse.service;

import com.civicpulse.ai.AiProcessingService;
import com.civicpulse.dto.*;
import com.civicpulse.entity.*;
import com.civicpulse.exception.BadRequestException;
import com.civicpulse.exception.ResourceNotFoundException;
import com.civicpulse.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ComplaintStatusHistoryRepository statusHistoryRepository;
    private final ComplaintAssignmentRepository assignmentRepository;
    private final AiProcessingService aiProcessingService;
    private final FileStorageService fileStorageService;

    public ComplaintService(
            ComplaintRepository complaintRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            ComplaintStatusHistoryRepository statusHistoryRepository,
            ComplaintAssignmentRepository assignmentRepository,
            AiProcessingService aiProcessingService,
            FileStorageService fileStorageService) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.assignmentRepository = assignmentRepository;
        this.aiProcessingService = aiProcessingService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public ComplaintResponse createComplaint(ComplaintCreateRequest req, Long citizenId, MultipartFile image) {
        User citizen = userRepository.findById(citizenId)
            .orElseThrow(() -> new ResourceNotFoundException("Citizen user not found with ID: " + citizenId));

        String imageUrl = req.getImageUrl();
        if (image != null && !image.isEmpty()) {
            imageUrl = fileStorageService.storeFile(image);
        }

        // Run AI Analysis Pipeline:
        AiAnalysisResult aiResult = aiProcessingService.processNewComplaint(
            req.getTitle(),
            req.getDescription(),
            req.getCategory(),
            req.getLatitude(),
            req.getLongitude()
        );

        ComplaintCategory finalCategory = (req.getCategory() != null && req.getCategory() != ComplaintCategory.OTHER)
            ? req.getCategory()
            : aiResult.getDetectedCategory();

        Complaint complaint = new Complaint();
        complaint.setTrackingId(generateTrackingId());
        complaint.setCitizen(citizen);
        complaint.setTitle(req.getTitle().trim());
        complaint.setDescription(req.getDescription().trim());
        complaint.setCategory(finalCategory != null ? finalCategory : ComplaintCategory.OTHER);
        complaint.setAddress(req.getAddress());
        complaint.setLatitude(req.getLatitude());
        complaint.setLongitude(req.getLongitude());
        complaint.setImageUrl(imageUrl);
        complaint.setPriority(aiResult.getPriority());
        complaint.setStatus(ComplaintStatus.SUBMITTED);
        complaint.setAiSummary(aiResult.getSummary());
        complaint.setAiExplanation(aiResult.getExplanation());
        complaint.setIsDuplicate(aiResult.isDuplicate());

        if (aiResult.isDuplicate() && aiResult.getDuplicateComplaintId() != null) {
            complaintRepository.findById(aiResult.getDuplicateComplaintId()).ifPresent(complaint::setDuplicateOf);
        }

        // Auto-assign default department based on category
        autoAssignDepartmentByCategory(complaint, finalCategory);

        Complaint saved = complaintRepository.save(complaint);

        // Record Initial Status History
        ComplaintStatusHistory history = new ComplaintStatusHistory(
            saved,
            ComplaintStatus.SUBMITTED,
            ComplaintStatus.SUBMITTED,
            "Complaint submitted via Citizen Portal. AI categorized as " + saved.getCategory().getDisplayName() + " with " + saved.getPriority().name() + " priority.",
            citizen
        );
        statusHistoryRepository.save(history);

        return mapToResponse(saved);
    }

    public ComplaintResponse getByTrackingId(String trackingId) {
        Complaint complaint = complaintRepository.findByTrackingId(trackingId.trim().toUpperCase())
            .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with Tracking ID: " + trackingId));
        return mapToResponse(complaint);
    }

    public ComplaintResponse getById(Long id) {
        Complaint complaint = complaintRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));
        return mapToResponse(complaint);
    }

    public List<ComplaintResponse> getCitizenComplaints(Long citizenId) {
        return complaintRepository.findByCitizenIdOrderByCreatedAtDesc(citizenId).stream()
            .map(this::mapToResponse)
            .toList();
    }

    public List<ComplaintResponse> getAllComplaints(ComplaintCategory category, Priority priority, ComplaintStatus status, String search) {
        List<Complaint> list = complaintRepository.findAllByOrderByCreatedAtDesc();

        return list.stream()
            .filter(c -> category == null || c.getCategory() == category)
            .filter(c -> priority == null || c.getPriority() == priority)
            .filter(c -> status == null || c.getStatus() == status)
            .filter(c -> {
                if (search == null || search.trim().isEmpty()) return true;
                String q = search.toLowerCase().trim();
                return c.getTitle().toLowerCase().contains(q)
                    || c.getTrackingId().toLowerCase().contains(q)
                    || c.getAddress().toLowerCase().contains(q)
                    || (c.getDescription() != null && c.getDescription().toLowerCase().contains(q));
            })
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional
    public ComplaintResponse updateStatus(Long complaintId, StatusUpdateRequest req, Long userId) {
        Complaint complaint = complaintRepository.findById(complaintId)
            .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        User user = (userId != null) ? userRepository.findById(userId).orElse(null) : null;
        ComplaintStatus oldStatus = complaint.getStatus();
        complaint.setStatus(req.getStatus());
        Complaint saved = complaintRepository.save(complaint);

        String remarks = (req.getRemarks() != null && !req.getRemarks().isBlank())
            ? req.getRemarks()
            : "Status transitioned from " + oldStatus.getDisplayName() + " to " + req.getStatus().getDisplayName();

        ComplaintStatusHistory history = new ComplaintStatusHistory(saved, oldStatus, req.getStatus(), remarks, user);
        statusHistoryRepository.save(history);

        return mapToResponse(saved);
    }

    @Transactional
    public ComplaintResponse updatePriority(Long complaintId, Priority priority, String reason, Long userId) {
        Complaint complaint = complaintRepository.findById(complaintId)
            .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        Priority oldPriority = complaint.getPriority();
        complaint.setPriority(priority);
        complaint.setAiExplanation(complaint.getAiExplanation() + " | Priority manually adjusted by admin from " + oldPriority.name() + " to " + priority.name() + ". Reason: " + reason);
        Complaint saved = complaintRepository.save(complaint);

        return mapToResponse(saved);
    }

    @Transactional
    public ComplaintResponse assignDepartment(Long complaintId, AssignmentRequest req, Long userId) {
        Complaint complaint = complaintRepository.findById(complaintId)
            .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        Department department = departmentRepository.findById(req.getDepartmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + req.getDepartmentId()));

        User user = (userId != null) ? userRepository.findById(userId).orElse(null) : null;

        complaint.setAssignedDepartment(department);
        if (complaint.getStatus() == ComplaintStatus.SUBMITTED || complaint.getStatus() == ComplaintStatus.UNDER_REVIEW) {
            complaint.setStatus(ComplaintStatus.ASSIGNED);
        }

        Complaint saved = complaintRepository.save(complaint);

        ComplaintAssignment assignment = new ComplaintAssignment(saved, department, user, req.getNotes());
        assignmentRepository.save(assignment);

        ComplaintStatusHistory history = new ComplaintStatusHistory(
            saved,
            complaint.getStatus(),
            complaint.getStatus(),
            "Assigned to " + department.getName() + (req.getNotes() != null ? " - Notes: " + req.getNotes() : ""),
            user
        );
        statusHistoryRepository.save(history);

        return mapToResponse(saved);
    }

    private void autoAssignDepartmentByCategory(Complaint complaint, ComplaintCategory category) {
        if (category == null) return;
        String deptCode = switch (category) {
            case ROAD_DAMAGE -> "ROADS";
            case WATERLOGGING, DRAINAGE_PROBLEMS -> "DRAINAGE";
            case GARBAGE_OVERFLOW -> "SOLID_WASTE";
            case STREETLIGHT_PROBLEMS -> "ELECTRICAL";
            case WATER_SUPPLY -> "WATER_WORKS";
            case OTHER -> "GENERAL_CIVIC";
        };

        departmentRepository.findByCode(deptCode).ifPresent(complaint::setAssignedDepartment);
    }

    public ComplaintResponse mapToResponse(Complaint c) {
        ComplaintResponse dto = new ComplaintResponse();
        dto.setId(c.getId());
        dto.setTrackingId(c.getTrackingId());
        if (c.getCitizen() != null) {
            dto.setCitizenId(c.getCitizen().getId());
            dto.setCitizenName(c.getCitizen().getFullName());
            dto.setCitizenEmail(c.getCitizen().getEmail());
            dto.setCitizenPhone(c.getCitizen().getPhone());
        }
        dto.setTitle(c.getTitle());
        dto.setDescription(c.getDescription());
        dto.setCategory(c.getCategory());
        dto.setCategoryDisplayName(c.getCategory() != null ? c.getCategory().getDisplayName() : "Unassigned");
        dto.setAddress(c.getAddress());
        dto.setLatitude(c.getLatitude());
        dto.setLongitude(c.getLongitude());
        dto.setImageUrl(c.getImageUrl());
        dto.setPriority(c.getPriority());
        dto.setPrioritySla(c.getPriority() != null ? c.getPriority().getSla() : "");
        dto.setStatus(c.getStatus());
        dto.setStatusDisplayName(c.getStatus() != null ? c.getStatus().getDisplayName() : "");
        if (c.getAssignedDepartment() != null) {
            dto.setAssignedDepartmentId(c.getAssignedDepartment().getId());
            dto.setAssignedDepartmentName(c.getAssignedDepartment().getName());
        }
        dto.setAiSummary(c.getAiSummary());
        dto.setAiExplanation(c.getAiExplanation());
        dto.setIsDuplicate(c.getIsDuplicate());
        if (c.getDuplicateOf() != null) {
            dto.setDuplicateOfId(c.getDuplicateOf().getId());
            dto.setDuplicateOfTrackingId(c.getDuplicateOf().getTrackingId());
        }
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());

        List<ComplaintStatusHistory> historyList = statusHistoryRepository.findByComplaintIdOrderByCreatedAtAsc(c.getId());
        dto.setHistory(historyList.stream().map(h -> new ComplaintResponse.StatusHistoryDto(
            h.getId(),
            h.getOldStatus(),
            h.getNewStatus(),
            h.getRemarks(),
            h.getUpdatedBy() != null ? h.getUpdatedBy().getFullName() : "CivicPulse System",
            h.getCreatedAt()
        )).toList());

        return dto;
    }

    private String generateTrackingId() {
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "CP-2026-" + randomNum;
    }
}
