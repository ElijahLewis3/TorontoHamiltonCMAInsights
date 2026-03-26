package com.mfhe.controller;

import com.mfhe.service.StatCanFetcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final StatCanFetcherService statCanFetcher;

    @Value("${app.admin.api-key:}")
    private String adminApiKey;

    public AdminController(StatCanFetcherService statCanFetcher) {
        this.statCanFetcher = statCanFetcher;
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshData(
            @RequestHeader(value = "X-API-Key", required = false) String providedKey) {

        if (!adminApiKey.isBlank()) {
            if (providedKey == null || !providedKey.equals(adminApiKey)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("status", "Forbidden"));
            }
        }

        try {
            String result = statCanFetcher.refreshData();
            return ResponseEntity.ok(Map.of("status", result));
        } catch (Exception e) {
            log.error("Data refresh failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "Data refresh failed. Check server logs."));
        }
    }
}
