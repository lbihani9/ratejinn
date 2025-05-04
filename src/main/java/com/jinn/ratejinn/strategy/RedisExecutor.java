package com.jinn.ratejinn.strategy;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScriptOutputType;


public interface RedisExecutor {
    <T> RedisFuture<T> eval(String script, ScriptOutputType outputType, String[] keys, String[] args);

    void shutdown();
}
