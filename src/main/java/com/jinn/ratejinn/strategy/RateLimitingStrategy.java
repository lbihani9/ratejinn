package com.jinn.ratejinn.strategy;

import com.jinn.ratejinn.adapter.RedisAdapter;
import com.jinn.ratejinn.config.RateLimitGroupConfig;
import com.jinn.ratejinn.dto.RateLimitResponse;

import java.util.concurrent.CompletableFuture;

public interface RateLimitingStrategy {
     default String getKey(String userId, String group) {
        return "rate_limit_" + group + "_user_" + userId;
    }

    CompletableFuture<RateLimitResponse> checkLimit(String userId, String group, RateLimitGroupConfig groupConfig, RedisAdapter adapter);
}
