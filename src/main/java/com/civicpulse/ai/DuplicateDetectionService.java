package com.civicpulse.ai;

import com.civicpulse.dto.DuplicateCheckResult;
import com.civicpulse.entity.Complaint;
import com.civicpulse.entity.ComplaintCategory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DuplicateDetectionService {

    private static final double MAX_DISTANCE_METERS = 250.0;
    private static final double SIMILARITY_THRESHOLD = 0.45;

    public DuplicateCheckResult checkForDuplicate(
            String title,
            String description,
            ComplaintCategory category,
            Double latitude,
            Double longitude,
            List<Complaint> existingComplaints) {

        if (latitude == null || longitude == null || existingComplaints == null || existingComplaints.isEmpty()) {
            return new DuplicateCheckResult(false, null, null, null, 0.0, 0.0, "No existing complaints to compare against.");
        }

        String inputTokens = normalizeAndTokenize(title + " " + description);

        Complaint bestMatch = null;
        double minDistance = Double.MAX_VALUE;
        double maxSimilarity = 0.0;

        for (Complaint existing : existingComplaints) {
            if (existing.getLatitude() == null || existing.getLongitude() == null) continue;

            double distance = calculateDistanceInMeters(
                latitude, longitude,
                existing.getLatitude(), existing.getLongitude()
            );

            if (distance <= MAX_DISTANCE_METERS) {
                String existingTokens = normalizeAndTokenize(existing.getTitle() + " " + existing.getDescription());
                double similarity = calculateJaccardSimilarity(inputTokens, existingTokens);

                boolean categoryMatch = (category != null && category == existing.getCategory())
                    || areRelatedCategories(category, existing.getCategory());

                if (categoryMatch) {
                    similarity += 0.20;
                }

                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    minDistance = distance;
                    bestMatch = existing;
                }
            }
        }

        if (bestMatch != null && (maxSimilarity >= SIMILARITY_THRESHOLD || (minDistance < 60.0 && bestMatch.getCategory() == category))) {
            String reasoning = String.format(
                "Potential duplicate detected of %s ('%s'). Located %.1fm away with %.0f%% semantic/category correlation.",
                bestMatch.getTrackingId(), bestMatch.getTitle(), minDistance, Math.min(maxSimilarity * 100, 99.0)
            );
            return new DuplicateCheckResult(
                true,
                bestMatch.getId(),
                bestMatch.getTrackingId(),
                bestMatch.getTitle(),
                Math.round(minDistance * 10.0) / 10.0,
                Math.round(maxSimilarity * 100.0) / 100.0,
                reasoning
            );
        }

        return new DuplicateCheckResult(false, null, null, null, 0.0, 0.0, "Unique report. No conflicting complaints found within 250m radius.");
    }

    private boolean areRelatedCategories(ComplaintCategory c1, ComplaintCategory c2) {
        if (c1 == null || c2 == null) return false;
        if ((c1 == ComplaintCategory.WATERLOGGING && c2 == ComplaintCategory.DRAINAGE_PROBLEMS) ||
            (c1 == ComplaintCategory.DRAINAGE_PROBLEMS && c2 == ComplaintCategory.WATERLOGGING)) {
            return true;
        }
        return false;
    }

    public static double calculateDistanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private String normalizeAndTokenize(String text) {
        if (text == null) return "";
        return text.toLowerCase()
            .replaceAll("[^a-z0-9\s]", " ")
            .replaceAll("\s+", " ")
            .trim();
    }

    private double calculateJaccardSimilarity(String text1, String text2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(text1.split(" ")));
        Set<String> set2 = new HashSet<>(Arrays.asList(text2.split(" ")));

        Set<String> stopWords = Set.of("the", "is", "at", "which", "on", "and", "a", "an", "in", "to", "of", "for", "with", "near", "road", "street");
        set1.removeAll(stopWords);
        set2.removeAll(stopWords);

        if (set1.isEmpty() || set2.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }
}
