package com.jinn.ratejinn.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RateLimitRequest {
    @NotBlank(message = "userId must not be blank")
    private String userId;

    @NotBlank(message = "group must not be blank")
    private String group;

    public RateLimitRequest() {}

    public RateLimitRequest(String userId) {
        this(userId, "global");
    }

    public RateLimitRequest(String userId, String group) {
        this.userId = userId;
        this.group = group;
    }
}
