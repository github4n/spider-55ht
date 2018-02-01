package com.haitao55.spider.common.service.impl;

import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.service.MonitorService;
import com.haitao55.spider.common.utils.Constants;

/**
 * 
 * 功能：监控日志服务接口实现类
 * 
 * @author Arthur.Liu
 * @time 2016年8月3日 下午3:18:03
 * @version 1.0
 */
public class MonitorServiceImpl implements MonitorService {
	private static final Logger loggerMonitor = LoggerFactory.getLogger(Constants.LOGGER_NAME_MONITOR);
	private static final Logger loggerSystem = LoggerFactory.getLogger(Constants.LOGGER_NAME_SYSTEM);

	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final Timer TIMER = new Timer("Monitor-Flush-Fields-Timer");
	private static final long FLUSH_PERIOD = 60 * 1000;

	private String namespace = "spider.haitao55";
	private String name = "monitor";

	/** 计数器对象 */
	private ConcurrentMap<String, AtomicInteger> monitor = new ConcurrentHashMap<String, AtomicInteger>();

	/**
	 * 默认构造方法,使用默认的namespace和name
	 */
	public MonitorServiceImpl() {
		this.startFlushFieldsTimer();
	}

	/**
	 * 定制构造方法,使用定制的namespace和name
	 * 
	 * @param namespace
	 * @param name
	 */
	public MonitorServiceImpl(String namespace, String name) {
		this.namespace = namespace;
		this.name = name;

		this.startFlushFieldsTimer();
	}

	@Override
	public void incField(String field) {
		if (!this.monitor.containsKey(field)) {
			synchronized (this.monitor) {
				if (!this.monitor.containsKey(field)) {
					this.monitor.put(field, new AtomicInteger(0));
				}
			}
		}

		this.monitor.get(field).incrementAndGet();
	}

	@Override
	public void incField(String field, int num) {
		if (!this.monitor.containsKey(field)) {
			synchronized (this.monitor) {
				if (!this.monitor.containsKey(field)) {
					this.monitor.put(field, new AtomicInteger(0));
				}
			}
		}

		this.monitor.get(field).addAndGet(num);
	}

	private void flushFields() {
		ConcurrentMap<String, AtomicInteger> existsMonitor = null;
		synchronized (this.monitor) {
			existsMonitor = this.monitor;
			this.monitor = new ConcurrentHashMap<String, AtomicInteger>();
		}

		if (existsMonitor == null || existsMonitor.isEmpty()) {
			loggerSystem.warn("MonitorServiceImpl.monitor is empty!!!");
			return;
		}

		JSONObject jsonMonitor = new JSONObject();
		jsonMonitor.put("time", new SimpleDateFormat(DATE_FORMAT).format(System.currentTimeMillis()));
		jsonMonitor.put("namespace", namespace);
		jsonMonitor.put("name", name);

		JSONArray jsonDataArray = new JSONArray();
		for (Entry<String, AtomicInteger> entry : existsMonitor.entrySet()) {
			String key = entry.getKey();
			AtomicInteger value = entry.getValue();

			JSONObject jsonData = new JSONObject();
			jsonData.put("k", key);
			jsonData.put("v", value.intValue());
			jsonDataArray.add(jsonData);
		}
		jsonMonitor.put("data", jsonDataArray);

		loggerMonitor.info(jsonMonitor.toJSONString());
	}

	private void startFlushFieldsTimer() {
		TIMER.schedule(new TimerTask() {
			@Override
			public void run() {
				MonitorServiceImpl.this.flushFields();
			}
		}, FLUSH_PERIOD, FLUSH_PERIOD);
	}
}