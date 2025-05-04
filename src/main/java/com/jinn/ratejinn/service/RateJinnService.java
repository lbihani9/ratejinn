package com.jinn.ratejinn.service;

import com.jinn.ratejinn.adapter.RedisAdapter;
import com.jinn.ratejinn.common.enums.RateJinnErrorCode;
import com.jinn.ratejinn.config.RateJinnConfig;
import com.jinn.ratejinn.config.RateLimitGroupConfig;
import com.jinn.ratejinn.dto.RateLimitRequest;
import com.jinn.ratejinn.dto.RateLimitResponse;
import com.jinn.ratejinn.exception.RateLimitServiceException;
import com.jinn.ratejinn.strategy.RateLimitingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Service
public class RateJinnService {
    private static final Logger logger = LoggerFactory.getLogger(RateJinnService.class);

    @Autowired
    private final Map<String, RateLimitGroupConfig> limiterGroups;

    @Autowired
    private final RedisAdapter redisAdapter;

    @Autowired
    private final Map<String, RateLimitingStrategy> strategies;

    public RateJinnService(Map<String, RateLimitGroupConfig> limiterGroups, RedisAdapter redisAdapter, Map<String, RateLimitingStrategy> strategies) {
        this.redisAdapter = redisAdapter;
        this.strategies = strategies;
        this.limiterGroups = limiterGroups;
    }

    public CompletableFuture<RateLimitResponse> shouldRateLimit(RateLimitRequest request) {
        String userId = request.getUserId();
        String group = request.getGroup();

        RateLimitGroupConfig groupConfig = this.limiterGroups.get(group);
        if (groupConfig == null) {
            throw new RateLimitServiceException(RateJinnErrorCode.INVALID_GROUP, "Rate limiting config missing for group: " + group);
        }

        String strategy = groupConfig.getRateLimit().getStrategy();
        logger.debug("Group: {}", group);
        logger.debug("{}", strategies.get(strategy));
        CompletionStage<RateLimitResponse> rateLimitResponse = strategies.get(strategy).checkLimit(userId, group, groupConfig, redisAdapter);

        return rateLimitResponse.exceptionally(ex -> {
            logger.error("Rate limiting check failed", ex);
            throw new RateLimitServiceException(RateJinnErrorCode.INTERNAL_ERROR, ex.getMessage());
        })
        .toCompletableFuture();
    }
}
