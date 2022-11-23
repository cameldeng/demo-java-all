package com.aws.demo.redis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RedisClusterDemoImplTest {

    @Test
    void upsertCacheEntry() {
        String key = "key_001";
        String value = "Carlos_001";

        IRedisClusterDemo redisClusterDemo = new RedisClusterDemoImpl();
        redisClusterDemo.upsertCacheEntry(key,value,true);
    }

    @Test
    void getCacheValue() {
    }
}