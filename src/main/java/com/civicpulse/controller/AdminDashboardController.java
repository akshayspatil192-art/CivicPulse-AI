package com.civicpulse.controller;

import com.civicpulse.dto.DashboardMetricsDto;
import com.civicpulse.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsDto> getMetrics() {
        return ResponseEntity.ok(dashboardService.getMetrics());
    }
}
