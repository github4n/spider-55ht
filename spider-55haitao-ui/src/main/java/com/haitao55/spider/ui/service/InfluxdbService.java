package com.haitao55.spider.ui.service;

/**
 * 
 * 功能：数据库Influxdb操作服务接口
 * 
 * @author Arthur.Liu
 * @time 2016年11月29日 下午5:05:55
 * @version 1.0
 */
public interface InfluxdbService {
	/**
	 * 写监控日志内容到influxdb数据库中
	 * 
	 * @param ip
	 * @param module
	 * @param content
	 * @param comeFrom
	 */
	public void writeInfluxdb(String ip, String module, String content, String comeFrom) throws Exception;
}