package com.haitao55.spider.crawler.common.cache;

import java.util.Collection;

/**
 * 
 * 功能：程序内部局部缓存
 * 
 * @author Arthur.Liu
 * @time 2016年5月31日 下午3:47:34
 * @version 1.0
 */
public interface Cache<K, V> {

    public void put(K key, V value);

    public V get(K key);

    public void remove(K key);

    public Collection<K> keys();

}