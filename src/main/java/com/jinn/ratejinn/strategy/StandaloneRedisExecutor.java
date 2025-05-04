package com.jinn.ratejinn.strategy;

import com.jinn.ratejinn.config.AdapterConfig;
import com.jinn.ratejinn.config.RedisConfig;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.Getter;

@Getter
public class StandaloneRedisExecutor implements RedisExecutor {

    private final RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;

    public StandaloneRedisExecutor(AdapterConfig adapterConfig, RedisConfig redisConfig) {
        this.redisClient = RedisClient.create(RedisURI.builder()
                .withHost(redisConfig.getStandalone().getHost())
                .withPort(redisConfig.getStandalone().getPort())
                .build()
        );
        this.connection = redisClient.connect();
    }

    @Override
    public <T> RedisFuture<T> eval(String script, ScriptOutputType outputType, String[] keys, String[] args) {
        return this.connection.async().eval(script, outputType, keys, args);
    }

    @Override
    public void shutdown() {
        this.connection.close();
        this.redisClient.shutdown();
    }
}
