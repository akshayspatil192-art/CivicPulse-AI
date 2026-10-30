package com.civicpulse.service;

import com.civicpulse.ai.DuplicateDetectionService;
import com.civicpulse.ai.PriorityScoringEngine;
import com.civicpulse.ai.RuleBasedAiEngine;
import com.civicpulse.entity.ComplaintCategory;
import com.civicpulse.entity.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiProcessingServiceTest {

    private PriorityScoringEngine priorityEngine;
    private RuleBasedAiEngine ruleBasedEngine;

    @BeforeEach
    void setUp() {
        priorityEngine = new PriorityScoringEngine();
        ruleBasedEngine = new RuleBasedAiEngine(priorityEngine);
    }

    @Test
    void testAutomaticCategorization_Waterlogging() {
        String title = "Severe water accumulation";
        String description = "Heavy rainwater is accumulated on the main road. Vehicles cannot pass properly.";
        ComplaintCategory detected = ruleBasedEngine.detectCategory((title + " " + description).toLowerCase());
        assertEquals(ComplaintCategory.WATERLOGGING, detected);
    }

    @Test
    void testAutomaticCategorization_RoadDamage() {
        String title = "Deep crater on highway";
        String description = "Huge pothole and broken asphalt causing accidents.";
        ComplaintCategory detected = ruleBasedEngine.detectCategory((title + " " + description).toLowerCase());
        assertEquals(ComplaintCategory.ROAD_DAMAGE, detected);
    }

    @Test
    void testAutomaticCategorization_LiveWire() {
        String title = "Broken streetlight";
        String description = "Street light bulb fused and dark alley.";
        ComplaintCategory detected = ruleBasedEngine.detectCategory((title + " " + description).toLowerCase());
        assertEquals(ComplaintCategory.STREETLIGHT_PROBLEMS, detected);
    }

    @Test
    void testCriticalPriorityScoring_LiveWire() {
        var result = priorityEngine.calculate(
            ComplaintCategory.STREETLIGHT_PROBLEMS,
            "Live wire dangling near primary school",
            "A sparking live wire snapped and fell on the school gate. Immediate danger of electrocution.",
            5
        );
        assertEquals(Priority.CRITICAL, result.priority());
        assertTrue(result.safetyRisk() >= 85);
        assertTrue(result.explanation().contains("CRITICAL"));
    }

    @Test
    void testHighPriorityScoring_WaterloggingOnMainRoad() {
        var result = priorityEngine.calculate(
            ComplaintCategory.WATERLOGGING,
            "Severe water accumulation blocking arterial road",
            "Heavy rainwater logging near hospital entrance. Traffic blocked, vehicles cannot pass properly.",
            4
        );
        assertTrue(result.priority() == Priority.HIGH || result.priority() == Priority.CRITICAL);
        assertTrue(result.urgency() >= 60);
    }

    @Test
    void testDistanceCalculation() {
        // MG Road Metro vs 35m away
        double d = DuplicateDetectionService.calculateDistanceInMeters(12.9716, 77.5946, 12.9719, 77.5948);
        assertTrue(d > 0 && d < 60, "Distance should be under 60 meters");
    }
}
