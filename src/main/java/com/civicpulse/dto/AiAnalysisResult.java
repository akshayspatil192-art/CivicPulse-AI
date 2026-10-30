package com.civicpulse.dto;

import com.civicpulse.entity.ComplaintCategory;
import com.civicpulse.entity.Priority;

public class AiAnalysisResult {
    private ComplaintCategory detectedCategory;
    private String summary;
    private Priority priority;
    private int priorityScore; // 0 - 100
    private String explanation;
    private int safetyRiskFactor; // 0 - 100
    private int urgencyFactor; // 0 - 100
    private int publicImpactFactor; // 0 - 100
    private boolean isDuplicate;
    private Long duplicateComplaintId;
    private String duplicateTrackingId;
    private double duplicateConfidence;
    private String actionBrief;
    private boolean geminiPowered;

    public AiAnalysisResult() {}

    // Getters and Setters
    public ComplaintCategory getDetectedCategory() { return detectedCategory; }
    public void setDetectedCategory(ComplaintCategory detectedCategory) { this.detectedCategory = detectedCategory; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public int getPriorityScore() { return priorityScore; }
    public void setPriorityScore(int priorityScore) { this.priorityScore = priorityScore; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public int getSafetyRiskFactor() { return safetyRiskFactor; }
    public void setSafetyRiskFactor(int safetyRiskFactor) { this.safetyRiskFactor = safetyRiskFactor; }

    public int getUrgencyFactor() { return urgencyFactor; }
    public void setUrgencyFactor(int urgencyFactor) { this.urgencyFactor = urgencyFactor; }

    public int getPublicImpactFactor() { return publicImpactFactor; }
    public void setPublicImpactFactor(int publicImpactFactor) { this.publicImpactFactor = publicImpactFactor; }

    public boolean isDuplicate() { return isDuplicate; }
    public void setDuplicate(boolean duplicate) { isDuplicate = duplicate; }

    public Long getDuplicateComplaintId() { return duplicateComplaintId; }
    public void setDuplicateComplaintId(Long duplicateComplaintId) { this.duplicateComplaintId = duplicateComplaintId; }

    public String getDuplicateTrackingId() { return duplicateTrackingId; }
    public void setDuplicateTrackingId(String duplicateTrackingId) { this.duplicateTrackingId = duplicateTrackingId; }

    public double getDuplicateConfidence() { return duplicateConfidence; }
    public void setDuplicateConfidence(double duplicateConfidence) { this.duplicateConfidence = duplicateConfidence; }

    public String getActionBrief() { return actionBrief; }
    public void setActionBrief(String actionBrief) { this.actionBrief = actionBrief; }

    public boolean isGeminiPowered() { return geminiPowered; }
    public void setGeminiPowered(boolean geminiPowered) { this.geminiPowered = geminiPowered; }
}
