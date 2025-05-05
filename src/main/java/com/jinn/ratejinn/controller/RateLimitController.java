package com.jinn.ratejinn.controller;

import com.jinn.ratejinn.dto.RateLimitRequest;
import com.jinn.ratejinn.dto.RateLimitResponse;
import com.jinn.ratejinn.service.RateJinnService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/check")
public class RateLimitController {
    private static final Logger logger = LoggerFactory.getLogger(RateLimitController.class);
    @Autowired
    private final RateJinnService rateJinnService;

    public RateLimitController(RateJinnService rateJinnService) {
        this.rateJinnService = rateJinnService;
    }

    @PostMapping
    public ResponseEntity<RateLimitResponse> check(@Valid @RequestBody RateLimitRequest request) throws ExecutionException, InterruptedException {
        RateLimitResponse response = rateJinnService.shouldRateLimit(request).get();
        return ResponseEntity.ok(response);
    }
}
