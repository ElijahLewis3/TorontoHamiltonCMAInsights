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
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private static final Pattern REGION_CODE_PATTERN = Pattern.compile("^[0-9]{1,5}$");

    private final InsightsService insightsService;

    public InsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping("/combined/{code}")
    public ResponseEntity<CombinedTimeSeries> getCombinedTimeSeries(@PathVariable String code) {
        if (!isValidCode(code)) return ResponseEntity.badRequest().build();
        return insightsService.getCombinedTimeSeries(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employment/naics/{code}")
    public ResponseEntity<List<NaicsShare>> getNaicsBreakdown(@PathVariable String code) {
        if (!isValidCode(code)) return ResponseEntity.badRequest().build();
        return insightsService.getNaicsBreakdown(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/housing/categories/{code}")
    public ResponseEntity<List<HousingCategorySummary>> getHousingCategories(@PathVariable String code) {
        if (!isValidCode(code)) return ResponseEntity.badRequest().build();
        return insightsService.getHousingCategories(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/housing/heatmap/{code}")
    public ResponseEntity<List<HeatmapCell>> getHeatmapData(@PathVariable String code) {
        if (!isValidCode(code)) return ResponseEntity.badRequest().build();
        return insightsService.getHeatmapData(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private static boolean isValidCode(String code) {
        return code != null && REGION_CODE_PATTERN.matcher(code).matches();
    }
}
