package com.haitao55.spider.common.dao;

import com.haitao55.spider.common.dos.RealtimeStatisticsDO;

/**
 * RealtimeStatistics DAO
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年1月19日 下午2:49:16
* @version 1.0
 */
public interface RealtimeStatisticsDAO {

	/**
	 * 根据taskId 和 realtimeTime查询记录
	 * @param taskId
	 * @param realtimeTime
	 * @return
	 */
	RealtimeStatisticsDO findByPrimaryKey(Long taskId, String realtimeTime);
	
}
