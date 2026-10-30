package com.civicpulse.ai;

import com.civicpulse.dto.AiAnalysisResult;
import com.civicpulse.dto.DuplicateCheckResult;
import com.civicpulse.entity.Complaint;
import com.civicpulse.entity.ComplaintCategory;
import com.civicpulse.repository.ComplaintRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AiProcessingService {

    private final GeminiClient geminiClient;
    private final RuleBasedAiEngine ruleBasedAiEngine;
    private final DuplicateDetectionService duplicateDetectionService;
    private final ComplaintRepository complaintRepository;

    public AiProcessingService(
            GeminiClient geminiClient,
            RuleBasedAiEngine ruleBasedAiEngine,
            DuplicateDetectionService duplicateDetectionService,
            ComplaintRepository complaintRepository) {
        this.geminiClient = geminiClient;
        this.ruleBasedAiEngine = ruleBasedAiEngine;
        this.duplicateDetectionService = duplicateDetectionService;
        this.complaintRepository = complaintRepository;
    }

    public AiAnalysisResult processNewComplaint(String title, String description, ComplaintCategory category, Double lat, Double lng) {
        List<Complaint> activeComplaints = complaintRepository.findActiveComplaints();
        DuplicateCheckResult dupResult = duplicateDetectionService.checkForDuplicate(title, description, category, lat, lng, activeComplaints);

        AiAnalysisResult analysisResult;
        Optional<AiAnalysisResult> geminiOpt = geminiClient.analyzeComplaint(title, description, category);

        if (geminiOpt.isPresent()) {
            analysisResult = geminiOpt.get();
        } else {
            int nearbyCount = (int) activeComplaints.stream()
                .filter(c -> c.getLatitude() != null && c.getLongitude() != null && lat != null && lng != null)
                .filter(c -> DuplicateDetectionService.calculateDistanceInMeters(lat, lng, c.getLatitude(), c.getLongitude()) <= 350.0)
                .count();
            analysisResult = ruleBasedAiEngine.analyze(title, description, category, nearbyCount);
        }

        analysisResult.setDuplicate(dupResult.isDuplicate());
        if (dupResult.isDuplicate()) {
            analysisResult.setDuplicateComplaintId(dupResult.getExistingComplaintId());
            analysisResult.setDuplicateTrackingId(dupResult.getExistingTrackingId());
            analysisResult.setDuplicateConfidence(dupResult.getTextSimilarity());
            analysisResult.setExplanation(analysisResult.getExplanation() + " [ALERT: " + dupResult.getReasoning() + "]");
        }

        return analysisResult;
    }
}
