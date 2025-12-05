package com.mfhe.controller;

import com.mfhe.service.StatCanFetcherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final StatCanFetcherService statCanFetcher;

    public AdminController(StatCanFetcherService statCanFetcher) {
        this.statCanFetcher = statCanFetcher;
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshData() {
        String result = statCanFetcher.refreshData();
        return ResponseEntity.ok(Map.of("status", result));
    }
}
