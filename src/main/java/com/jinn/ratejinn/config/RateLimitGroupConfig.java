package com.jinn.ratejinn.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RateLimitGroupConfig {
    @Valid
    private RateLimitConfig rateLimit;

    @Data
    public static class RateLimitConfig {
        @NotBlank
        private String strategy;

        @NotBlank
        private String unit;

        @Min(1)
        private int bucketCapacity;

        @Min(1)
        private int tokenConsumptionPerRequest;

        private int refillRate;
    }
}
