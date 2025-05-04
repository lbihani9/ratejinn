package com.jinn.ratejinn.strategy;

import com.jinn.ratejinn.config.AdapterConfig;
import com.jinn.ratejinn.config.RedisConfig;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import lombok.Getter;

import java.util.List;

@Getter
public class ClusterRedisExecutor implements RedisExecutor {

    private final RedisClusterClient redisClusterClient;
    private StatefulRedisClusterConnection<String, String> connection;

    public ClusterRedisExecutor(AdapterConfig adapterConfig, RedisConfig redisConfig) {
        List<RedisURI> redisURIS = redisConfig.getCluster().getNodes().stream()
                .map(node -> RedisURI.builder()
                        .withHost(node.getHost())
                        .withPort(node.getPort())
                        .build()
                )
                .toList();

        this.redisClusterClient = RedisClusterClient.create(redisURIS);
        this.connection = this.redisClusterClient.connect();
    }

    @Override
    public <T> RedisFuture<T> eval(String script, ScriptOutputType outputType, String[] keys, String[] args) {
        return this.connection.async().eval(script, outputType, keys, args);
    }

    @Override
    public void shutdown() {
        this.connection.close();
        this.redisClusterClient.shutdown();
    }
}
