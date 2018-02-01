package com.haitao55.spider.crawler.common.cache;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;

/**
 * 
 * 功能：一个简单的内存cache实现
 * 
 * @author Arthur.Liu
 * @time 2016年5月31日 下午4:05:10
 * @version 1.0
 */
public class SimpleCache<K, V> implements Cache<K, V> {
    private com.google.common.cache.Cache<K, V> cache = CacheBuilder
            .newBuilder().expireAfterWrite(1, TimeUnit.DAYS).build();

    public void put(K key, V value) {
        cache.put(key, value);
    }

    public V get(K key) {
        return cache.getIfPresent(key);
    }

    public void remove(K key) {
        cache.invalidate(key);
    }

    public Collection<K> keys() {
        return cache.asMap().keySet();
    }
}