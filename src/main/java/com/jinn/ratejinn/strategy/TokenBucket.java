package com.jinn.ratejinn.strategy;

import com.jinn.ratejinn.adapter.RedisAdapter;
import com.jinn.ratejinn.common.enums.RateJinnErrorCode;
import com.jinn.ratejinn.config.RateLimitGroupConfig;
import com.jinn.ratejinn.dto.RateLimitMetadata;
import com.jinn.ratejinn.dto.RateLimitResponse;
import com.jinn.ratejinn.exception.RateLimitServiceException;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScriptOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component("token-bucket")
public class TokenBucket implements RateLimitingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(TokenBucket.class);

    private final String luaScript = """
            local user_key = KEYS[1]
            local current_time = tonumber(ARGV[1])
            local max_tokens = tonumber(ARGV[2])
            local refill_rate = tonumber(ARGV[3])
            local refill_time = tonumber(ARGV[4])
            local tokens_per_request = tonumber(ARGV[5])
            
            -- Current state
            local state = redis.call('HMGET', user_key, 'tokens', 'last_request_time')
            local tokens = state[1] and tonumber(state[1]) or max_tokens
            local last_request_time = state[2] and tonumber(state[2]) or current_time
            
            -- New tokens calculation
            local elapsed = current_time - last_request_time
            local gain = (elapsed * refill_rate) / refill_time
            local new_tokens = math.min(max_tokens, tokens + gain)

            if new_tokens >= tokens_per_request then
                new_tokens = new_tokens - tokens_per_request
                redis.call('HMSET', user_key, 'tokens', new_tokens, 'last_request_time', current_time)
                redis.call('EXPIRE', user_key, 3600)
               
                return { 1, new_tokens, 0 }
            else
                redis.call('HMSET', user_key, 'tokens', new_tokens, 'last_request_time', current_time)
                redis.call('EXPIRE', user_key, 3600)
                
                local per_sec = refill_rate / refill_time
                local retry_after = math.max(0, (tokens_per_request - new_tokens) / per_sec)
                return { 0, new_tokens, math.ceil(retry_after) }
            end
    """;

    @Override
    public CompletableFuture<RateLimitResponse> checkLimit(String userId, String group, RateLimitGroupConfig groupConfig, RedisAdapter adapter) {
        long currentTime = System.currentTimeMillis() / 1000;
        int limit = groupConfig.getRateLimit().getBucketCapacity();

        String[] keys = {
            getKey(userId, group)
        };

        String[] args = {
            String.valueOf(currentTime),
            String.valueOf(limit),
            String.valueOf(groupConfig.getRateLimit().getRefillRate()),
            String.valueOf(groupConfig.getRateLimit().getUnit()),
            String.valueOf(groupConfig.getRateLimit().getTokenConsumptionPerRequest()),
        };

        RedisFuture<List<Long>> resultFuture = adapter.getRedisExecutor().eval(this.luaScript, ScriptOutputType.MULTI, keys, args);

        return resultFuture.toCompletableFuture().thenApply(result -> {
            boolean allowed = result.get(0) == 1;
            int remaining = Math.toIntExact(result.get(1));
            long retryAfter = result.get(2);

            return new RateLimitResponse(
                new RateLimitMetadata(remaining, limit, retryAfter),
                allowed
            );
        }).exceptionally(err -> {
            logger.error("[Token Bucket] Failed to check rate limit", err);
            throw new RateLimitServiceException(RateJinnErrorCode.REDIS_ERROR, err.getMessage());
        });
    }
}
