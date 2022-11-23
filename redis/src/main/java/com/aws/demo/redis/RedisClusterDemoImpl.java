package com.aws.demo.redis;

import com.aws.demo.redis.IRedisClusterDemo;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.SetParams;

public class RedisClusterDemoImpl implements IRedisClusterDemo {
    private final static String REDIS_CLUSTER_ENDPOINT_HOSTNAME = "test-redis.lfucw7.ng.0001.use1.cache.amazonaws.com";
    private final static int REDIS_CLUSTER_ENDPOINT_PORT = 6379;
    private final static int CLIENT_TIMEOUT_IN_SECS = 10;
    private final static int CACHE_EXPIRY_IN_SECS = 60;
    private static Jedis jedis = null;
    private static JedisCluster jedisCluster = null;

    static {
        jedis = new Jedis(REDIS_CLUSTER_ENDPOINT_HOSTNAME, REDIS_CLUSTER_ENDPOINT_PORT, CLIENT_TIMEOUT_IN_SECS);
        jedisCluster = new JedisCluster(new HostAndPort(REDIS_CLUSTER_ENDPOINT_HOSTNAME, REDIS_CLUSTER_ENDPOINT_PORT), CLIENT_TIMEOUT_IN_SECS);
    }

    @Override
    public void upsertCacheEntry(String key, String value, boolean checkExists) {
        boolean valueExists = false;
        if (checkExists && (getCacheValue(key) != null)) {
            valueExists = true;
        }
        String result = jedisCluster.set(key, value, (new SetParams()).ex(CACHE_EXPIRY_IN_SECS));
        if (result.equalsIgnoreCase("OK")) {
            if (checkExists) {
                if (valueExists) {
                    System.out.println("Updated = {key=" + key + ", value=" + value + "}");
                } else {
                    System.out.println("Inserted = {key=" + key + ", value=" + value + "}");
                }
            } else {
                System.out.println("Upserted = {key=" + key + ", value=" + value + "}");
            }
        } else {
            System.out.println("Could not upsert key '" + key + "'");
        }
    }

    @Override
    public String getCacheValue(String key) {
        String value = jedis.get(key);
        String value1 = jedisCluster.get(key);
        System.out.println(String.format("jedis client: %s cached value = %s \n", key, value));
        System.out.println(String.format("jedisCluster client: %s cached value = %s", key, value1));
        return  value;
    }
}
