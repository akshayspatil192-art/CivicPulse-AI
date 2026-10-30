package com.civicpulse.ai;

import com.civicpulse.dto.AiAnalysisResult;
import com.civicpulse.entity.ComplaintCategory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.contains("PLACEHOLDER");
    }

    public Optional<AiAnalysisResult> analyzeComplaint(String title, String description, ComplaintCategory category) {
        if (!isConfigured()) {
            return Optional.empty();
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey.trim();

            String prompt = "Analyze this civic municipal complaint:\n" +
                "Title: " + title + "\n" +
                "Description: " + description + "\n" +
                "Selected Category: " + (category != null ? category.name() : "UNSPECIFIED") + "\n" +
                "Return ONLY a valid JSON object with schema: {\"category\": \"ROAD_DAMAGE\"|\"WATERLOGGING\"|\"GARBAGE_OVERFLOW\"|\"DRAINAGE_PROBLEMS\"|\"STREETLIGHT_PROBLEMS\"|\"WATER_SUPPLY\"|\"OTHER\", \"summary\": \"1 sentence summary\", \"actionBrief\": \"1 sentence municipal action brief\", \"safetyRisk\": 0-100, \"urgency\": 0-100, \"publicImpact\": 0-100, \"explanation\": \"plain language priority explanation\"}";

            Map<String, Object> part = Map.of("text", prompt);
            Map<String, Object> contentObj = Map.of("parts", List.of(part));
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(contentObj),
                "generationConfig", Map.of("temperature", 0.2, "responseMimeType", "application/json")
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    String text = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
                    JsonNode data = objectMapper.readTree(text);

                    AiAnalysisResult result = new AiAnalysisResult();
                    String catStr = data.path("category").asText();
                    try {
                        result.setDetectedCategory(ComplaintCategory.valueOf(catStr));
                    } catch (Exception e) {
                        result.setDetectedCategory(category != null ? category : ComplaintCategory.OTHER);
                    }
                    result.setSummary(data.path("summary").asText());
                    result.setActionBrief(data.path("actionBrief").asText());
                    result.setSafetyRiskFactor(data.path("safetyRisk").asInt(50));
                    result.setUrgencyFactor(data.path("urgency").asInt(50));
                    result.setPublicImpactFactor(data.path("publicImpact").asInt(50));
                    result.setExplanation(data.path("explanation").asText());
                    result.setGeminiPowered(true);

                    return Optional.of(result);
                }
            }
        } catch (Exception ex) {
            log.warn("Gemini API call failed or timed out; seamlessly failing over to deterministic rule-based engine: {}", ex.getMessage());
        }
        return Optional.empty();
    }
}
