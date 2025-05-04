package com.jinn.ratejinn.exception;

import com.jinn.ratejinn.common.enums.RateJinnErrorCode;
import lombok.Data;

@Data
public class RateLimitServiceException extends RuntimeException {
    private RateJinnErrorCode code;

    public RateLimitServiceException(RateJinnErrorCode code, String message) {
        super(message);
        this.code = code;
    }
}

