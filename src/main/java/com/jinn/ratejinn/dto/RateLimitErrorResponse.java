package com.jinn.ratejinn.dto;

import com.jinn.ratejinn.common.enums.RateJinnErrorCode;
import lombok.Data;

import java.time.Instant;

@Data
public class RateLimitErrorResponse {
    private final RateJinnErrorCode code;
    private final String message;
    private final Instant timestamp;

    public RateLimitErrorResponse(RateJinnErrorCode code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = Instant.now();
    }
}
