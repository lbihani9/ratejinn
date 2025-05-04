package com.jinn.ratejinn.dto;

import lombok.Data;

@Data
public class RateLimitMetadata {
    private int remaining;
    private long retryAfter;
    private int limit;

    public RateLimitMetadata() {}

    public RateLimitMetadata(int remaining, int limit, long retryAfter) {
        this.remaining = remaining;
        this.limit = limit;
        this.retryAfter = retryAfter;
    }
}
