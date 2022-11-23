package com.aws.demo.redis;

public interface IRedisClusterDemo {

    /**
     * 更新缓存条目
     * @param key 缓存 key
     * @param value 缓存 value
     * @param checkExists 是否检查已存在
     */
    void upsertCacheEntry(String key, String value, boolean checkExists);

    /**
     * 获取缓存条目
     * @param key 缓存 key
     * @return 缓存 value
     */
    String getCacheValue(String key);
}
