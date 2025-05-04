package com.jinn.ratejinn.dto;

import lombok.Data;

@Data
public class RateLimitResponse {
    private RateLimitMetadata rateLimit;
    private boolean allowed;

    public RateLimitResponse() {}

    public RateLimitResponse(RateLimitMetadata rateLimit, boolean allowed) {
        this.rateLimit = rateLimit;
        this.allowed = allowed;
    }
}
