package com.haitao55.spider.common.service;

/**
 * 
 * 功能：监控日志服务接口
 * 
 * @author Arthur.Liu
 * @time 2016年8月3日 下午3:16:25
 * @version 1.0
 */
public interface MonitorService {
	/**
	 * 计数，每次累加1
	 * 
	 * @param field
	 */
	public void incField(String field);

	/**
	 * 计数，每次累加给定数量
	 * 
	 * @param field
	 * @param num
	 */
	public void incField(String field, int num);
}