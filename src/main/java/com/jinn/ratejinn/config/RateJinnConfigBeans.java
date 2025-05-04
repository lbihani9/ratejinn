package com.jinn.ratejinn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RateJinnConfigBeans {

    @Bean
    public RedisConfig redisConfig(RateJinnConfig config) {
        return config.getRedis();
    }

    @Bean
    public AdapterConfig adapterConfig(RateJinnConfig config) {
        return config.getAdapter();
    }

    @Bean
    public Map<String, RateLimitGroupConfig> limiterGroups(RateJinnConfig config) {
        return config.getLimiter();
    }
}

