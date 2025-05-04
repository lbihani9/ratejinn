package com.jinn.ratejinn.adapter;

import com.jinn.ratejinn.config.AdapterConfig;
import com.jinn.ratejinn.config.RedisConfig;
import com.jinn.ratejinn.strategy.ClusterRedisExecutor;
import com.jinn.ratejinn.strategy.RedisExecutor;
import com.jinn.ratejinn.strategy.StandaloneRedisExecutor;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RedisAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RedisAdapter.class);

    @Autowired
    private final AdapterConfig adapterConfig;

    @Autowired
    private final RedisConfig redisConfig;

    @Getter
    private final RedisExecutor redisExecutor;

    public RedisAdapter(AdapterConfig adapterConfig, RedisConfig redisConfig) {
        this.adapterConfig = adapterConfig;
        this.redisConfig = redisConfig;

        if ("standalone".equalsIgnoreCase(redisConfig.getMode())) {
            this.redisExecutor = new StandaloneRedisExecutor(adapterConfig, redisConfig);
        } else {
            this.redisExecutor = new ClusterRedisExecutor(adapterConfig, redisConfig);
        }
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down redis client(s)...");
        this.redisExecutor.shutdown();
    }
}
