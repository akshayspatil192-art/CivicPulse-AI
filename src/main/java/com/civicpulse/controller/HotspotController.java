package com.civicpulse.controller;

import com.civicpulse.dto.HotspotClusterDto;
import com.civicpulse.service.HotspotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotspots")
public class HotspotController {

    private final HotspotService hotspotService;

    public HotspotController(HotspotService hotspotService) {
        this.hotspotService = hotspotService;
    }

    @GetMapping
    public ResponseEntity<List<HotspotClusterDto>> getHotspots() {
        return ResponseEntity.ok(hotspotService.getHotspots());
    }
}
