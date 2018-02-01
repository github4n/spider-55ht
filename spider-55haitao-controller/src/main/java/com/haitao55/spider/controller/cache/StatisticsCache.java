package com.haitao55.spider.controller.cache;

import java.util.concurrent.ConcurrentHashMap;

import com.haitao55.spider.common.dos.StatisticsDO;

/**
 * 
* Title: 维护 任务,urls到执行结果,成功,失败,下架,处理数量
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2016年9月23日 下午5:09:40
* @version 1.0
 */
public class StatisticsCache extends ConcurrentHashMap<Long, StatisticsDO>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private  StatisticsCache(){}
	
	private static class StatisticsCacheHolder{
		private static final StatisticsCache statisticsCache=new StatisticsCache();
	}
	
	public static StatisticsCache getInstance(){
		return StatisticsCacheHolder.statisticsCache;
	}
}
