package com.haitao55.spider.ui.service.impl;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.ui.service.InfluxdbService;

/**
 * 
 * 功能：数据库Influxdb操作服务接口实现类
 * 
 * @author Arthur.Liu
 * @time 2016年11月29日 下午5:07:37
 * @version 1.0
 */
@Service("influxdbService")
public class InfluxdbServiceImpl implements InfluxdbService {
	private static final Logger logger = LoggerFactory.getLogger("system");

	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	private static final String COME_FROM_GOOGLEVPS = "googlevps";

	private static final String MONITOR_LOG_FIELD_NAME_IP = "ip";
	private static final String MONITOR_LOG_FIELD_NAME_TIME = "time";
	private static final String MONITOR_LOG_FIELD_NAME_NAMESPACE = "namespace";
	private static final String MONITOR_LOG_FIELD_NAME_NAME = "name";
	private static final String MONITOR_LOG_FIELD_NAME_DATA = "data";

	private static final String DATA_KEY = "k";
	private static final String DATA_VALUE = "v";

	@Value("#{configProperties['influxdb.url']}")
	private String influxdbUrl;
	@Value("#{configProperties['influxdb.username']}")
	private String influxdbUsername;
	@Value("#{configProperties['influxdb.password']}")
	private String influxdbPassword;
	@Value("#{configProperties['influxdb.database.name']}")
	private String influxdbDatabaseName;

	private InfluxDB influxDB = null;

	public String getInfluxdbUrl() {
		return influxdbUrl;
	}

	public void setInfluxdbUrl(String influxdbUrl) {
		this.influxdbUrl = influxdbUrl;
	}

	public String getInfluxdbUsername() {
		return influxdbUsername;
	}

	public void setInfluxdbUsername(String influxdbUsername) {
		this.influxdbUsername = influxdbUsername;
	}

	public String getInfluxdbPassword() {
		return influxdbPassword;
	}

	public void setInfluxdbPassword(String influxdbPassword) {
		this.influxdbPassword = influxdbPassword;
	}

	public String getInfluxdbDatabaseName() {
		return influxdbDatabaseName;
	}

	public void setInfluxdbDatabaseName(String influxdbDatabaseName) {
		this.influxdbDatabaseName = influxdbDatabaseName;
	}

	@PostConstruct
	public void init() {
		try {
			this.influxDB = InfluxDBFactory.connect(this.influxdbUrl, this.influxdbUsername, this.influxdbPassword);
			this.influxDB.createDatabase(this.influxdbDatabaseName);// 不存在才创建
		} catch (Exception e) {

		}
	}

	@PreDestroy
	public void destroy() {
		// ignore
	}

	@Override
	public void writeInfluxdb(String ip, String module, String content, String comeFrom) throws Exception {
		if (StringUtils.isBlank(module)) {
			logger.warn("module should not be blank!");
			return;
		}

		JSONObject jsonObject = JSONObject.parseObject(content);
		if (ObjectUtils.isEmpty(jsonObject)) {
			logger.warn("content should not be blank!");
			return;
		}

		String timeStr = jsonObject.getString(MONITOR_LOG_FIELD_NAME_TIME);
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		long time = sdf.parse(timeStr).getTime();
		time = (time / (60 * 1000)) * (60 * 1000);// 精确到分钟,舍去秒数
		time = this.adjustMonitorLogTime(time, comeFrom);

		String namespace = jsonObject.getString(MONITOR_LOG_FIELD_NAME_NAMESPACE);
		String name = jsonObject.getString(MONITOR_LOG_FIELD_NAME_NAME);

		JSONArray data = jsonObject.getJSONArray(MONITOR_LOG_FIELD_NAME_DATA);
		if (ObjectUtils.isEmpty(data)) {
			logger.warn("data should not be blank!");
			return;
		}

		BatchPoints batchPoints = BatchPoints.database(this.influxdbDatabaseName).tag(MONITOR_LOG_FIELD_NAME_IP, ip)
				.retentionPolicy("default").consistency(ConsistencyLevel.ALL).build();

		Point.Builder pointBuilder = Point.measurement(module).time(time, TimeUnit.MILLISECONDS)
				.addField(MONITOR_LOG_FIELD_NAME_NAMESPACE, namespace).addField(MONITOR_LOG_FIELD_NAME_NAME, name);
		for (int i = 0; i < data.size(); i++) {
			JSONObject element = data.getJSONObject(i);
			String k = element.getString(DATA_KEY);
			String v = element.getString(DATA_VALUE);

			try {
				long value = Long.parseLong(v);
				pointBuilder.addField(k, value);
			} catch (Exception e) {
				continue;
			}
		}
		Point point = pointBuilder.build();

		batchPoints.point(point);

		influxDB.write(batchPoints);
	}

	private long adjustMonitorLogTime(long time, String comeFrom) {
		long rst = time;

		if (StringUtils.equals(COME_FROM_GOOGLEVPS, comeFrom)) {
			rst = time + 1000 * 60 * 60 * 8;
		}

		return rst;
	}
}