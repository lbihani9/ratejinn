package com.jinn.ratejinn.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@ConfigurationProperties(prefix="ratejinn")
@Data
@Validated
public class RateJinnConfig {
    private static final Logger logger = LoggerFactory.getLogger(RateJinnConfig.class);

    @Valid
    @NotNull
    @NestedConfigurationProperty
    private RedisConfig redis;

    @NotNull
    @NestedConfigurationProperty
    private AdapterConfig adapter;

    @NotEmpty
    private Map<String, RateLimitGroupConfig> limiter;

    public RateLimitGroupConfig getRateLimitGroup(String group) {
        return limiter.get(group);
    }

    @PostConstruct
    public void logConfig() {
        logger.debug("RateJinn Configuration loaded: {}", this);
        logger.debug("Redis mode: {}", redis.getMode());
        logger.debug("Redis standalone: {}", redis.getStandalone());
        logger.debug("Redis cluster: {}", redis.getCluster());
        logger.debug("Limiter groups: {}", limiter.keySet());
    }
}
