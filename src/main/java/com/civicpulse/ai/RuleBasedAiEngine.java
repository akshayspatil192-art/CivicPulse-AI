package com.civicpulse.ai;

import com.civicpulse.dto.AiAnalysisResult;
import com.civicpulse.entity.ComplaintCategory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RuleBasedAiEngine {

    private final PriorityScoringEngine priorityScoringEngine;

    public RuleBasedAiEngine(PriorityScoringEngine priorityScoringEngine) {
        this.priorityScoringEngine = priorityScoringEngine;
    }

    public AiAnalysisResult analyze(String title, String description, ComplaintCategory userCategory, int nearbyCount) {
        String combined = ((title != null ? title : "") + " " + (description != null ? description : "")).toLowerCase();

        ComplaintCategory detectedCategory = userCategory;
        if (detectedCategory == null || detectedCategory == ComplaintCategory.OTHER) {
            detectedCategory = detectCategory(combined);
        }

        String summary = generateExecutiveSummary(detectedCategory, title, description);
        String actionBrief = generateActionBrief(detectedCategory, title, combined);

        var pResult = priorityScoringEngine.calculate(detectedCategory, title, description, nearbyCount);

        AiAnalysisResult result = new AiAnalysisResult();
        result.setDetectedCategory(detectedCategory);
        result.setSummary(summary);
        result.setActionBrief(actionBrief);
        result.setPriority(pResult.priority());
        result.setPriorityScore(pResult.totalScore());
        result.setSafetyRiskFactor(pResult.safetyRisk());
        result.setUrgencyFactor(pResult.urgency());
        result.setPublicImpactFactor(pResult.publicImpact());
        result.setExplanation(pResult.explanation());
        result.setGeminiPowered(false);

        return result;
    }

    public ComplaintCategory detectCategory(String text) {
        Map<ComplaintCategory, Integer> scores = new EnumMap<>(ComplaintCategory.class);
        for (ComplaintCategory c : ComplaintCategory.values()) {
            scores.put(c, 0);
        }

        // Specific, highly indicative keyword groups
        if (text.matches(".*(waterlog|rainwater|flooding|flood|accumulat|standing water|submerged).*")) {
            scores.put(ComplaintCategory.WATERLOGGING, scores.get(ComplaintCategory.WATERLOGGING) + 12);
        }
        if (text.matches(".*(drain|gutter|sewage|sewer|clogged|blocked drain|manhole|foul smell|waste water).*")) {
            scores.put(ComplaintCategory.DRAINAGE_PROBLEMS, scores.get(ComplaintCategory.DRAINAGE_PROBLEMS) + 12);
        }
        if (text.matches(".*(pothole|crater|asphalt|tar|uneven road|crack|broken road|traffic barrier|road damage).*")) {
            scores.put(ComplaintCategory.ROAD_DAMAGE, scores.get(ComplaintCategory.ROAD_DAMAGE) + 12);
        } else if (text.matches(".*(road|street).*")) {
            scores.put(ComplaintCategory.ROAD_DAMAGE, scores.get(ComplaintCategory.ROAD_DAMAGE) + 1);
        }

        if (text.matches(".*(garbage|trash|waste|dustbin|dump|rubbish|litter|debris|dumpster).*")) {
            scores.put(ComplaintCategory.GARBAGE_OVERFLOW, scores.get(ComplaintCategory.GARBAGE_OVERFLOW) + 12);
        }
        if (text.matches(".*(streetlight|street light|light|dark|bulb|lamp|pole|illumination|wire).*")) {
            scores.put(ComplaintCategory.STREETLIGHT_PROBLEMS, scores.get(ComplaintCategory.STREETLIGHT_PROBLEMS) + 12);
        }
        if (text.matches(".*(drinking water|pipe leak|burst pipe|water supply|no water|low pressure|pipeline).*")) {
            scores.put(ComplaintCategory.WATER_SUPPLY, scores.get(ComplaintCategory.WATER_SUPPLY) + 12);
        }

        ComplaintCategory bestCategory = ComplaintCategory.OTHER;
        int maxScore = 0;
        for (Map.Entry<ComplaintCategory, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                bestCategory = entry.getKey();
            }
        }
        return bestCategory;
    }

    private String generateExecutiveSummary(ComplaintCategory category, String title, String description) {
        String base = (description != null && description.length() > 15) ? description.trim() : (title != null ? title.trim() : "");
        if (base.length() > 140) {
            int dot = base.indexOf('.', 60);
            if (dot != -1 && dot <= 140) {
                base = base.substring(0, dot + 1);
            } else {
                base = base.substring(0, 137) + "...";
            }
        }
        return switch (category) {
            case WATERLOGGING -> "Severe water accumulation obstructing vehicular movement and pedestrian transit.";
            case DRAINAGE_PROBLEMS -> "Sewer/drainage blockage causing sewage overflow and public sanitation hazard.";
            case ROAD_DAMAGE -> "Significant roadway deterioration and surface craters impairing traffic safety.";
            case GARBAGE_OVERFLOW -> "Accumulated municipal solid waste requiring immediate clearance and sanitization.";
            case STREETLIGHT_PROBLEMS -> "Non-operational public illumination leading to safety and security vulnerability.";
            case WATER_SUPPLY -> "Municipal potable water disruption or distribution pipeline leakage.";
            default -> base;
        };
    }

    private String generateActionBrief(ComplaintCategory category, String title, String text) {
        return switch (category) {
            case WATERLOGGING -> "Dispatch high-capacity dewatering pump unit and inspect downstream culvert clearance.";
            case DRAINAGE_PROBLEMS -> "Deploy suction jetting machine for immediate desilting and unclogging.";
            case ROAD_DAMAGE -> "Schedule rapid cold-mix asphalt patch team; erect safety hazard cones.";
            case GARBAGE_OVERFLOW -> "Mobilize compactor truck and sanitation crew for bulk clearance and lime powder disinfection.";
            case STREETLIGHT_PROBLEMS -> "Dispatch electrical lineman crew with aerial lift to repair wiring / replace LED luminaire.";
            case WATER_SUPPLY -> "Alert water works valve engineer; isolate pipeline segment and initiate emergency weld repair.";
            default -> "Assign field officer for on-site assessment and dispatch appropriate municipal work order.";
        };
    }
}
