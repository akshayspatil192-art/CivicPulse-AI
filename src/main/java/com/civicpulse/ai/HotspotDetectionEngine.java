package com.civicpulse.ai;

import com.civicpulse.dto.ComplaintResponse;
import com.civicpulse.dto.HotspotClusterDto;
import com.civicpulse.entity.Complaint;
import com.civicpulse.entity.ComplaintCategory;
import com.civicpulse.entity.Priority;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class HotspotDetectionEngine {

    private static final double CLUSTER_RADIUS_METERS = 350.0;
    private static final int MIN_CLUSTER_SIZE = 2;

    public List<HotspotClusterDto> detectHotspots(List<Complaint> complaints, java.util.function.Function<Complaint, ComplaintResponse> mapper) {
        if (complaints == null || complaints.isEmpty()) {
            return Collections.emptyList();
        }

        List<Complaint> active = complaints.stream()
            .filter(c -> c.getLatitude() != null && c.getLongitude() != null)
            .toList();

        boolean[] visited = new boolean[active.size()];
        List<List<Complaint>> clusters = new ArrayList<>();

        for (int i = 0; i < active.size(); i++) {
            if (visited[i]) continue;

            List<Complaint> cluster = new ArrayList<>();
            cluster.add(active.get(i));
            visited[i] = true;

            for (int j = i + 1; j < active.size(); j++) {
                if (visited[j]) continue;

                double dist = DuplicateDetectionService.calculateDistanceInMeters(
                    active.get(i).getLatitude(), active.get(i).getLongitude(),
                    active.get(j).getLatitude(), active.get(j).getLongitude()
                );

                if (dist <= CLUSTER_RADIUS_METERS) {
                    cluster.add(active.get(j));
                    visited[j] = true;
                }
            }

            if (cluster.size() >= MIN_CLUSTER_SIZE) {
                clusters.add(cluster);
            }
        }

        List<HotspotClusterDto> resultList = new ArrayList<>();
        int clusterIdx = 1;

        for (List<Complaint> group : clusters) {
            double sumLat = 0;
            double sumLng = 0;
            Map<String, Integer> catCounts = new HashMap<>();
            boolean hasCritical = false;
            boolean hasHigh = false;

            for (Complaint c : group) {
                sumLat += c.getLatitude();
                sumLng += c.getLongitude();
                catCounts.put(c.getCategory().name(), catCounts.getOrDefault(c.getCategory().name(), 0) + 1);
                if (c.getPriority() == Priority.CRITICAL) hasCritical = true;
                if (c.getPriority() == Priority.HIGH) hasHigh = true;
            }

            double centerLat = sumLat / group.size();
            double centerLng = sumLng / group.size();

            String dominant = catCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("CIVIC_HAZARD");

            String severity = hasCritical ? "CRITICAL" : (hasHigh ? "HIGH" : "MEDIUM");

            boolean hasDrainage = catCounts.containsKey("DRAINAGE_PROBLEMS");
            boolean hasWaterlog = catCounts.containsKey("WATERLOGGING");
            boolean hasRoad = catCounts.containsKey("ROAD_DAMAGE");

            String compoundDesc;
            if (hasDrainage && hasWaterlog && hasRoad) {
                compoundDesc = "Compound Infrastructure Failure: Blocked arterial drainage causing localized flooding and progressive road asphalt collapse.";
            } else if (hasDrainage && hasWaterlog) {
                compoundDesc = "Hydraulic Drainage Choke: Stormwater backing up due to clogged trunk sewer system.";
            } else if (hasRoad && hasWaterlog) {
                compoundDesc = "Flood-Induced Road Hazard: Standing water concealing deep craters and weakening sub-base.";
            } else {
                compoundDesc = String.format("Recurring %s Cluster: High concentration of %d reports within %.0fm radius requiring coordinated municipal intervention.",
                    dominant.replace('_', ' '), group.size(), CLUSTER_RADIUS_METERS);
            }

            HotspotClusterDto dto = new HotspotClusterDto();
            dto.setClusterId("HOTSPOT-" + clusterIdx);
            dto.setName("Hotspot Area " + clusterIdx + " (" + group.get(0).getAddress().split(",")[0] + ")");
            dto.setCenterLatitude(centerLat);
            dto.setCenterLongitude(centerLng);
            dto.setRadiusMeters(CLUSTER_RADIUS_METERS);
            dto.setComplaintCount(group.size());
            dto.setDominantCategory(dominant);
            dto.setSeverityLevel(severity);
            dto.setCompoundRiskDescription(compoundDesc);
            dto.setCategoryBreakdown(catCounts);
            dto.setComplaints(group.stream().map(mapper).toList());

            resultList.add(dto);
            clusterIdx++;
        }

        resultList.sort((a, b) -> Integer.compare(b.getComplaintCount(), a.getComplaintCount()));

        return resultList;
    }
}
