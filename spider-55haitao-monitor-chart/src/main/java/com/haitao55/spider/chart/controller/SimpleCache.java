package com.haitao55.spider.chart.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SimpleCache<T> {
	private final Map<String, T> m_objects;
	private final Map<String, Long> m_expiredObjects;
	private final long m_lExpireTime;
	private final ExecutorService m_executor;

	public SimpleCache() {
		this(500);
	}

	public SimpleCache(final int nExpireTime) {
		m_objects = Collections.synchronizedMap(new HashMap<String, T>());
		m_expiredObjects = Collections.synchronizedMap(new HashMap<String, Long>());
		m_lExpireTime = nExpireTime;
		m_executor = Executors.newFixedThreadPool(256);
		Executors.newScheduledThreadPool(5).scheduleWithFixedDelay(RemoveExpiredObjects(), m_lExpireTime / 2,
				m_lExpireTime, TimeUnit.SECONDS);
	}

	private final Runnable RemoveExpiredObjects() {
		return new Runnable() {
			public void run() {
				for (final String name : m_expiredObjects.keySet()) {
					if (System.currentTimeMillis() > m_expiredObjects.get(name)) {
						m_executor.execute(CreateRemoveRunnable(name));
					}
				}
			}
		};
	}

	private final Runnable CreateRemoveRunnable(final String name) {
		return new Runnable() {
			public void run() {
				m_objects.remove(name);
				m_expiredObjects.remove(name);
			}
		};
	}

	public long GetExpireTime() {
		return m_lExpireTime;
	}

	public void Put(final String name, final T obj) {
		Put(name, obj, m_lExpireTime);
	}

	public void Put(final String name, final T obj, final long expireTime) {
		m_objects.put(name, obj);
		m_expiredObjects.put(name, System.currentTimeMillis() + expireTime * 1000);
	}

	public T Get(final String name) {
		final Long expireTime = m_expiredObjects.get(name);
		if (expireTime == null)
			return null;
		if (System.currentTimeMillis() > expireTime) {
			m_executor.execute(CreateRemoveRunnable(name));
			return null;
		}
		return m_objects.get(name);
	}

	@SuppressWarnings("unchecked")
	public <R extends T> R Get(final String name, final Class<R> type) {
		return (R) Get(name);
	}
	
	public static void main(String[] args) throws InterruptedException {
		SimpleCache<Integer> cache = new SimpleCache<>(5);
		cache.Put("test", 188);
		cache.Put("test", 189);
		System.out.println(cache.Get("test"));
		Thread.sleep(5000);
		System.out.println(cache.Get("test"));
	}
}