package com.civicpulse.ai;

import com.civicpulse.entity.ComplaintCategory;
import com.civicpulse.entity.Priority;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PriorityScoringEngine {

    private static final List<String> CRITICAL_HAZARDS = List.of(
        "open manhole", "live wire", "electric shock", "sparking wire", "deep crater",
        "bridge collapse", "sinkhole", "toxic gas", "ambulance blocked", "hospital access",
        "fallen pole", "fire hazard", "burst main line", "submerged vehicle", "structural collapse"
    );

    private static final List<String> HIGH_HAZARDS = List.of(
        "severe waterlogging", "knee deep water", "heavy traffic jam", "arterial road",
        "school zone", "bus stop flooded", "overflowing sewage", "foul smell",
        "contamination", "no water for 3 days", "blackout", "pitch dark road", "major accident risk"
    );

    private static final List<String> MEDIUM_HAZARDS = List.of(
        "pothole", "broken street light", "garbage pile", "clogged drain", "stagnant water",
        "low water pressure", "broken curb", "mosquito breeding", "leaking tap"
    );

    public record PriorityResult(
        Priority priority,
        int totalScore,
        int safetyRisk,
        int urgency,
        int publicImpact,
        String explanation
    ) {}

    public PriorityResult calculate(ComplaintCategory category, String title, String description, int nearbyComplaintsCount) {
        String fullText = ((title != null ? title : "") + " " + (description != null ? description : "")).toLowerCase();

        int safetyRisk = computeSafetyRisk(fullText, category);
        int urgency = computeUrgency(fullText, category);
        int publicImpact = computePublicImpact(fullText, nearbyComplaintsCount);

        int totalScore = (int) Math.round((safetyRisk * 0.40) + (urgency * 0.30) + (publicImpact * 0.30));

        Priority priority;
        if (totalScore >= 78 || safetyRisk >= 85) {
            priority = Priority.CRITICAL;
        } else if (totalScore >= 58 || safetyRisk >= 65) {
            priority = Priority.HIGH;
        } else if (totalScore >= 38) {
            priority = Priority.MEDIUM;
        } else {
            priority = Priority.LOW;
        }

        String explanation = generateExplanation(priority, category, safetyRisk, urgency, publicImpact, fullText);

        return new PriorityResult(priority, totalScore, safetyRisk, urgency, publicImpact, explanation);
    }

    private int computeSafetyRisk(String text, ComplaintCategory category) {
        for (String kw : CRITICAL_HAZARDS) {
            if (text.contains(kw)) return 95;
        }
        for (String kw : HIGH_HAZARDS) {
            if (text.contains(kw)) return 78;
        }
        for (String kw : MEDIUM_HAZARDS) {
            if (text.contains(kw)) return 50;
        }

        if (category == null) return 30;
        return switch (category) {
            case WATERLOGGING, DRAINAGE_PROBLEMS -> 60;
            case ROAD_DAMAGE -> 55;
            case STREETLIGHT_PROBLEMS -> 45;
            case WATER_SUPPLY -> 40;
            case GARBAGE_OVERFLOW -> 35;
            default -> 30;
        };
    }

    private int computeUrgency(String text, ComplaintCategory category) {
        int score = 40;
        if (text.contains("emergency") || text.contains("immediate") || text.contains("danger") || text.contains("urgent")) {
            score += 40;
        }
        if (text.contains("hospital") || text.contains("school") || text.contains("arterial") || text.contains("main road")) {
            score += 20;
        }
        if (text.contains("traffic blocked") || text.contains("impassable") || text.contains("stuck")) {
            score += 25;
        }
        return Math.min(score, 100);
    }

    private int computePublicImpact(String text, int nearbyComplaintsCount) {
        int score = 35;
        if (text.contains("market") || text.contains("station") || text.contains("junction") || text.contains("highway")) {
            score += 30;
        }
        if (text.contains("hundreds") || text.contains("entire locality") || text.contains("all residents")) {
            score += 25;
        }
        score += Math.min(nearbyComplaintsCount * 8, 30);
        return Math.min(score, 100);
    }

    private String generateExplanation(Priority priority, ComplaintCategory category, int safetyRisk, int urgency, int publicImpact, String text) {
        List<String> reasons = new ArrayList<>();
        if (safetyRisk >= 80) {
            reasons.add("Severe safety hazard identified posing imminent risk of injury or structural damage");
        } else if (safetyRisk >= 60) {
            reasons.add("Elevated safety concern creating dangerous transit conditions");
        }

        if (urgency >= 70) {
            reasons.add("High operational urgency affecting emergency transit or vital municipal services");
        }

        if (publicImpact >= 65) {
            reasons.add("Widespread public disruption affecting a high-density transit corridor or vulnerable facility");
        }

        if (reasons.isEmpty()) {
            reasons.add("Routine municipal maintenance request without critical safety escalation factors");
        }

        return String.format("Assigned %s priority (%s SLA). %s. Factors: Safety Risk (%d/100), Urgency (%d/100), Public Impact (%d/100).",
            priority.name(), priority.getSla(), String.join("; ", reasons), safetyRisk, urgency, publicImpact);
    }
}
