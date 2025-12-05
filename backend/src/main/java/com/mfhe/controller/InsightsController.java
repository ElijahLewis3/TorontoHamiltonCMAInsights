package com.mfhe.controller;

import com.mfhe.dto.CombinedTimeSeries;
import com.mfhe.dto.HeatmapCell;
import com.mfhe.dto.HousingCategorySummary;
import com.mfhe.dto.NaicsShare;
import com.mfhe.service.InsightsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final InsightsService insightsService;

    public InsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping("/combined/{code}")
    public ResponseEntity<CombinedTimeSeries> getCombinedTimeSeries(@PathVariable String code) {
        return insightsService.getCombinedTimeSeries(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employment/naics/{code}")
    public ResponseEntity<List<NaicsShare>> getNaicsBreakdown(@PathVariable String code) {
        return insightsService.getNaicsBreakdown(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/housing/categories/{code}")
    public ResponseEntity<List<HousingCategorySummary>> getHousingCategories(@PathVariable String code) {
        return insightsService.getHousingCategories(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/housing/heatmap/{code}")
    public ResponseEntity<List<HeatmapCell>> getHeatmapData(@PathVariable String code) {
        return insightsService.getHeatmapData(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
